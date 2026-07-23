package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SpotLocalMainScaffold
import com.example.ui.theme.SpotLocalTheme
import com.example.ui.viewmodel.PlayerViewModel
import com.example.util.DebugLogger
import com.example.util.MainActivityFilePickerHelper

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var pickerHelper: MainActivityFilePickerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pickerHelper = MainActivityFilePickerHelper(this, viewModel)

        // Inicializar interceptor de logs y capturador de crashes para la APK Debug
        DebugLogger.init(applicationContext)
        DebugLogger.logAction("MainActivity", "Activity creada exitosamente")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                pickerHelper.requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        handleIncomingAudioIntent(intent)

        setContent {
            SpotLocalTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsStateWithLifecycle()

                val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
                val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
                val recentTracks by viewModel.recentTracks.collectAsStateWithLifecycle()
                val playlists by viewModel.playlists.collectAsStateWithLifecycle()
                val folders by viewModel.folders.collectAsStateWithLifecycle()

                val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
                val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()
                val currentPositionMs by viewModel.playerManager.currentPositionMs.collectAsStateWithLifecycle()
                val durationMs by viewModel.playerManager.durationMs.collectAsStateWithLifecycle()
                val isShuffle by viewModel.playerManager.isShuffle.collectAsStateWithLifecycle()
                val repeatMode by viewModel.playerManager.repeatMode.collectAsStateWithLifecycle()
                val playbackSpeed by viewModel.playerManager.playbackSpeed.collectAsStateWithLifecycle()
                val playbackPitch by viewModel.playerManager.playbackPitch.collectAsStateWithLifecycle()
                val playbackError by viewModel.playerManager.playbackError.collectAsStateWithLifecycle()

                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val selectedFilter by viewModel.selectedFilterChip.collectAsStateWithLifecycle()

                val showCreatePlaylistDialog by viewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
                val trackOptionsTarget by viewModel.showTrackOptionsDialog.collectAsStateWithLifecycle()
                val importMessage by viewModel.importMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(playbackError) {
                    playbackError?.let { err ->
                        Toast.makeText(this@MainActivity, err, Toast.LENGTH_LONG).show()
                        viewModel.playerManager.clearError()
                    }
                }

                LaunchedEffect(importMessage) {
                    importMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearImportMessage()
                    }
                }

                SpotLocalMainScaffold(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    currentTab = currentTab,
                    isPlayerExpanded = isPlayerExpanded,
                    allTracks = allTracks,
                    favoriteTracks = favoriteTracks,
                    recentTracks = recentTracks,
                    playlists = playlists,
                    folders = folders,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    playbackSpeed = playbackSpeed,
                    playbackPitch = playbackPitch,
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    selectedFilter = selectedFilter,
                    showCreatePlaylistDialog = showCreatePlaylistDialog,
                    trackOptionsTarget = trackOptionsTarget,
                    onLaunchImportPicker = { pickerHelper.launchImportPicker() },
                    onLaunchVideoImportPicker = { pickerHelper.launchVideoToMusicPicker() },
                    onLaunchExportPicker = { pickerHelper.launchExportPicker() },
                    onPickCustomCover = { track -> pickerHelper.launchCustomCoverPicker(track) },
                    onPickCanvasVideo = { track -> pickerHelper.launchCanvasVideoPicker(track) }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAudioIntent(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                viewModel.adjustVolume(1)
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                viewModel.adjustVolume(-1)
                true
            }
            KeyEvent.KEYCODE_VOLUME_MUTE -> {
                viewModel.toggleMuteVolume()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun handleIncomingAudioIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return

        val urisToImport = mutableListOf<android.net.Uri>()

        if (action == Intent.ACTION_VIEW) {
            intent.data?.let { urisToImport.add(it) }
        } else if (action == Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                ?: intent.data
            uri?.let { urisToImport.add(it) }
        } else if (action == Intent.ACTION_SEND_MULTIPLE) {
            val uris = intent.getParcelableArrayListExtra<android.net.Uri>(Intent.EXTRA_STREAM)
            if (!uris.isNullOrEmpty()) {
                urisToImport.addAll(uris)
            }
        }

        if (urisToImport.isNotEmpty()) {
            DebugLogger.logAction("OpenWith", "Abrir con SpotLocal: ${urisToImport.size} archivo(s)")
            viewModel.importUrisAndPlay(urisToImport)
        }
    }
}
