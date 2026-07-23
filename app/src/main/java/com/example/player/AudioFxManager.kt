package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.ai.StemMode

class AudioFxManager {

    companion object {
        private const val TAG = "AudioFxManager"
    }

    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null

    fun attachEqualizer(
        audioSessionId: Int,
        currentStemMode: StemMode,
        is3dAudioEnabled: Boolean,
        audio3dStrength: Short,
        currentReverbEnvironment: SpatialReverbEnvironment,
        reverbStrength: Float
    ) {
        try {
            releaseEqualizer()
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                Log.i(TAG, "Equalizer attached to audioSessionId $audioSessionId successfully")
                applyStemMode(currentStemMode)

                try {
                    virtualizer = Virtualizer(0, audioSessionId).apply {
                        enabled = is3dAudioEnabled
                        if (strengthSupported) {
                            setStrength(if (is3dAudioEnabled) audio3dStrength else 0)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Virtualizer init error: ${e.message}")
                }

                try {
                    bassBoost = BassBoost(0, audioSessionId).apply {
                        enabled = is3dAudioEnabled
                        if (strengthSupported) {
                            setStrength((audio3dStrength * 0.7f).toInt().toShort())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "BassBoost init error: ${e.message}")
                }

                try {
                    presetReverb = PresetReverb(0, audioSessionId).apply {
                        enabled = currentReverbEnvironment != SpatialReverbEnvironment.OFF
                        if (currentReverbEnvironment != SpatialReverbEnvironment.OFF) {
                            preset = currentReverbEnvironment.presetValue
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "PresetReverb init error: ${e.message}")
                }

                apply3dAudioFx(is3dAudioEnabled, audio3dStrength, Audio3dSpeakerMode.DUAL_SPEAKER)
                applyReverbEnvironment(currentReverbEnvironment, reverbStrength)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching AudioFX: ${e.message}")
        }
    }

    fun releaseEqualizer() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Exception) {
            // ignore
        }
        equalizer = null

        try {
            virtualizer?.enabled = false
            virtualizer?.release()
        } catch (e: Exception) {
            // ignore
        }
        virtualizer = null

        try {
            bassBoost?.enabled = false
            bassBoost?.release()
        } catch (e: Exception) {
            // ignore
        }
        bassBoost = null

        try {
            presetReverb?.enabled = false
            presetReverb?.release()
        } catch (e: Exception) {
            // ignore
        }
        presetReverb = null
    }

    fun applyReverbEnvironment(
        environment: SpatialReverbEnvironment,
        strength: Float
    ) {
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
            Log.i(TAG, "Applied Spatial Reverb 3D HRTF: env=$environment, strength=$safeStrength")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying PresetReverb: ${e.message}")
        }
    }

    fun apply3dAudioFx(
        enabled: Boolean,
        strength: Short,
        mode: Audio3dSpeakerMode
    ) {
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
            Log.i(TAG, "Applied 3D Audio FX: enabled=$enabled, mode=$mode, strength=$strength")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying 3D Audio FX: ${e.message}")
        }
    }

    fun applyStemMode(mode: StemMode) {
        val eq = equalizer ?: return
        try {
            if (!eq.enabled) eq.enabled = true
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minEQLevel = eq.bandLevelRange[0] // Typically -1500 mB (-15dB)
            val maxEQLevel = eq.bandLevelRange[1] // Typically +1500 mB (+15dB)

            for (i in 0 until numBands) {
                val band = i.toShort()
                val centerFreqHz = eq.getCenterFreq(band) / 1000 // Freq in Hz

                val level: Short = when (mode) {
                    StemMode.ORIGINAL -> 0.toShort()
                    StemMode.VOCALS -> {
                        if (centerFreqHz in 800..4000) (maxEQLevel * 0.85f).toInt().toShort()
                        else (minEQLevel * 0.9f).toInt().toShort()
                    }
                    StemMode.DRUMS -> {
                        if (centerFreqHz in 60..250 || centerFreqHz in 3000..8000) (maxEQLevel * 0.8f).toInt().toShort()
                        else (minEQLevel * 0.7f).toInt().toShort()
                    }
                    StemMode.BASS -> {
                        if (centerFreqHz <= 250) (maxEQLevel * 0.9f).toInt().toShort()
                        else (minEQLevel * 0.95f).toInt().toShort()
                    }
                    StemMode.OTHER -> {
                        if (centerFreqHz in 250..1000 || centerFreqHz in 4000..12000) (maxEQLevel * 0.75f).toInt().toShort()
                        else (minEQLevel * 0.6f).toInt().toShort()
                    }
                    StemMode.KARAOKE -> {
                        if (centerFreqHz in 600..3000) (minEQLevel * 0.6f).toInt().toShort()
                        else (maxEQLevel * 0.3f).toInt().toShort()
                    }
                }
                eq.setBandLevel(band, level.coerceIn(minEQLevel, maxEQLevel))
            }
            Log.i(TAG, "Applied StemMode $mode to Equalizer ($numBands bands)")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting Equalizer band levels for mode $mode: ${e.message}")
        }
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray, currentStemMode: StemMode) {
        val eq = equalizer ?: return
        try {
            if (!isEqEnabled) {
                applyStemMode(currentStemMode)
                return
            }
            if (!eq.enabled) eq.enabled = true
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minEQLevel = eq.bandLevelRange[0] // -1500 mB
            val maxEQLevel = eq.bandLevelRange[1] // +1500 mB

            for (i in 0 until numBands) {
                val band = i.toShort()
                val gainIndex = (i * 5 / numBands).coerceIn(0, 4)
                val gainDb = bandGainsDb.getOrElse(gainIndex) { 0f }
                val levelMb = (gainDb * 100f).toInt().toShort()
                eq.setBandLevel(band, levelMb.coerceIn(minEQLevel, maxEQLevel))
            }

            // Calculate native C++ biquad filter coefficients for hardware DSP
            OboeNativeAudioEngine.calculateBiquadCoefficients(bandGainsDb)

            Log.i(TAG, "Applied custom 5-band Equalizer settings via C++ & Rust engines")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying custom EQ bands: ${e.message}")
        }
    }
}
