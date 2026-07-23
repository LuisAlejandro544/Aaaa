package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.example.data.repository.MusicRepository
import com.example.player.Audio3dSpeakerMode
import com.example.player.MusicPlayerManager
import com.example.player.RepeatMode
import com.example.player.SleepTimerOption
import com.example.player.SpatialReverbEnvironment
import com.example.player.VolumeController
import com.example.player.VolumeState
import com.example.ui.components.player.EqPreset
import com.example.ui.viewmodel.delegates.LibraryDelegate
import com.example.ui.viewmodel.delegates.NavigationDelegate
import com.example.util.DuplicateCluster
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SpotifyTab {
    HOME, SEARCH, LIBRARY, SETTINGS
}

sealed class PlaylistDetailTarget {
    object Favorites : PlaylistDetailTarget()
    data class CustomPlaylist(val playlist: PlaylistEntity) : PlaylistDetailTarget()
    data class Folder(val folderName: String) : PlaylistDetailTarget()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MusicRepository(db)
    val playerManager = MusicPlayerManager(application)
    val volumeController = VolumeController(application)
    val volumeState: StateFlow<VolumeState> = volumeController.volumeState

    val navDelegate = NavigationDelegate()
    val libraryDelegate = LibraryDelegate(repository, viewModelScope)

    // AI Settings Manager & State
    val aiSettingsManager = com.example.data.ai.AiSettingsManager(application)
    val aiSettingsState: StateFlow<com.example.data.ai.AiSettingsState> = aiSettingsManager.settings

    private val _translatedLyrics = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val translatedLyrics: StateFlow<String?> = _translatedLyrics.asStateFlow()

    private val _isTranslatingLyrics = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isTranslatingLyrics: StateFlow<Boolean> = _isTranslatingLyrics.asStateFlow()

    private val _songExplanation = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val songExplanation: StateFlow<String?> = _songExplanation.asStateFlow()

    private val _isExplainingSong = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isExplainingSong: StateFlow<Boolean> = _isExplainingSong.asStateFlow()

    private val _showSmartVibeDialog = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showSmartVibeDialog: StateFlow<Boolean> = _showSmartVibeDialog.asStateFlow()

    private val _showLyricsExplanationSheet = kotlinx.coroutines.flow.MutableStateFlow(false)
    val showLyricsExplanationSheet: StateFlow<Boolean> = _showLyricsExplanationSheet.asStateFlow()

    private val _explanationModalType = kotlinx.coroutines.flow.MutableStateFlow("explanation") // "explanation" or "translation"
    val explanationModalType: StateFlow<String> = _explanationModalType.asStateFlow()

    // Navigation state
    val currentTab: StateFlow<SpotifyTab> = navDelegate.currentTab
    val isPlayerExpanded: StateFlow<Boolean> = navDelegate.isPlayerExpanded
    val openedPlaylistDetail: StateFlow<PlaylistDetailTarget?> = navDelegate.openedPlaylistDetail

    // Library & Search state
    val searchQuery: StateFlow<String> = libraryDelegate.searchQuery
    val selectedFilterChip: StateFlow<String> = libraryDelegate.selectedFilterChip
    val showImportInfoDialog: StateFlow<Boolean> = libraryDelegate.showImportInfoDialog
    val showCreatePlaylistDialog: StateFlow<Boolean> = libraryDelegate.showCreatePlaylistDialog
    val showTrackOptionsDialog: StateFlow<TrackEntity?> = libraryDelegate.showTrackOptionsDialog
    val showExportDialog: StateFlow<Boolean> = libraryDelegate.showExportDialog
    val importMessage: StateFlow<String?> = libraryDelegate.importMessage
    val duplicateClusters: StateFlow<List<DuplicateCluster>> = libraryDelegate.duplicateClusters
    val isScanningDuplicates: StateFlow<Boolean> = libraryDelegate.isScanningDuplicates
    val isImportingVideo: StateFlow<Boolean> = libraryDelegate.isImportingVideo
    val importVideoProgressText: StateFlow<String> = libraryDelegate.importVideoProgressText

    // Player State Forwarding
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val queue: StateFlow<List<TrackEntity>> = playerManager.queue
    val currentIndex: StateFlow<Int> = playerManager.currentIndex
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val playbackSpeed: StateFlow<Float> = playerManager.playbackSpeed
    val playbackPitch: StateFlow<Float> = playerManager.playbackPitch
    val playbackError: StateFlow<String?> = playerManager.playbackError

