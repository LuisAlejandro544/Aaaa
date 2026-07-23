package com.example.player.controllers

import android.media.audiofx.Equalizer
import android.util.Log
import com.example.player.OboeNativeAudioEngine

/**
 * Controlador modular para la gestión del Ecualizador y bandas de frecuencia.
 */
class EqualizerFxController {

    companion object {
        private const val TAG = "EqualizerFxController"
    }

    var equalizer: Equalizer? = null
        private set

    fun attach(audioSessionId: Int) {
        release()
        try {
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                Log.i(TAG, "Ecualizador acoplado a audioSessionId $audioSessionId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando Ecualizador: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Exception) {
            // ignorar
        }
        equalizer = null
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray) {
        val eq = equalizer ?: return
        try {
            if (!isEqEnabled) return
            if (!eq.enabled) eq.enabled = true
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minEQLevel = eq.bandLevelRange[0]
            val maxEQLevel = eq.bandLevelRange[1]

            for (i in 0 until numBands) {
                val band = i.toShort()
                val gainIndex = (i * 5 / numBands).coerceIn(0, 4)
                val gainDb = bandGainsDb.getOrElse(gainIndex) { 0f }
                val levelMb = (gainDb * 100f).toInt().toShort()
                eq.setBandLevel(band, levelMb.coerceIn(minEQLevel, maxEQLevel))
            }

            // Cálculo de coeficientes de filtro biquad C++ para DSP nativo
            OboeNativeAudioEngine.calculateBiquadCoefficients(bandGainsDb)
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando bandas EQ personalizadas: ${e.message}")
        }
    }
}
