package com.example.ui.viewmodel.delegates

import android.app.Application
import android.net.Uri
import com.example.data.db.TrackEntity
import com.example.data.repository.MusicRepository
import com.example.util.AudioFingerprintEngine
import com.example.util.DuplicateCluster
import com.example.util.Id3TagCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LibraryDelegate(
    private val repository: MusicRepository,
    private val scope: CoroutineScope
) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterChip = MutableStateFlow("Todo")
    val selectedFilterChip: StateFlow<String> = _selectedFilterChip.asStateFlow()

    private val _showImportInfoDialog = MutableStateFlow(false)
    val showImportInfoDialog: StateFlow<Boolean> = _showImportInfoDialog.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showTrackOptionsDialog = MutableStateFlow<TrackEntity?>(null)
    val showTrackOptionsDialog: StateFlow<TrackEntity?> = _showTrackOptionsDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _importMessage = MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage.asStateFlow()

    private val _duplicateClusters = MutableStateFlow<List<DuplicateCluster>>(emptyList())
    val duplicateClusters: StateFlow<List<DuplicateCluster>> = _duplicateClusters.asStateFlow()

    private val _isScanningDuplicates = MutableStateFlow(false)
    val isScanningDuplicates: StateFlow<Boolean> = _isScanningDuplicates.asStateFlow()

    private val _isImportingVideo = MutableStateFlow(false)
    val isImportingVideo: StateFlow<Boolean> = _isImportingVideo.asStateFlow()

    private val _importVideoProgressText = MutableStateFlow("Procesando video de alta calidad...")
    val importVideoProgressText: StateFlow<String> = _importVideoProgressText.asStateFlow()

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterChip(chip: String) { _selectedFilterChip.value = chip }
    fun setShowImportInfoDialog(show: Boolean) { _showImportInfoDialog.value = show }
    fun setShowCreatePlaylistDialog(show: Boolean) { _showCreatePlaylistDialog.value = show }
    fun setShowTrackOptionsDialog(track: TrackEntity?) { _showTrackOptionsDialog.value = track }
    fun setShowExportDialog(show: Boolean) { _showExportDialog.value = show }
    fun clearImportMessage() { _importMessage.value = null }
    fun setImportingVideoState(isImporting: Boolean, text: String = "Procesando video...") {
        _isImportingVideo.value = isImporting
        _importVideoProgressText.value = text
    }

    fun importUris(app: Application, uris: List<Uri>) {
        scope.launch {
            _importMessage.value = "Importando ${uris.size} archivo(s)..."
            val count = repository.importUris(app, uris)
            _importMessage.value = if (count > 0) {
                "Se importaron $count canción(es) correctamente a tu biblioteca local."
            } else {
                "No se encontraron archivos de audio válidos."
            }
        }
    }

    fun updateCustomCoverArt(app: Application, track: TrackEntity, imageUri: Uri) {
        scope.launch {
            _importMessage.value = "Procesando carátula WebP en segundo plano..."
            val success = repository.updateCustomCoverArt(app, track, imageUri)
            _importMessage.value = if (success) {
                "Carátula WebP guardada correctamente sin congelar la app."
            } else {
                "No se pudo procesar la carátula elegida."
            }
        }
    }

    fun updateCanvasVideo(app: Application, track: TrackEntity, videoUri: Uri) {
        scope.launch {
            _importMessage.value = "Guardando video Canvas de fondo..."
            val success = repository.updateCanvasVideo(app, track, videoUri)
            _importMessage.value = if (success) {
                "¡Canvas Video asignado exitosamente al reproductor!"
            } else {
                "No se pudo procesar el archivo de video."
            }
        }
    }

    fun removeCanvasVideo(app: Application, track: TrackEntity) {
        scope.launch {
            repository.removeCanvasVideo(app, track)
            _importMessage.value = "Canvas Video eliminado de esta canción."
        }
    }

    fun updateLyrics(app: Application, track: TrackEntity, newLyrics: String) {
        scope.launch {
            repository.updateLyrics(app, track, newLyrics)
            _importMessage.value = "Letras actualizadas correctamente."
        }
    }

    fun cleanTrackTags(track: TrackEntity) {
        scope.launch {
            val result = Id3TagCleaner.cleanTrack(track)
            if (result.hasChanges) {
                repository.updateTrack(result.cleanedTrack)
                _importMessage.value = "Etiquetas ID3 corregidas para '${result.cleanedTrack.title}'."
            } else {
                _importMessage.value = "Las etiquetas ya están limpias."
            }
        }
    }

    fun classifyTrackMoodAndGenre(track: TrackEntity) {
        scope.launch {
            val classification = com.example.data.ai.AudioMoodGenreClassifier.classify(track)
            val updatedTrack = track.copy(
                mood = classification.mood,
                genre = classification.genre
            )
            repository.updateTrack(updatedTrack)
            _importMessage.value = "IA clasificó: ${classification.mood} | ${classification.genre}"
        }
    }

    fun cleanAllLibraryTags() {
        scope.launch {
            val allTracks = repository.allTracks.first()
            var count = 0
            allTracks.forEach { track ->
                val result = Id3TagCleaner.cleanTrack(track)
                if (result.hasChanges) {
                    repository.updateTrack(result.cleanedTrack)
                    count++
                }
            }
            _importMessage.value = "Se limpiaron y corrigieron etiquetas de $count canciones."
        }
    }

    fun scanDuplicates() {
        scope.launch {
            _isScanningDuplicates.value = true
            val allTracks = repository.allTracks.first()
            val clusters = AudioFingerprintEngine.findDuplicates(allTracks)
            _duplicateClusters.value = clusters
            _isScanningDuplicates.value = false
            if (clusters.isEmpty()) {
                _importMessage.value = "¡No se encontraron canciones duplicadas en tu biblioteca!"
            }
        }
    }

    fun deleteDuplicateTrack(track: TrackEntity) {
        scope.launch {
            repository.deleteTrack(track)
            scanDuplicates()
            _importMessage.value = "Canción duplicada eliminada con éxito."
        }
    }

    fun exportLibrary(app: Application, destinationUri: Uri) {
        scope.launch {
            val success = repository.exportLibraryToJson(app, destinationUri)
            _importMessage.value = if (success) {
                "Copia de seguridad exportada con éxito."
            } else {
                "Error al exportar la copia de seguridad."
            }
            _showExportDialog.value = false
        }
    }
}