    // Sleep Timer State
    val sleepTimerOption: StateFlow<SleepTimerOption> = playerManager.sleepTimerOption
    val sleepTimerRemainingMs: StateFlow<Long?> = playerManager.sleepTimerRemainingMs

    // DSP State
    val isEqEnabled: StateFlow<Boolean> = playerManager.isEqEnabled
    val bandGainsDb: StateFlow<FloatArray> = playerManager.bandGainsDb
    val eqPreset: StateFlow<EqPreset> = playerManager.eqPreset
    val is3dAudioEnabled: StateFlow<Boolean> = playerManager.is3dAudioEnabled
    val audio3dStrength: StateFlow<Float> = playerManager.audio3dStrength
    val audio3dMode: StateFlow<Audio3dSpeakerMode> = playerManager.audio3dMode
    val reverbEnvironment: StateFlow<SpatialReverbEnvironment> = playerManager.reverbEnvironment
    val reverbStrength: StateFlow<Float> = playerManager.reverbStrength
    val crossfadeDurationSec: StateFlow<Float> = playerManager.crossfadeDurationSec
    val isVolumeNormalizerEnabled: StateFlow<Boolean> = playerManager.isVolumeNormalizerEnabled
    val targetLufs: StateFlow<Float> = playerManager.targetLufs

