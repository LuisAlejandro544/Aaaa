package com.example.ui.components.player

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.DebugLogger
import java.io.File
import kotlin.math.abs

@Composable
fun PlayerCanvasVideoView(
    videoPath: String,
    isPlaying: Boolean,
    currentPositionMs: Long,
    trackDurationMs: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mediaPlayerState by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPrepared by remember { mutableStateOf(false) }

    val videoFile = remember(videoPath) { File(videoPath) }

    if (!videoFile.exists()) {
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            try {
                                val surface = Surface(surfaceTexture)
                                val mp = MediaPlayer().apply {
                                    setDataSource(context, Uri.fromFile(videoFile))
                                    setSurface(surface)
                                    setVolume(0f, 0f) // Mute audio for Canvas loop
                                    isLooping = true
                                    setOnPreparedListener { preparedMp ->
                                        isPrepared = true
                                        preparedMp.isLooping = true
                                        val vDur = preparedMp.duration.toLong().coerceAtLeast(1000L)
                                        val targetOffset = (currentPositionMs % vDur).toInt()
                                        preparedMp.seekTo(targetOffset)
                                        if (isPlaying) {
                                            preparedMp.start()
                                        }
                                        DebugLogger.logAction("CanvasVideo", "Canvas sincronizado en ${targetOffset}ms (Duración Video: ${vDur}ms)")
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        DebugLogger.logWarning("CanvasVideo", "Error en MediaPlayer Canvas: $what, $extra")
                                        true
                                    }
                                    prepareAsync()
                                }
                                mediaPlayerState = mp
                            } catch (e: Exception) {
                                DebugLogger.logWarning("CanvasVideo", "Excepción al inicializar Canvas: ${e.message}")
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {}

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            mediaPlayerState?.let { mp ->
                                try {
                                    if (mp.isPlaying) mp.stop()
                                    mp.release()
                                } catch (e: Exception) {
                                    // Ignore release errors
                                }
                            }
                            mediaPlayerState = null
                            isPrepared = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Synchronization logic with music position
        LaunchedEffect(isPlaying, currentPositionMs, isPrepared) {
            val mp = mediaPlayerState
            if (mp != null && isPrepared) {
                try {
                    val vDur = mp.duration.toLong().coerceAtLeast(1000L)
                    val targetOffset = (currentPositionMs % vDur).toInt()
                    val currentVideoPos = mp.currentPosition

                    // Sync if current position diverged by more than 1.5 seconds (e.g. user seeked track)
                    if (abs(currentVideoPos - targetOffset) > 1500) {
                        mp.seekTo(targetOffset)
                    }

                    if (isPlaying && !mp.isPlaying) {
                        mp.start()
                    } else if (!isPlaying && mp.isPlaying) {
                        mp.pause()
                    }
                } catch (e: Exception) {
                    // Handle transient playback state changes gracefully
                }
            }
        }

        DisposableEffect(videoPath) {
            onDispose {
                mediaPlayerState?.let { mp ->
                    try {
                        if (mp.isPlaying) mp.stop()
                        mp.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                mediaPlayerState = null
                isPrepared = false
            }
        }

        // Dark gradient overlay to keep text/controls crisp and readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )
    }
}
