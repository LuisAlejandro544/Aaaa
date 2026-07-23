package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.StemMode
import com.example.data.ai.StemSeparatorEngine
import com.example.data.db.TrackEntity
import com.example.player.Audio3dSpeakerMode
import com.example.player.RepeatMode
import com.example.player.SpatialReverbEnvironment
import com.example.ui.components.player.EqPreset
import com.example.ui.components.player.PlayerAdvancedOptionsSheet
import com.example.ui.components.player.PlayerCanvasVideoView
import com.example.ui.components.player.PlayerScrollableContent
import com.example.ui.components.player.PlayerTopBar
import com.example.ui.components.player.QueueModalSheet
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyDarkSlate

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
    onStemModeSelected: (StemMode) -> Unit = {},
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

    val stemState by StemSeparatorEngine.separationState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val backgroundBrush = remember(currentTrack.canvasVideoPath) {
        if (currentTrack.canvasVideoPath != null) {
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(SpotifyDarkSlate, SpotifyBlack, SpotifyBlack))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fullscreen Canvas Background Video if assigned
        currentTrack.canvasVideoPath?.let { canvasPath ->
            PlayerCanvasVideoView(
                videoPath = canvasPath,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                trackDurationMs = durationMs,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .testTag("full_screen_player")
        ) {
            // Fixed Top Bar
            PlayerTopBar(
                albumName = currentTrack.album,
                onCollapse = onCollapse,
                onVolumeClick = onVolumeClick,
                onAudioSettingsClick = { showAdvancedOptionsSheet = true }
            )

            // Scrollable Spotify Player View
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
                onQueueClick = { showQueueSheet = true },
                onAudioSettingsClick = { showAdvancedOptionsSheet = true }
            )
        }

        // Queue Modal Sheet
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

        // Advanced Options Bottom Sheet
        if (showAdvancedOptionsSheet) {
            PlayerAdvancedOptionsSheet(
                track = currentTrack,
                playbackSpeed = playbackSpeed,
                playbackPitch = playbackPitch,
                stemState = stemState,
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
                onSpeedChange = onSpeedChange,
                onPitchChange = onPitchChange,
                onStemModeSelected = onStemModeSelected,
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
    }
}
