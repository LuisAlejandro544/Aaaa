package com.example.ui.components.player

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.db.TrackEntity
import com.example.player.RepeatMode

/**
 * Contenido desplazable modular para el reproductor a pantalla completa.
 * Organiza estructuralmente las secciones de carátula [PlayerHeaderArtSection],
 * título [PlayerTrackHeader], barra de progreso [PlayerSeekBar],
 * controles [PlayerPlaybackControls], insignia [PlayerFooterBadge] y letras [PlayerLyricsSection].
 */
@Composable
fun PlayerScrollableContent(
    currentTrack: TrackEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isUserScrubbing: Boolean,
    scrubPosition: Float,
    scrollState: ScrollState,
    onScrubChange: (Float) -> Unit,
    onScrubFinished: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onUpdateLyrics: (TrackEntity, String) -> Unit,
    isLyricsTranslatorEnabled: Boolean = true,
    onTranslateLyricsClick: (TrackEntity) -> Unit = {},
    onExplainLyricsClick: (TrackEntity) -> Unit = {},
    onQueueClick: () -> Unit = {},
    onAudioSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Sección modular de carátula / canvas
        PlayerHeaderArtSection(
            coverArtPath = currentTrack.coverArtPath,
            hasCanvasVideo = currentTrack.canvasVideoPath != null
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título de la pista y artista
        PlayerTrackHeader(
            track = currentTrack,
            onToggleFavorite = onToggleFavorite
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de progreso
        PlayerSeekBar(
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isUserScrubbing = isUserScrubbing,
            scrubPosition = scrubPosition,
            onScrubChange = onScrubChange,
            onScrubFinished = onScrubFinished
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Controles de reproducción
        PlayerPlaybackControls(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            onTogglePlayPause = onTogglePlayPause,
            onNextTrack = onNextTrack,
            onPreviousTrack = onPreviousTrack,
            onToggleShuffle = onToggleShuffle,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Insignia de pie de página
        PlayerFooterBadge(
            isSampleTrack = currentTrack.isSample,
            onQueueClick = onQueueClick,
            onAudioSettingsClick = onAudioSettingsClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección modular de letras estilo Spotify
        PlayerLyricsSection(
            track = currentTrack,
            currentPositionMs = currentPositionMs,
            onUpdateLyrics = { newLyrics -> onUpdateLyrics(currentTrack, newLyrics) },
            isLyricsTranslatorEnabled = isLyricsTranslatorEnabled,
            onTranslateLyricsClick = onTranslateLyricsClick,
            onExplainLyricsClick = onExplainLyricsClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
