package com.example.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LocalStorageManager {

    private const val TAG = "LocalStorageManager"

    /**
     * Directory structure in Android/data/<package>/files/
     * ├── music/
     * ├── images/
     * ├── videos/
     * └── json/
     */
    fun getMusicDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(baseDir, "music")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getImagesDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(baseDir, "images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getJsonDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(baseDir, "json")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getVideosDir(context: Context): File {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        val dir = File(baseDir, "videos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun saveCustomCanvasVideoFromUri(
        context: Context,
        videoUri: Uri,
        trackId: Long
    ): String? = MediaStorageDelegate.saveCustomCanvasVideoFromUri(context, videoUri, trackId)

    suspend fun importVideoAsTrackAndCanvas(
        context: Context,
        videoUri: Uri
    ): TrackEntity? = MediaStorageDelegate.importVideoAsTrackAndCanvas(context, videoUri)

    suspend fun saveWebpCoverFromBytes(
        context: Context,
        bytes: ByteArray,
        identifier: String
    ): String? = ImageStorageDelegate.saveWebpCoverFromBytes(context, bytes, identifier)

    suspend fun saveCustomWebpCoverFromUri(
        context: Context,
        imageUri: Uri,
        trackId: Long
    ): String? = ImageStorageDelegate.saveCustomWebpCoverFromUri(context, imageUri, trackId)

    suspend fun generateSeedWebpCover(
        context: Context,
        title: String,
        artist: String,
        seedInput: String
    ): String? = ImageStorageDelegate.generateSeedWebpCover(context, title, artist, seedInput)

    suspend fun syncMetadataJsonCache(
        context: Context,
        tracks: List<TrackEntity>
    ) = JsonStorageDelegate.syncMetadataJsonCache(context, tracks)

    /**
     * Cleans up all generated files in music/, images/, videos/, and json/ subfolders manually.
     * Note: Android OS automatically deletes all files in context.getExternalFilesDir()
     * when the application is uninstalled by the user.
     */
    suspend fun clearAllCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            getMusicDir(context).deleteRecursively()
            getImagesDir(context).deleteRecursively()
            getJsonDir(context).deleteRecursively()
            getVideosDir(context).deleteRecursively()
            Log.d(TAG, "All local app cache folders wiped successfully.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear local cache folders", e)
            false
        }
    }
}
