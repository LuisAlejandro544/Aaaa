package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.PlaylistDao
import com.example.data.db.TrackDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter

object LibraryExportHelper {

    suspend fun exportLibraryToJson(
        context: Context,
        destinationUri: Uri,
        trackDao: TrackDao,
        playlistDao: PlaylistDao
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val tracks = trackDao.getAllTracks().first()
            val playlistsList = playlistDao.getAllPlaylists().first()

            val rootJson = JSONObject()
            rootJson.put("app", "SpotLocal")
            rootJson.put("version", "1.0")
            rootJson.put("exportedAt", System.currentTimeMillis())

            val tracksArray = JSONArray()
            tracks.forEach { t ->
                val trackObj = JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("artist", t.artist)
                    put("album", t.album)
                    put("durationMs", t.durationMs)
                    put("uriString", t.uriString)
                    put("folderName", t.folderName)
                    put("isFavorite", t.isFavorite)
                }
                tracksArray.put(trackObj)
            }
            rootJson.put("tracks", tracksArray)

            val playlistArray = JSONArray()
            playlistsList.forEach { p ->
                val pObj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("description", p.description)
                }
                playlistArray.put(pObj)
            }
            rootJson.put("playlists", playlistArray)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
