package com.example.player.controllers

import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.player.Audio3dSpeakerMode

/**
 * Controlador modular para efectos 3D de Virtualizer y BassBoost.
 */
class Spatial3dAudioFxController {

    companion object {
        private const val TAG = "Spatial3dAudioFxController"
    }

    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null

    fun attach(audioSessionId: Int, is3dAudioEnabled: Boolean, audio3dStrength: Short) {
        release()
        if (audioSessionId == 0) return

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = is3dAudioEnabled
                if (strengthSupported) {
                    setStrength(if (is3dAudioEnabled) audio3dStrength else 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error init Virtualizer: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = is3dAudioEnabled
                if (strengthSupported) {
                    setStrength((audio3dStrength * 0.7f).toInt().toShort())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error init BassBoost: ${e.message}")
        }
    }

    fun release() {
        try {
            virtualizer?.enabled = false
            virtualizer?.release()
        } catch (e: Exception) {
            // ignorar
        }
        virtualizer = null

        try {
            bassBoost?.enabled = false
            bassBoost?.release()
        } catch (e: Exception) {
            // ignorar
        }
        bassBoost = null
    }

    fun apply3dAudioFx(enabled: Boolean, strength: Short, mode: Audio3dSpeakerMode) {
        try {
            virtualizer?.let { virt ->
                virt.enabled = enabled
                if (virt.strengthSupported) {
                    val virtStrength = when (mode) {
                        Audio3dSpeakerMode.DUAL_SPEAKER -> strength
                        Audio3dSpeakerMode.SINGLE_SPEAKER -> (strength * 0.9f).toInt().toShort()
                        Audio3dSpeakerMode.HEADPHONES_3D -> strength
                    }
                    virt.setStrength(virtStrength.coerceIn(0, 1000))
                }
            }

            bassBoost?.let { bb ->
                bb.enabled = enabled
                if (bb.strengthSupported) {
                    val bassStrength = when (mode) {
                        Audio3dSpeakerMode.SINGLE_SPEAKER -> (strength * 0.95f).toInt().toShort()
                        Audio3dSpeakerMode.DUAL_SPEAKER -> (strength * 0.65f).toInt().toShort()
                        Audio3dSpeakerMode.HEADPHONES_3D -> (strength * 0.5f).toInt().toShort()
                    }
                    bb.setStrength(bassStrength.coerceIn(0, 1000))
                }
            }
            Log.i(TAG, "Aplicado FX Audio 3D: enabled=$enabled, mode=$mode, strength=$strength")
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando FX Audio 3D: ${e.message}")
        }
    }
}