    // DB Flows
    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<TrackEntity>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTracks: StateFlow<List<TrackEntity>> = repository.recentTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<TrackEntity>> = combine(searchQuery, repository.allTracks) { query, tracks ->
        if (query.isBlank()) tracks
        else com.example.data.rust.RustFuzzySearchEngine.filterTracksFuzzy(tracks, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePlaylistTracks: StateFlow<List<TrackEntity>> = openedPlaylistDetail
        .flatMapLatest { target ->
            when (target) {
                is PlaylistDetailTarget.Favorites -> repository.favoriteTracks
                is PlaylistDetailTarget.CustomPlaylist -> repository.getTracksForPlaylist(target.playlist.id)
                is PlaylistDetailTarget.Folder -> repository.getTracksByFolder(target.folderName)
                null -> kotlinx.coroutines.flow.MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentTrack: StateFlow<TrackEntity?> = combine(playerManager.currentTrack, allTracks) { playing, tracks ->
        if (playing == null) null
        else tracks.find { it.id == playing.id } ?: playing
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.loadDemoTracksIfEmpty(getApplication())
        }
    }

    // Navigation delegate actions
    fun selectTab(tab: SpotifyTab) = navDelegate.selectTab(tab)
    fun setPlayerExpanded(expanded: Boolean) = navDelegate.setPlayerExpanded(expanded)
    fun openPlaylistDetail(target: PlaylistDetailTarget?) = navDelegate.openPlaylistDetail(target)

    // Library delegate actions
    fun updateSearchQuery(query: String) = libraryDelegate.updateSearchQuery(query)
    fun setFilterChip(chip: String) = libraryDelegate.setFilterChip(chip)
    fun setShowImportInfoDialog(show: Boolean) = libraryDelegate.setShowImportInfoDialog(show)
    fun setShowCreatePlaylistDialog(show: Boolean) = libraryDelegate.setShowCreatePlaylistDialog(show)
    fun setShowTrackOptionsDialog(track: TrackEntity?) = libraryDelegate.setShowTrackOptionsDialog(track)
    fun setShowExportDialog(show: Boolean) = libraryDelegate.setShowExportDialog(show)
    fun clearImportMessage() = libraryDelegate.clearImportMessage()
    fun setImportMessage(msg: String) = libraryDelegate.setImportMessage(msg)

    fun importUris(uris: List<Uri>) = libraryDelegate.importUris(getApplication(), uris)
    
    fun importUrisAndPlay(uris: List<Uri>) {
        viewModelScope.launch {
            libraryDelegate.importUris(getApplication(), uris)
            val firstUriStr = uris.firstOrNull()?.toString()
            if (firstUriStr != null) {
                val all = repository.allTracks.first()
                val importedTrack = all.find { it.uriString == firstUriStr } ?: all.firstOrNull()
                if (importedTrack != null) {
                    playTrack(importedTrack, all)
                    navDelegate.setPlayerExpanded(true)
                }
            }
        }
    }

    fun importVideoAsTrackAndCanvas(videoUri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            libraryDelegate.setImportingVideoState(
                isImporting = true,
                text = "Procesando video de alta calidad...\nExtrayendo audio local y fotograma WebP sin congelar la app."
            )
            try {
                val track = repository.importVideoAsTrackAndCanvas(getApplication(), videoUri)
                if (track != null) {
                    val all = repository.allTracks.first()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        playTrack(track, all)
                        navDelegate.setPlayerExpanded(true)
                    }
                }
            } catch (e: Exception) {
                com.example.util.DebugLogger.logCrash("ImportVideo", "Error procesando video", e)
            } finally {
                libraryDelegate.setImportingVideoState(false)
            }
        }
    }

    fun updateCustomCoverArt(track: TrackEntity, imageUri: Uri) = libraryDelegate.updateCustomCoverArt(getApplication(), track, imageUri)
    fun updateCanvasVideo(track: TrackEntity, videoUri: Uri) = libraryDelegate.updateCanvasVideo(getApplication(), track, videoUri)
    fun removeCanvasVideo(track: TrackEntity) = libraryDelegate.removeCanvasVideo(getApplication(), track)
    fun updateLyrics(track: TrackEntity, newLyrics: String) = libraryDelegate.updateLyrics(getApplication(), track, newLyrics)
    fun cleanTrackTags(track: TrackEntity) = libraryDelegate.cleanTrackTags(track)
    fun updateTrackMetadata(track: TrackEntity, title: String, artist: String, album: String, genre: String, year: String) =
        libraryDelegate.updateTrackMetadata(track, title, artist, album, genre, year)
    fun classifyTrackMoodAndGenre(track: TrackEntity) = libraryDelegate.classifyTrackMoodAndGenre(track)
    fun cleanAllLibraryTags() = libraryDelegate.cleanAllLibraryTags()
    fun scanDuplicates() = libraryDelegate.scanDuplicates()
    fun deleteDuplicateTrack(track: TrackEntity) = libraryDelegate.deleteDuplicateTrack(track)
    fun exportLibrary(destinationUri: Uri) = libraryDelegate.exportLibrary(getApplication(), destinationUri)

    fun setSleepTimer(option: SleepTimerOption, customMinutes: Int? = null) = playerManager.setSleepTimer(option, customMinutes)

    fun deleteTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            libraryDelegate.setShowCreatePlaylistDialog(false)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun getTracksForPlaylist(playlistId: Long): kotlinx.coroutines.flow.Flow<List<TrackEntity>> {
        return repository.getTracksForPlaylist(playlistId)
    }

    // Player Actions
    fun playTrack(track: TrackEntity, customQueue: List<TrackEntity>? = null) {
        val q = customQueue ?: allTracks.value
        val idx = q.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerManager.setQueueAndPlay(q, idx)
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun nextTrack() = playerManager.nextTrack()
    fun previousTrack() = playerManager.previousTrack()
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun toggleRepeat() = playerManager.toggleRepeat()
    fun setSpeed(speed: Float) = playerManager.setSpeed(speed)
    fun setPitch(pitch: Float) = playerManager.setPitch(pitch)
    fun clearPlaybackError() = playerManager.clearError()

    // Queue Actions
    fun moveQueueItem(fromIndex: Int, toIndex: Int) = playerManager.moveQueueItem(fromIndex, toIndex)
    fun removeQueueItem(index: Int) = playerManager.removeQueueItem(index)
    fun addTrackToNext(track: TrackEntity) = playerManager.addTrackToNext(track)
    fun addTrackToQueue(track: TrackEntity) = playerManager.addTrackToQueue(track)
    fun playQueueIndex(index: Int) = playerManager.playQueueIndex(index)

    // DSP & AI Actions
    fun setCrossfadeDuration(seconds: Float) = playerManager.setCrossfadeDuration(seconds)
    fun setVolumeNormalizerEnabled(enabled: Boolean) = playerManager.setVolumeNormalizerEnabled(enabled)
    fun setTargetLufs(lufs: Float) = playerManager.setTargetLufs(lufs)
    fun set3dAudioEnabled(enabled: Boolean) = playerManager.set3dAudioEnabled(enabled)
    fun set3dAudioStrength(strength: Float) = playerManager.set3dAudioStrength(strength)
    fun set3dAudioMode(mode: Audio3dSpeakerMode) = playerManager.set3dAudioMode(mode)
    fun setReverbEnvironment(environment: SpatialReverbEnvironment) = playerManager.setReverbEnvironment(environment)
    fun setReverbStrength(strength: Float) = playerManager.setReverbStrength(strength)
    fun setEqEnabled(enabled: Boolean) = playerManager.setEqEnabled(enabled)
    fun setBandGain(bandIndex: Int, gainDb: Float) = playerManager.setBandGain(bandIndex, gainDb)
    fun setEqPreset(preset: EqPreset) = playerManager.setEqPreset(preset)
    fun resetEq() = playerManager.resetEq()

    // Volume HUD Controller actions
    fun setVolumePercent(percent: Float) = volumeController.setVolumePercent(percent, showHud = true)
    fun adjustVolume(deltaStep: Int) = volumeController.adjustVolume(deltaStep)
    fun toggleMuteVolume() = volumeController.toggleMute()
    fun showVolumeHud() = volumeController.showHud()
    fun hideVolumeHud() = volumeController.hideHud()

    // Smart AI Feature Actions & Dialogs
    fun setShowSmartVibeDialog(show: Boolean) {
        _showSmartVibeDialog.value = show
    }

    fun setShowLyricsExplanationSheet(show: Boolean) {
        _showLyricsExplanationSheet.value = show
    }

    fun setVibeGenEnabled(enabled: Boolean) = aiSettingsManager.setVibePlaylistGeneratorEnabled(enabled)
    fun setLyricsTranslatorEnabled(enabled: Boolean) = aiSettingsManager.setLyricsTranslatorEnabled(enabled)
    fun setVibeMatchEnabled(enabled: Boolean) = aiSettingsManager.setVibeMatchEnabled(enabled)
    fun setCustomApiKey(apiKey: String) = aiSettingsManager.setCustomApiKey(apiKey)
    fun setSelectedModel(model: String) = aiSettingsManager.setSelectedModel(model)

    fun generateVibePlaylist(vibePrompt: String) {
        viewModelScope.launch {
            val tracks = allTracks.value
            val (playlistName, vibeTracks) = com.example.data.ai.SmartVibeEngine.generateVibePlaylist(vibePrompt, tracks)
            if (vibeTracks.isNotEmpty()) {
                val createdId = repository.createPlaylist(playlistName, "Lista Inteligente Vibe: $vibePrompt")
                vibeTracks.forEach { track ->
                    repository.addTrackToPlaylist(createdId, track.id)
                }
                libraryDelegate.setImportMessage("Lista '$playlistName' creada con ${vibeTracks.size} canciones.")
            } else {
                libraryDelegate.setImportMessage("No se encontraron canciones en tu biblioteca para el vibe: '$vibePrompt'.")
            }
        }
    }

    fun generateVibeMatchQueue(sourceTrack: TrackEntity) {
        viewModelScope.launch {
            val tracks = allTracks.value
            val vibeMatches = com.example.data.ai.SmartVibeEngine.findVibeMatches(sourceTrack, tracks)
            if (vibeMatches.isNotEmpty()) {
                playerManager.setQueueAndPlay(vibeMatches, 0)
                libraryDelegate.setImportMessage("⚡ Vibe-Match activado: ${vibeMatches.size} canciones de tono similar en la cola.")
            }
        }
    }

    fun translateCurrentLyrics(track: TrackEntity) {
        viewModelScope.launch {
            _explanationModalType.value = "translation"
            _showLyricsExplanationSheet.value = true
            _isTranslatingLyrics.value = true
            val lyrics = track.lyrics ?: "No hay letras registradas para traducir."
            val customKey = aiSettingsState.value.customApiKey
            val model = aiSettingsState.value.selectedModel
            val result = com.example.data.ai.GeminiLyricsService.translateLyrics(
                lyricsText = lyrics,
                customApiKey = customKey,
                selectedModel = model
            )
            _translatedLyrics.value = result
            _isTranslatingLyrics.value = false
        }
    }

    fun explainCurrentSongMeaning(track: TrackEntity) {
        viewModelScope.launch {
            _explanationModalType.value = "explanation"
            _showLyricsExplanationSheet.value = true
            _isExplainingSong.value = true
            val lyrics = track.lyrics ?: "Música instrumental o sin letra registrada."
            val customKey = aiSettingsState.value.customApiKey
            val model = aiSettingsState.value.selectedModel
            val result = com.example.data.ai.GeminiLyricsService.explainSongMeaning(
                title = track.title,
                artist = track.artist,
                lyricsText = lyrics,
                customApiKey = customKey,
                selectedModel = model
            )
            _songExplanation.value = result
            _isExplainingSong.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        volumeController.release()
    }
}
