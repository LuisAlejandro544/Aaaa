package com.example.data.importer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.data.rust.RustMetadataParser
import com.example.data.storage.LocalStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extractor modular de metadatos y arte de portada para pistas de audio importadas.
 * Combina parsing ultra-seguro en Rust con MediaMetadataRetriever nativo.
 */
object TrackMetadataExtractor {

    private const val TAG = "TrackMetadataExtractor"

    data class ExtractedMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val coverPath: String?
    )

    suspend fun extractMetadata(
        context: Context,
        uri: Uri,
        fileName: String
    ): ExtractedMetadata = withContext(Dispatchers.IO) {
        val rustMetadata = RustMetadataParser.parseAudioTags(context, uri, fileName)
        Log.d(TAG, "Rust Tag Reader procesó archivo ($fileName): Formato=${rustMetadata.format}")

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

            val embeddedPicture = rustMetadata.embeddedPictureBytes ?: retriever.embeddedPicture
            if (embeddedPicture != null && embeddedPicture.isNotEmpty()) {
                coverPath = LocalStorageManager.saveWebpCoverFromBytes(
                    context,
                    embeddedPicture,
                    uri.hashCode().toString()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo metadatos para $uri", e)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignorar
            }
        }

        // Fallbacks para campos vacíos
        if (title.isBlank()) {
            title = UriMetadataHelper.cleanFileName(fileName)
        }
        if (artist.isBlank()) {
            artist = "Artista desconocido"
        }
        if (album.isBlank()) {
            album = "Álbum local"
        }

        // Generar carátula por semilla de metadatos si no traía embebida
        if (coverPath == null) {
            coverPath = LocalStorageManager.generateSeedWebpCover(
                context = context,
                title = title,
                artist = artist,
                seedInput = uri.toString()
            )
        }

        ExtractedMetadata(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            coverPath = coverPath
        )
    }
}
