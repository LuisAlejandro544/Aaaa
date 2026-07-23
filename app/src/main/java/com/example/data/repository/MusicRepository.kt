package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistTrackCrossRef
import com.example.data.db.TrackEntity
import com.example.data.importer.AudioImporter
import com.example.data.importer.SampleAudioGenerator
import com.example.data.storage.LocalStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MusicRepository(private val db: AppDatabase) {

    private val trackDao = db.trackDao()
    private val playlistDao = db.playlistDao()
    private val playlistDelegate = PlaylistRepositoryDelegate(playlistDao)

    val allTracks: Flow<List<TrackEntity>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<TrackEntity>> = trackDao.getFavoriteTracks()
    val recentTracks: Flow<List<TrackEntity>> = trackDao.getRecentTracks()
    val folders: Flow<List<String>> = trackDao.getFolders()
    val playlists: Flow<List<PlaylistEntity>> = playlistDelegate.playlists

    suspend fun loadDemoTracksIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        val currentTracks = trackDao.getAllTracks().first()
        if (currentTracks.isEmpty()) {
            val demos = SampleAudioGenerator.createDemoTracks(context)
            trackDao.insertTracks(demos)

            // Create default "Canciones que te gustan" playlist entry or favorites setup
            val favs = demos.filter { it.isFavorite }
            if (favs.isNotEmpty()) {
                val favPlaylistId = playlistDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Canciones que te gustan",
                        description = "Tus canciones favoritas importadas localmente"
                    )
                )
                demos.forEach { track ->
                    if (track.isFavorite) {
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(favPlaylistId, track.id))
                    }
                }
            }
            LocalStorageManager.syncMetadataJsonCache(context, demos)
        } else {
            LocalStorageManager.syncMetadataJsonCache(context, currentTracks)
        }
    }

    suspend fun importUris(context: Context, uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val newTracks = AudioImporter.processImportedUris(context, uris)
        if (newTracks.isNotEmpty()) {
            trackDao.insertTracks(newTracks)
            val updatedAll = trackDao.getAllTracks().first()
            LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        }
        newTracks.size
    }

    suspend fun importVideoAsTrackAndCanvas(context: Context, videoUri: Uri): TrackEntity? = withContext(Dispatchers.IO) {
        val track = LocalStorageManager.importVideoAsTrackAndCanvas(context, videoUri)
            ?: return@withContext null

        trackDao.insertTracks(listOf(track))
        val updatedAll = trackDao.getAllTracks().first()
        val savedTrack = updatedAll.find { it.uriString == track.uriString } ?: track
        LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        savedTrack
    }

    suspend fun updateCustomCoverArt(context: Context, track: TrackEntity, imageUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val webpPath = LocalStorageManager.saveCustomWebpCoverFromUri(
            context = context,
            imageUri = imageUri,
            trackId = track.id
        ) ?: return@withContext false

        val updatedTrack = track.copy(coverArtPath = webpPath)
        trackDao.updateTrack(updatedTrack)

        val updatedAll = trackDao.getAllTracks().first()
        LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        true
    }

    suspend fun updateCanvasVideo(context: Context, track: TrackEntity, videoUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val videoPath = LocalStorageManager.saveCustomCanvasVideoFromUri(
            context = context,
            videoUri = videoUri,
            trackId = track.id
        ) ?: return@withContext false

        val updatedTrack = track.copy(canvasVideoPath = videoPath)
        trackDao.updateTrack(updatedTrack)

        val updatedAll = trackDao.getAllTracks().first()
        LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        true
    }

    suspend fun removeCanvasVideo(context: Context, track: TrackEntity) = withContext(Dispatchers.IO) {
        val updatedTrack = track.copy(canvasVideoPath = null)
        trackDao.updateTrack(updatedTrack)

        val updatedAll = trackDao.getAllTracks().first()
        LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
    }

    suspend fun toggleFavorite(track: TrackEntity) = withContext(Dispatchers.IO) {
        val updated = track.copy(isFavorite = !track.isFavorite)
        trackDao.updateTrack(updated)
    }

    suspend fun updateTrack(track: TrackEntity) = withContext(Dispatchers.IO) {
        trackDao.updateTrack(track)
    }

    suspend fun deleteTrack(track: TrackEntity) = withContext(Dispatchers.IO) {
        trackDao.deleteTrack(track)
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return playlistDelegate.createPlaylist(name, description)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        playlistDelegate.addTrackToPlaylist(playlistId, trackId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDelegate.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>> {
        return playlistDelegate.getTracksForPlaylist(playlistId)
    }

    fun getTracksByFolder(folderName: String): Flow<List<TrackEntity>> {
        return trackDao.getTracksByFolder(folderName)
    }

    fun searchTracks(query: String): Flow<List<TrackEntity>> {
        return trackDao.searchTracks(query)
    }

    suspend fun exportLibraryToJson(context: Context, destinationUri: Uri): Boolean {
        return LibraryExportHelper.exportLibraryToJson(context, destinationUri, trackDao, playlistDao)
    }

    suspend fun updateLyrics(context: Context, track: TrackEntity, lyricsText: String) = withContext(Dispatchers.IO) {
        val updated = track.copy(lyrics = lyricsText.ifBlank { null })
        trackDao.updateTrack(updated)
        val all = trackDao.getAllTracks().first()
        LocalStorageManager.syncMetadataJsonCache(context, all)
    }

    suspend fun clearAppCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        LocalStorageManager.clearAllCache(context)
    }
}
