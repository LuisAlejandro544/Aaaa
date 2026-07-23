package com.example.player

import android.media.MediaPlayer
import android.util.Log
import com.example.player.controllers.PlaybackParamsController

/**
 * Motor DSP de Audio principal.
 * Fachada modular que integra la librería C++ nativa Oboe NDK,
 * delega los efectos de audio a [AudioFxManager] y los parámetros de reproducción a [PlaybackParamsController].
 */
class AudioDspEngine {

    companion object {
        private const val TAG = "AudioDspEngine"

        init {
            try {
                System.loadLibrary("native-audio")
                Log.i(TAG, "Librería nativa C++ Oboe Audio Engine cargada exitosamente")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Librería nativa native-audio no disponible, usando fallback DSP Android MediaPlayer")
            }
        }
    }

    private val audioFxManager = AudioFxManager()
    private val playbackParamsController = PlaybackParamsController()

    private var is3dAudioEnabled: Boolean = false
    private var audio3dStrength: Short = 800 // 0 a 1000
    private var audio3dSpeakerMode: Audio3dSpeakerMode = Audio3dSpeakerMode.DUAL_SPEAKER

    private var currentReverbEnvironment: SpatialReverbEnvironment = SpatialReverbEnvironment.OFF
    private var reverbStrength: Float = 0.7f

    fun attachEqualizer(audioSessionId: Int) {
        audioFxManager.attachEqualizer(
            audioSessionId = audioSessionId,
            is3dAudioEnabled = is3dAudioEnabled,
            audio3dStrength = audio3dStrength,
            currentReverbEnvironment = currentReverbEnvironment,
            reverbStrength = reverbStrength
        )
    }

    fun releaseEqualizer() {
        audioFxManager.releaseEqualizer()
    }

    fun applyReverbEnvironment(
        environment: SpatialReverbEnvironment = currentReverbEnvironment,
        strength: Float = reverbStrength
    ) {
        currentReverbEnvironment = environment
        reverbStrength = strength.coerceIn(0f, 1f)
        audioFxManager.applyReverbEnvironment(environment, strength)
    }

    fun apply3dAudioFx(
        enabled: Boolean = is3dAudioEnabled,
        strength: Short = audio3dStrength,
        mode: Audio3dSpeakerMode = audio3dSpeakerMode
    ) {
        is3dAudioEnabled = enabled
        audio3dStrength = strength
        audio3dSpeakerMode = mode
        audioFxManager.apply3dAudioFx(enabled, strength, mode)
    }

    fun applyDspParams(mediaPlayer: MediaPlayer?, speed: Float, pitch: Float) {
        playbackParamsController.applyParams(mediaPlayer, speed, pitch)
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray) {
        audioFxManager.applyCustomEqBands(isEqEnabled, bandGainsDb)
    }
}
