package com.example.util

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.db.TrackEntity
import com.example.ui.viewmodel.PlayerViewModel

class MainActivityFilePickerHelper(
    private val activity: ComponentActivity,
    private val viewModel: PlayerViewModel
) {

    private var pendingTargetTrack: TrackEntity? = null

    val openAudioFilesLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (!uris.isNullOrEmpty()) {
                DebugLogger.logAction("Import", "Usuario seleccionó ${uris.size} archivos para importar")
                viewModel.importUris(uris)
            } else {
                DebugLogger.logWarning("Import", "El usuario canceló la selección de archivos")
            }
        }

    val pickCustomCoverLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && pendingTargetTrack != null) {
                DebugLogger.logAction("CoverArt", "Seleccionada imagen para carátula WebP para track ID=${pendingTargetTrack?.id}")
                viewModel.updateCustomCoverArt(pendingTargetTrack!!, uri)
            } else {
                DebugLogger.logWarning("CoverArt", "Cancelada selección de imagen de carátula")
            }
        }

    val pickVideoToMusicLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                DebugLogger.logAction("VideoToMusic", "Importando video como música + canvas de fondo")
                viewModel.importVideoAsTrackAndCanvas(uri)
            } else {
                DebugLogger.logWarning("VideoToMusic", "Cancelada importación de video")
            }
        }

    val pickCanvasVideoLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && pendingTargetTrack != null) {
                DebugLogger.logAction("CanvasVideo", "Seleccionado video para Canvas en track ID=${pendingTargetTrack?.id}")
                viewModel.updateCanvasVideo(pendingTargetTrack!!, uri)
            } else {
                DebugLogger.logWarning("CanvasVideo", "Cancelada selección de video Canvas")
            }
        }

    val exportBackupLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                DebugLogger.logAction("Export", "Exportando respaldo a URI: $uri")
                viewModel.exportLibrary(uri)
            } else {
                DebugLogger.logWarning("Export", "El usuario canceló la exportación")
            }
        }

    val requestNotificationPermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                DebugLogger.logAction("Permission", "Permiso de notificaciones concedido")
            } else {
                DebugLogger.logWarning("Permission", "Permiso de notificaciones denegado")
            }
        }

    fun launchImportPicker() {
        try {
            openAudioFilesLauncher.launch(
                arrayOf("audio/*", "application/ogg", "application/x-flac")
            )
        } catch (e: Exception) {
            Toast.makeText(activity, "No se pudo abrir el explorador de archivos.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchVideoToMusicPicker() {
        try {
            pickVideoToMusicLauncher.launch("video/*")
        } catch (e: Exception) {
            Toast.makeText(activity, "No se pudo abrir el selector de video.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchExportPicker() {
        try {
            exportBackupLauncher.launch("SpotLocal_Biblioteca_Backup.json")
        } catch (e: Exception) {
            Toast.makeText(activity, "Error al iniciar la exportación.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCustomCoverPicker(track: TrackEntity) {
        try {
            pendingTargetTrack = track
            pickCustomCoverLauncher.launch("image/*")
        } catch (e: Exception) {
            Toast.makeText(activity, "No se pudo abrir el selector de imágenes.", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCanvasVideoPicker(track: TrackEntity) {
        try {
            pendingTargetTrack = track
            pickCanvasVideoLauncher.launch("video/*")
        } catch (e: Exception) {
            Toast.makeText(activity, "No se pudo abrir el selector de video.", Toast.LENGTH_SHORT).show()
        }
    }
}
