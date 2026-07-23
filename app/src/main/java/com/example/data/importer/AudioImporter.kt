package com.example.data.importer

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.data.db.TrackEntity
import com.example.data.rust.RustMetadataParser
import com.example.data.storage.LocalStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AudioImporter {

    private const val TAG = "AudioImporter"

    /**
     * Inspects a list of audio URIs selected manually by the user, extracts ID3 tags,
     * extracts cover art if available, and returns a list of TrackEntity objects.
     */
    suspend fun processImportedUris(context: Context, uris: List<Uri>): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()

        for (uri in uris) {
            try {
                // Take persistable permission if possible
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Ignored if uri doesn't support persistable permissions
                }

                val fileName = UriMetadataHelper.getFileName(context, uri) ?: "Pista importada"
                
                // Skip if obvious junk non-audio or whatsapp voice notes if user selected folder by mistake
                if (!UriMetadataHelper.isAudioFile(fileName, context, uri)) {
                    Log.d(TAG, "Skipping non-audio file: $fileName")
                    continue
                }

                // Execute memory-safe Rust metadata parser verification for ID3/FLAC/OGG/WAV
                val rustMetadata = RustMetadataParser.parseAudioTags(context, uri, fileName)
                Log.d(TAG, "Rust Tag Reader processed file ($fileName): Format=${rustMetadata.format}")

                var title = ""
                var artist = ""
                var album = ""
                var durationMs = 0L
                var coverPath: String? = null

                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)

                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                    
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durationStr?.toLongOrNull() ?: 0L

                    // Extract embedded album art (APIC/PICTURE frames) parsed by Rust direct engine or MediaMetadataRetriever
                    val embeddedPicture = rustMetadata.embeddedPictureBytes ?: retriever.embeddedPicture
                    if (embeddedPicture != null && embeddedPicture.isNotEmpty()) {
                        coverPath = LocalStorageManager.saveWebpCoverFromBytes(
                            context,
                            embeddedPicture,
                            uri.hashCode().toString()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading metadata for $uri", e)
                } finally {
                    try {
                        retriever.release()
                    } catch (e: Exception) {
                        // Ignore
                    }
                }

                // Fallbacks for empty metadata
                if (title.isBlank()) {
                    title = UriMetadataHelper.cleanFileName(fileName)
                }
                if (artist.isBlank()) {
                    artist = "Artista desconocido"
                }
                if (album.isBlank()) {
                    album = "Álbum local"
                }

                // If no embedded cover art was found, generate seed-based WebP cover art
                if (coverPath == null) {
                    coverPath = LocalStorageManager.generateSeedWebpCover(
                        context = context,
                        title = title,
                        artist = artist,
                        seedInput = uri.toString()
                    )
                }

                val fileSize = UriMetadataHelper.getFileSize(context, uri)

                val tempTrack = TrackEntity(
                    uriString = uri.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = durationMs,
                    fileSizeBytes = fileSize,
                    folderName = UriMetadataHelper.getFolderNameFromUri(uri) ?: "Mi Música",
                    dateImported = System.currentTimeMillis(),
                    coverArtPath = coverPath,
                    isFavorite = false,
                    playCount = 0,
                    isSample = false
                )

                val classification = com.example.data.ai.AudioMoodGenreClassifier.classify(tempTrack)

                tracks.add(
                    tempTrack.copy(
                        mood = classification.mood,
                        genre = classification.genre
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to import file $uri", e)
            }
        }

        tracks
    }
}
