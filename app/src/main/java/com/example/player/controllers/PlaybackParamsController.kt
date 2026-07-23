package com.example.player.controllers

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.util.Log

/**
 * Controlador modular para la gestión segura de parámetros de reproducción (Velocidad y Pitch)
 * sobre el MediaPlayer de Android.
 */
class PlaybackParamsController {

    companion object {
        private const val TAG = "PlaybackParamsController"
    }

    var currentSpeed: Float = 1.0f
        private set

    var currentPitch: Float = 1.0f
        private set

    fun applyParams(mediaPlayer: MediaPlayer?, speed: Float, pitch: Float) {
        currentSpeed = speed.coerceIn(0.25f, 2.0f)
        currentPitch = pitch.coerceIn(0.25f, 2.0f)

        mediaPlayer?.let { player ->
            try {
                val params: PlaybackParams = player.playbackParams ?: PlaybackParams()
                params.speed = currentSpeed
                params.pitch = currentPitch
                player.playbackParams = params
            } catch (e: Exception) {
                Log.e(TAG, "Error aplicando PlaybackParams (speed=$currentSpeed, pitch=$currentPitch): ${e.message}")
            }
        }
    }
}
