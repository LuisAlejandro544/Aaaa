package com.example.player.controllers

import android.media.MediaPlayer
import com.example.data.db.TrackEntity
import com.example.player.Audio3dSpeakerMode
import com.example.player.AudioDspEngine
import com.example.player.SpatialReverbEnvironment
import com.example.player.VolumeNormalizerEngine
import com.example.ui.components.player.EqPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEffectsController {

    val dspEngine = AudioDspEngine()

    private val _isEqEnabled = MutableStateFlow(true)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _bandGainsDb = MutableStateFlow(floatArrayOf(0f, 0f, 0f, 0f, 0f))
    val bandGainsDb: StateFlow<FloatArray> = _bandGainsDb.asStateFlow()

    private val _eqPreset = MutableStateFlow(EqPreset.FLAT)
    val eqPreset: StateFlow<EqPreset> = _eqPreset.asStateFlow()

    private val _is3dAudioEnabled = MutableStateFlow(true)
    val is3dAudioEnabled: StateFlow<Boolean> = _is3dAudioEnabled.asStateFlow()

    private val _audio3dStrength = MutableStateFlow(0.8f)
    val audio3dStrength: StateFlow<Float> = _audio3dStrength.asStateFlow()

    private val _audio3dMode = MutableStateFlow(Audio3dSpeakerMode.DUAL_SPEAKER)
    val audio3dMode: StateFlow<Audio3dSpeakerMode> = _audio3dMode.asStateFlow()

    private val _reverbEnvironment = MutableStateFlow(SpatialReverbEnvironment.MEDIUM_HALL)
    val reverbEnvironment: StateFlow<SpatialReverbEnvironment> = _reverbEnvironment.asStateFlow()

    private val _reverbStrength = MutableStateFlow(0.7f)
    val reverbStrength: StateFlow<Float> = _reverbStrength.asStateFlow()

    private val _crossfadeDurationSec = MutableStateFlow(3.0f)
    val crossfadeDurationSec: StateFlow<Float> = _crossfadeDurationSec.asStateFlow()

    private val _isVolumeNormalizerEnabled = MutableStateFlow(true)
    val isVolumeNormalizerEnabled: StateFlow<Boolean> = _isVolumeNormalizerEnabled.asStateFlow()

    private val _targetLufs = MutableStateFlow(VolumeNormalizerEngine.DEFAULT_TARGET_LUFS)
    val targetLufs: StateFlow<Float> = _targetLufs.asStateFlow()

    fun attachEffects(mediaPlayer: MediaPlayer?, currentTrack: TrackEntity?, speed: Float, pitch: Float) {
        mediaPlayer?.let { player ->
            dspEngine.attachEqualizer(player.audioSessionId)
            dspEngine.applyCustomEqBands(_isEqEnabled.value, _bandGainsDb.value)
            dspEngine.apply3dAudioFx(_is3dAudioEnabled.value, (_audio3dStrength.value * 1000f).toInt().toShort(), _audio3dMode.value)
            dspEngine.applyReverbEnvironment(_reverbEnvironment.value, _reverbStrength.value)
        }
        dspEngine.applyDspParams(mediaPlayer, speed, pitch)
        applyVolumeSettings(mediaPlayer, currentTrack)
    }

    fun setReverbEnvironment(environment: SpatialReverbEnvironment) {
        _reverbEnvironment.value = environment
        dspEngine.applyReverbEnvironment(environment, _reverbStrength.value)
    }

    fun setReverbStrength(strength: Float) {
        val s = strength.coerceIn(0f, 1f)
        _reverbStrength.value = s
        dspEngine.applyReverbEnvironment(_reverbEnvironment.value, s)
    }

    fun applyVolumeSettings(mediaPlayer: MediaPlayer?, currentTrack: TrackEntity?) {
        val player = mediaPlayer ?: return
        val mult = VolumeNormalizerEngine.calculateVolumeGainMultiplier(
            track = currentTrack,
            targetLufs = _targetLufs.value,
            enabled = _isVolumeNormalizerEnabled.value
        )
        try {
            player.setVolume(mult, mult)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun setCrossfadeDuration(seconds: Float) {
        _crossfadeDurationSec.value = seconds.coerceIn(0.0f, 12.0f)
    }

    fun setVolumeNormalizerEnabled(enabled: Boolean, mediaPlayer: MediaPlayer?, currentTrack: TrackEntity?) {
        _isVolumeNormalizerEnabled.value = enabled
        applyVolumeSettings(mediaPlayer, currentTrack)
    }

    fun setTargetLufs(lufs: Float, mediaPlayer: MediaPlayer?, currentTrack: TrackEntity?) {
        _targetLufs.value = lufs.coerceIn(-24.0f, -8.0f)
        applyVolumeSettings(mediaPlayer, currentTrack)
    }

    fun set3dAudioEnabled(enabled: Boolean) {
        _is3dAudioEnabled.value = enabled
        dspEngine.apply3dAudioFx(
            enabled = enabled,
            strength = (_audio3dStrength.value * 1000f).toInt().toShort(),
            mode = _audio3dMode.value
        )
    }

    fun set3dAudioStrength(strength: Float) {
        val s = strength.coerceIn(0f, 1f)
        _audio3dStrength.value = s
        dspEngine.apply3dAudioFx(
            enabled = _is3dAudioEnabled.value,
            strength = (s * 1000f).toInt().toShort(),
            mode = _audio3dMode.value
        )
    }

    fun set3dAudioMode(mode: Audio3dSpeakerMode) {
        _audio3dMode.value = mode
        dspEngine.apply3dAudioFx(
            enabled = _is3dAudioEnabled.value,
            strength = (_audio3dStrength.value * 1000f).toInt().toShort(),
            mode = mode
        )
    }

    fun setEqEnabled(enabled: Boolean) {
        _isEqEnabled.value = enabled
        dspEngine.applyCustomEqBands(enabled, _bandGainsDb.value)
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        if (bandIndex in 0..4) {
            val newGains = _bandGainsDb.value.copyOf()
            newGains[bandIndex] = gainDb.coerceIn(-12f, 12f)
            _bandGainsDb.value = newGains
            _eqPreset.value = EqPreset.CUSTOM
            dspEngine.applyCustomEqBands(_isEqEnabled.value, newGains)
        }
    }

    fun setEqPreset(preset: EqPreset) {
        _eqPreset.value = preset
        if (preset != EqPreset.CUSTOM) {
            val newGains = preset.bandGainsDb.copyOf()
            _bandGainsDb.value = newGains
            dspEngine.applyCustomEqBands(_isEqEnabled.value, newGains)
        }
    }

    fun resetEq() {
        setEqPreset(EqPreset.FLAT)
    }

    fun release() {
        dspEngine.releaseEqualizer()
    }
}
