package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.db.TrackEntity
import com.example.player.Audio3dSpeakerMode
import com.example.player.RepeatMode
import com.example.player.SleepTimerOption
import com.example.player.SpatialReverbEnvironment
import com.example.ui.components.dialogs.EditTrackMetadataDialog
import com.example.ui.components.player.EqPreset
import com.example.ui.components.player.PlayerAdvancedOptionsSheet
import com.example.ui.components.player.PlayerBackgroundLayer
import com.example.ui.components.player.PlayerScrollableContent
import com.example.ui.components.player.PlayerTopBar
import com.example.ui.components.player.QueueModalSheet
import com.example.ui.components.player.SleepTimerSheet

@Composable
fun PlayerFullScreen(
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    queue: List<TrackEntity> = emptyList(),
    currentIndex: Int = 0,
    playbackSpeed: Float = 1.0f,
    playbackPitch: Float = 1.0f,
    sleepTimerOption: SleepTimerOption = SleepTimerOption.OFF,
    sleepTimerRemainingMs: Long? = null,
    isEqEnabled: Boolean = true,
    bandGainsDb: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f),
    eqPreset: EqPreset = EqPreset.FLAT,
    is3dAudioEnabled: Boolean = true,
    audio3dStrength: Float = 0.8f,
    audio3dMode: Audio3dSpeakerMode = Audio3dSpeakerMode.DUAL_SPEAKER,
    reverbEnvironment: SpatialReverbEnvironment = SpatialReverbEnvironment.MEDIUM_HALL,
    reverbStrength: Float = 0.7f,
    crossfadeDurationSec: Float = 3.0f,
    isVolumeNormalizerEnabled: Boolean = true,
    targetLufs: Float = -14.0f,
    isLyricsTranslatorEnabled: Boolean = true,
    onTranslateLyricsClick: (TrackEntity) -> Unit = {},
    onExplainLyricsClick: (TrackEntity) -> Unit = {},
    isVibeMatchEnabled: Boolean = true,
    onVibeMatchClick: (TrackEntity) -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onPlayQueueIndex: (Int) -> Unit = {},
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onRemoveFromQueue: (Int) -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onPitchChange: (Float) -> Unit = {},
    onSetSleepTimer: (SleepTimerOption, Int?) -> Unit = { _, _ -> },
    onUpdateTrackMetadata: (TrackEntity, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onPickCustomCover: (TrackEntity) -> Unit = {},
    onEqEnabledToggle: (Boolean) -> Unit = {},
    onBandGainChange: (Int, Float) -> Unit = { _, _ -> },
    onPresetSelect: (EqPreset) -> Unit = {},
    onResetEq: () -> Unit = {},
    on3dAudioEnabledToggle: (Boolean) -> Unit = {},
    on3dStrengthChange: (Float) -> Unit = {},
    on3dModeSelect: (Audio3dSpeakerMode) -> Unit = {},
    onReverbEnvironmentSelect: (SpatialReverbEnvironment) -> Unit = {},
    onReverbStrengthChange: (Float) -> Unit = {},
    onCrossfadeDurationChange: (Float) -> Unit = {},
    onVolumeNormalizerToggle: (Boolean) -> Unit = {},
    onTargetLufsChange: (Float) -> Unit = {},
    onUpdateLyrics: (TrackEntity, String) -> Unit = { _, _ -> },
    onVolumeClick: () -> Unit = {},
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) return

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }
    var showAdvancedOptionsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showEditMetadataDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    PlayerBackgroundLayer(
        canvasVideoPath = currentTrack.canvasVideoPath,
        isPlaying = isPlaying,
        currentPositionMs = currentPositionMs,
        trackDurationMs = durationMs,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .testTag("full_screen_player")
        ) {
            // Barra Superior Fija
            PlayerTopBar(
                albumName = currentTrack.album,
                onCollapse = onCollapse,
                onVolumeClick = onVolumeClick,
                onAudioSettingsClick = { showAdvancedOptionsSheet = true }
            )

            // Contenido Desplazable del Reproductor
            PlayerScrollableContent(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                isUserScrubbing = isUserScrubbing,
                scrubPosition = scrubPosition,
                scrollState = scrollState,
                onScrubChange = {
                    isUserScrubbing = true
                    scrubPosition = it
                },
                onScrubFinished = { pos ->
                    isUserScrubbing = false
                    onSeekTo(pos)
                },
                onTogglePlayPause = onTogglePlayPause,
                onNextTrack = onNextTrack,
                onPreviousTrack = onPreviousTrack,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onToggleFavorite = onToggleFavorite,
                onUpdateLyrics = onUpdateLyrics,
                isLyricsTranslatorEnabled = isLyricsTranslatorEnabled,
                onTranslateLyricsClick = onTranslateLyricsClick,
                onExplainLyricsClick = onExplainLyricsClick,
                onQueueClick = { showQueueSheet = true },
                onAudioSettingsClick = { showAdvancedOptionsSheet = true }
            )
        }

        // Hoja Modal de Cola de Reproducción
        if (showQueueSheet) {
            QueueModalSheet(
                queue = queue,
                currentIndex = currentIndex,
                currentTrack = currentTrack,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onPlayQueueIndex = onPlayQueueIndex,
                onMoveQueueItem = onMoveQueueItem,
                onRemoveFromQueue = onRemoveFromQueue,
                onDismiss = { showQueueSheet = false }
            )
        }

        // Hoja Modal de Ajustes Avanzados
        if (showAdvancedOptionsSheet) {
            PlayerAdvancedOptionsSheet(
                track = currentTrack,
                playbackSpeed = playbackSpeed,
                playbackPitch = playbackPitch,
                sleepTimerOption = sleepTimerOption,
                sleepTimerRemainingMs = sleepTimerRemainingMs,
                isEqEnabled = isEqEnabled,
                bandGainsDb = bandGainsDb,
                eqPreset = eqPreset,
                is3dAudioEnabled = is3dAudioEnabled,
                audio3dStrength = audio3dStrength,
                audio3dMode = audio3dMode,
                reverbEnvironment = reverbEnvironment,
                reverbStrength = reverbStrength,
                crossfadeDurationSec = crossfadeDurationSec,
                isVolumeNormalizerEnabled = isVolumeNormalizerEnabled,
                targetLufs = targetLufs,
                isVibeMatchEnabled = isVibeMatchEnabled,
                onVibeMatchClick = onVibeMatchClick,
                onSpeedChange = onSpeedChange,
                onPitchChange = onPitchChange,
                onOpenSleepTimer = {
                    showAdvancedOptionsSheet = false
                    showSleepTimerSheet = true
                },
                onOpenEditMetadata = {
                    showAdvancedOptionsSheet = false
                    showEditMetadataDialog = true
                },
                onEqEnabledToggle = onEqEnabledToggle,
                onBandGainChange = onBandGainChange,
                onPresetSelect = onPresetSelect,
                onResetEq = onResetEq,
                on3dAudioEnabledToggle = on3dAudioEnabledToggle,
                on3dStrengthChange = on3dStrengthChange,
                on3dModeSelect = on3dModeSelect,
                onReverbEnvironmentSelect = onReverbEnvironmentSelect,
                onReverbStrengthChange = onReverbStrengthChange,
                onCrossfadeDurationChange = onCrossfadeDurationChange,
                onVolumeNormalizerToggle = onVolumeNormalizerToggle,
                onTargetLufsChange = onTargetLufsChange,
                onDismiss = { showAdvancedOptionsSheet = false }
            )
        }

        // Hoja Modal de Temporizador de Sueño
        if (showSleepTimerSheet) {
            SleepTimerSheet(
                currentOption = sleepTimerOption,
                remainingMs = sleepTimerRemainingMs,
                onSelectOption = { option, customMinutes ->
                    onSetSleepTimer(option, customMinutes)
                },
                onDismiss = { showSleepTimerSheet = false }
            )
        }

        // Modal de Editor de Etiquetas ID3 & Portada
        if (showEditMetadataDialog) {
            EditTrackMetadataDialog(
                track = currentTrack,
                onSave = { track, title, artist, album, genre, year ->
                    onUpdateTrackMetadata(track, title, artist, album, genre, year)
                },
                onPickCoverArt = { track ->
                    onPickCustomCover(track)
                },
                onDismiss = { showEditMetadataDialog = false }
            )
        }
    }
}
