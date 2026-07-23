package com.example.player

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.util.Log
import com.example.data.ai.StemMode

class AudioDspEngine {

    companion object {
        private const val TAG = "AudioDspEngine"

        init {
            try {
                System.loadLibrary("native-audio")
                Log.i(TAG, "Native C++ Oboe Audio Engine loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library native-audio not loaded, falling back to Android MediaPlayer DSP")
            }
        }
    }

    private val audioFxManager = AudioFxManager()

    private var currentSpeed: Float = 1.0f
    private var currentPitch: Float = 1.0f

    private var currentStemMode: StemMode = StemMode.ORIGINAL
    private var is3dAudioEnabled: Boolean = false
    private var audio3dStrength: Short = 800 // 0 to 1000
    private var audio3dSpeakerMode: Audio3dSpeakerMode = Audio3dSpeakerMode.DUAL_SPEAKER

    private var currentReverbEnvironment: SpatialReverbEnvironment = SpatialReverbEnvironment.OFF
    private var reverbStrength: Float = 0.7f

    fun attachEqualizer(audioSessionId: Int) {
        audioFxManager.attachEqualizer(
            audioSessionId = audioSessionId,
            currentStemMode = currentStemMode,
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

    fun applyStemMode(mode: StemMode) {
        currentStemMode = mode
        audioFxManager.applyStemMode(mode)
    }

    fun applyDspParams(mediaPlayer: MediaPlayer?, speed: Float, pitch: Float) {
        currentSpeed = speed.coerceIn(0.25f, 2.0f)
        currentPitch = pitch.coerceIn(0.25f, 2.0f)

        mediaPlayer?.let { player ->
            try {
                val params: PlaybackParams = player.playbackParams ?: PlaybackParams()
                params.speed = currentSpeed
                params.pitch = currentPitch
                player.playbackParams = params
            } catch (e: Exception) {
                Log.e(TAG, "Error applying PlaybackParams: ${e.message}")
            }
        }
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray) {
        audioFxManager.applyCustomEqBands(isEqEnabled, bandGainsDb, currentStemMode)
    }
}
