package com.example.data.storage

import android.content.Context
import android.util.Log
import com.example.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object JsonStorageDelegate {

    private const val TAG = "JsonStorageDelegate"

    /**
     * Synchronizes track metadata into json/library_cache.json
     * and individual json/track_{id}.json files in background thread.
     */
    suspend fun syncMetadataJsonCache(
        context: Context,
        tracks: List<TrackEntity>
    ) = withContext(Dispatchers.IO) {
        try {
            val jsonDir = LocalStorageManager.getJsonDir(context)
            val rootArray = JSONArray()

            tracks.forEach { track ->
                val trackObj = JSONObject().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("durationMs", track.durationMs)
                    put("fileSizeBytes", track.fileSizeBytes)
                    put("uriString", track.uriString)
                    put("folderName", track.folderName)
                    put("webpCoverPath", track.coverArtPath ?: "")
                    put("isFavorite", track.isFavorite)
                    put("isSample", track.isSample)
                    put("dateImported", track.dateImported)
                    put("lyrics", track.lyrics ?: "")
                }
                rootArray.put(trackObj)

                // Save individual track json file
                val individualFile = File(jsonDir, "track_${track.id}.json")
                individualFile.writeText(trackObj.toString(2))
            }

            // Save master index cache
            val masterFile = File(jsonDir, "library_cache.json")
            val rootObj = JSONObject().apply {
                put("app", "SpotLocal")
                put("version", "1.0")
                put("lastUpdated", System.currentTimeMillis())
                put("totalTracks", tracks.size)
                put("tracks", rootArray)
            }
            masterFile.writeText(rootObj.toString(2))

            Log.d(TAG, "Successfully synced ${tracks.size} tracks into json/library_cache.json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync metadata JSON cache", e)
        }
    }
}
