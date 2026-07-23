package com.example.player

import com.example.player.controllers.EqualizerFxController
import com.example.player.controllers.ReverbFxController
import com.example.player.controllers.Spatial3dAudioFxController

/**
 * Gestor modular de efectos de audio de Android (AudioFX).
 * Coordina los sub-controladores [EqualizerFxController], [Spatial3dAudioFxController] y [ReverbFxController].
 */
class AudioFxManager {

    private val eqController = EqualizerFxController()
    private val spatialController = Spatial3dAudioFxController()
    private val reverbController = ReverbFxController()

    fun attachEqualizer(
        audioSessionId: Int,
        is3dAudioEnabled: Boolean,
        audio3dStrength: Short,
        currentReverbEnvironment: SpatialReverbEnvironment,
        reverbStrength: Float
    ) {
        releaseEqualizer()
        if (audioSessionId != 0) {
            eqController.attach(audioSessionId)
            spatialController.attach(audioSessionId, is3dAudioEnabled, audio3dStrength)
            reverbController.attach(audioSessionId, currentReverbEnvironment)

            apply3dAudioFx(is3dAudioEnabled, audio3dStrength, Audio3dSpeakerMode.DUAL_SPEAKER)
            applyReverbEnvironment(currentReverbEnvironment, reverbStrength)
        }
    }

    fun releaseEqualizer() {
        eqController.release()
        spatialController.release()
        reverbController.release()
    }

    fun applyReverbEnvironment(
        environment: SpatialReverbEnvironment,
        strength: Float
    ) {
        reverbController.applyReverbEnvironment(environment, strength)
    }

    fun apply3dAudioFx(
        enabled: Boolean,
        strength: Short,
        mode: Audio3dSpeakerMode
    ) {
        spatialController.apply3dAudioFx(enabled, strength, mode)
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray) {
        eqController.applyCustomEqBands(isEqEnabled, bandGainsDb)
    }
}
