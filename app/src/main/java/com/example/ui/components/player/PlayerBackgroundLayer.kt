package com.example.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyDarkSlate

/**
 * Capa de fondo modular para el reproductor a pantalla completa.
 * Renderiza el video Canvas en segundo plano o un gradiente oscuro de Spotify.
 */
@Composable
fun PlayerBackgroundLayer(
    canvasVideoPath: String?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    trackDurationMs: Long,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundBrush = remember(canvasVideoPath) {
        if (canvasVideoPath != null) {
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(SpotifyDarkSlate, SpotifyBlack, SpotifyBlack))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Video Canvas si está disponible
        canvasVideoPath?.let { canvasPath ->
            PlayerCanvasVideoView(
                videoPath = canvasPath,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                trackDurationMs = trackDurationMs,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            content()
        }
    }
}
