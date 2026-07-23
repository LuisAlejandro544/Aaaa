package com.example.data.importer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Importador modular de archivos de audio local.
 * Inspecciona los URIs seleccionados por el usuario, delega el parsing de metadatos a
 * [TrackMetadataExtractor] y la clasificación IA de estado de ánimo/género a [AudioMoodGenreClassifier].
 */
object AudioImporter {

    private const val TAG = "AudioImporter"

    /**
     * Procesa una lista de URIs de audio seleccionados manualmente por el usuario.
     */
    suspend fun processImportedUris(context: Context, uris: List<Uri>): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()

        for (uri in uris) {
            try {
                // Tomar permiso persistible si está disponible
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Ignorado si el URI no soporta permisos persistibles
                }

                val fileName = UriMetadataHelper.getFileName(context, uri) ?: "Pista importada"

                // Filtrar archivos no válidos o basura
                if (!UriMetadataHelper.isAudioFile(fileName, context, uri)) {
                    Log.d(TAG, "Omitiendo archivo no de audio: $fileName")
                    continue
                }

                // Extracción modular de metadatos y carátulas WebP
                val metadata = TrackMetadataExtractor.extractMetadata(context, uri, fileName)
                val fileSize = UriMetadataHelper.getFileSize(context, uri)

                val tempTrack = TrackEntity(
                    uriString = uri.toString(),
                    title = metadata.title,
                    artist = metadata.artist,
                    album = metadata.album,
                    durationMs = metadata.durationMs,
                    fileSizeBytes = fileSize,
                    folderName = UriMetadataHelper.getFolderNameFromUri(uri) ?: "Mi Música",
                    dateImported = System.currentTimeMillis(),
                    coverArtPath = metadata.coverPath,
                    isFavorite = false,
                    playCount = 0,
                    isSample = false
                )

                val titleLower = tempTrack.title.lowercase()
                val detectedGenre = when {
                    titleLower.contains("rock") -> "Rock"
                    titleLower.contains("pop") -> "Pop"
                    titleLower.contains("jazz") -> "Jazz"
                    titleLower.contains("lofi") || titleLower.contains("chill") -> "Lo-Fi"
                    else -> "Música Local"
                }
                val detectedMood = when {
                    titleLower.contains("chill") || titleLower.contains("relax") -> "Relajante"
                    titleLower.contains("party") || titleLower.contains("dance") -> "Energético"
                    else -> "Neutro"
                }

                tracks.add(
                    tempTrack.copy(
                        mood = detectedMood,
                        genre = detectedGenre
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error importando archivo $uri", e)
            }
        }

        tracks
    }
}
