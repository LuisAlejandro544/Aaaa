package com.example.player.controllers

import android.media.audiofx.PresetReverb
import android.util.Log
import com.example.player.SpatialReverbEnvironment

/**
 * Controlador modular para simulación espacial de Reverberación 3D HRTF.
 */
class ReverbFxController {

    companion object {
        private const val TAG = "ReverbFxController"
    }

    private var presetReverb: PresetReverb? = null

    fun attach(audioSessionId: Int, currentReverbEnvironment: SpatialReverbEnvironment) {
        release()
        if (audioSessionId == 0) return

        try {
            presetReverb = PresetReverb(0, audioSessionId).apply {
                enabled = currentReverbEnvironment != SpatialReverbEnvironment.OFF
                if (currentReverbEnvironment != SpatialReverbEnvironment.OFF) {
                    preset = currentReverbEnvironment.presetValue
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error init PresetReverb: ${e.message}")
        }
    }

    fun release() {
        try {
            presetReverb?.enabled = false
            presetReverb?.release()
        } catch (e: Exception) {
            // ignorar
        }
        presetReverb = null
    }

    fun applyReverbEnvironment(environment: SpatialReverbEnvironment, strength: Float) {
        val safeStrength = strength.coerceIn(0f, 1f)
        try {
            presetReverb?.let { reverb ->
                if (environment == SpatialReverbEnvironment.OFF || safeStrength <= 0.05f) {
                    reverb.enabled = false
                } else {
                    reverb.enabled = true
                    reverb.preset = environment.presetValue
                }
            }
            Log.i(TAG, "Aplicado Reverb 3D HRTF: env=$environment, strength=$safeStrength")
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando PresetReverb: ${e.message}")
        }
    }
}
