package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.db.TrackEntity
import com.example.util.AudioFingerprintEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MediaStorageDelegate {

    private const val TAG = "MediaStorageDelegate"

    suspend fun saveCustomCanvasVideoFromUri(
        context: Context,
        videoUri: Uri,
        trackId: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(LocalStorageManager.getVideosDir(context), "canvas_video_${trackId}_${System.currentTimeMillis()}.mp4")
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            Log.d(TAG, "Canvas video saved successfully: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving canvas video from Uri", e)
            null
        }
    }

    /**
     * Importa un archivo de video (MP4/MKV) como una canción de la biblioteca,
     * extrayendo su fotograma como carátula WebP y asignando el video como su Canvas de fondo.
     */
    suspend fun importVideoAsTrackAndCanvas(
        context: Context,
        videoUri: Uri
    ): TrackEntity? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)

            val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 180000L

            val fileName = getFileNameFromUri(context, videoUri) ?: "Video_Canvas_${System.currentTimeMillis()}"
            val cleanTitle = if (!rawTitle.isNullOrBlank()) rawTitle else fileName.substringBeforeLast('.')
            val cleanArtist = if (!rawArtist.isNullOrBlank()) rawArtist else "Canvas Video Original"

            // Save video file to videos/ with buffered stream copy for large 4K/long videos
            val videoFile = File(LocalStorageManager.getVideosDir(context), "canvas_${System.currentTimeMillis()}_${sanitizeFileName(fileName)}.mp4")
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(videoFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            } ?: return@withContext null

            // Extract thumbnail frame at 1s timestamp with fallback options for high resolution
            var coverPath: String? = null
            try {
                val frameBitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
                if (frameBitmap != null) {
                    val imgFile = File(LocalStorageManager.getImagesDir(context), "video_thumb_${System.currentTimeMillis()}.webp")
                    FileOutputStream(imgFile).use { out ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            frameBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 90, out)
                        } else {
                            @Suppress("DEPRECATION")
                            frameBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
                        }
                    }
                    frameBitmap.recycle()
                    coverPath = imgFile.absolutePath
                }
            } catch (e: Exception) {
                Log.w(TAG, "Frame extraction failed, fallback to seed cover: ${e.message}")
            }

            if (coverPath == null) {
                coverPath = ImageStorageDelegate.generateSeedWebpCover(context, cleanTitle, cleanArtist, videoFile.absolutePath)
            }

            retriever.release()

            val acousticHash = AudioFingerprintEngine.computeFingerprintHash(videoFile.length(), durationMs)

            TrackEntity(
                title = cleanTitle,
                artist = cleanArtist,
                album = "Video Canvas Import",
                durationMs = durationMs,
                fileSizeBytes = videoFile.length(),
                uriString = Uri.fromFile(videoFile).toString(),
                folderName = "Videos",
                coverArtPath = coverPath,
                isFavorite = false,
                isSample = false,
                lyrics = null,
                loudnessLufs = -14.0f,
                acousticHash = acousticHash,
                canvasVideoPath = videoFile.absolutePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error importing video as track and canvas", e)
            null
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) name = cursor.getString(index)
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
    }
}
