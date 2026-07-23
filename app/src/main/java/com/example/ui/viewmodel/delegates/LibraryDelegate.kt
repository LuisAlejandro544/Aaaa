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
    fun setImportMessage(msg: String) { _importMessage.value = msg }
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

    fun updateTrackMetadata(track: TrackEntity, title: String, artist: String, album: String, genre: String, mood: String) {
        scope.launch {
            val updatedTrack = track.copy(
                title = if (title.isBlank()) track.title else title,
                artist = if (artist.isBlank()) track.artist else artist,
                album = if (album.isBlank()) track.album else album,
                genre = if (genre.isBlank()) null else genre,
                mood = if (mood.isBlank()) null else mood
            )
            repository.updateTrack(updatedTrack)
            _importMessage.value = "Etiquetas ID3 actualizadas para '${updatedTrack.title}'."
        }
    }

    fun classifyTrackMoodAndGenre(track: TrackEntity) {
        scope.launch {
            val titleLower = track.title.lowercase()
            val detectedGenre = when {
                titleLower.contains("rock") -> "Rock"
                titleLower.contains("pop") -> "Pop"
                titleLower.contains("jazz") -> "Jazz"
                titleLower.contains("lofi") || titleLower.contains("chill") -> "Lo-Fi / Ambient"
                titleLower.contains("electronic") || titleLower.contains("dance") -> "Electronic"
                else -> track.genre ?: "Desconocido"
            }
            val detectedMood = when {
                titleLower.contains("chill") || titleLower.contains("sleep") || titleLower.contains("relax") -> "Relajado"
                titleLower.contains("happy") || titleLower.contains("dance") || titleLower.contains("party") -> "Enérgico"
                else -> track.mood ?: "Neutral"
            }
            val updatedTrack = track.copy(
                mood = detectedMood,
                genre = detectedGenre
            )
            repository.updateTrack(updatedTrack)
            _importMessage.value = "Clasificación detectada: $detectedMood | $detectedGenre"
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
