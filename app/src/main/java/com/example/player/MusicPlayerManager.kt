package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.db.TrackEntity
import com.example.player.controllers.AudioEffectsController
import com.example.player.controllers.QueueController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SleepTimerOption(val label: String, val minutes: Int?) {
    OFF("Desactivado", null),
    MIN_5("5 minutos", 5),
    MIN_10("10 minutos", 10),
    MIN_15("15 minutos", 15),
    MIN_30("30 minutos", 30),
    MIN_45("45 minutos", 45),
    MIN_60("60 minutos", 60),
    FINISH_TRACK("Al finalizar la canción", -1),
    CUSTOM("Personalizado", null)
}

class MusicPlayerManager(private val context: Context) {

    companion object {
        @Volatile
        var instance: MusicPlayerManager? = null
    }

    init {
        instance = this
    }

    private val TAG = "MusicPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null

    val queueController = QueueController()
    val effectsController = AudioEffectsController()

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    val queue: StateFlow<List<TrackEntity>> = queueController.queue
    val currentIndex: StateFlow<Int> = queueController.currentIndex
    val isShuffle: StateFlow<Boolean> = queueController.isShuffle
    val repeatMode: StateFlow<RepeatMode> = queueController.repeatMode

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _sleepTimerOption = MutableStateFlow(SleepTimerOption.OFF)
    val sleepTimerOption: StateFlow<SleepTimerOption> = _sleepTimerOption.asStateFlow()

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    private var sleepTimerJob: Job? = null

    val isEqEnabled: StateFlow<Boolean> = effectsController.isEqEnabled
    val bandGainsDb: StateFlow<FloatArray> = effectsController.bandGainsDb
    val eqPreset: StateFlow<com.example.ui.components.player.EqPreset> = effectsController.eqPreset
    val is3dAudioEnabled: StateFlow<Boolean> = effectsController.is3dAudioEnabled
    val audio3dStrength: StateFlow<Float> = effectsController.audio3dStrength
    val audio3dMode: StateFlow<Audio3dSpeakerMode> = effectsController.audio3dMode
    val reverbEnvironment: StateFlow<SpatialReverbEnvironment> = effectsController.reverbEnvironment
    val reverbStrength: StateFlow<Float> = effectsController.reverbStrength
    val crossfadeDurationSec: StateFlow<Float> = effectsController.crossfadeDurationSec
    val isVolumeNormalizerEnabled: StateFlow<Boolean> = effectsController.isVolumeNormalizerEnabled
    val targetLufs: StateFlow<Float> = effectsController.targetLufs

    fun setSleepTimer(option: SleepTimerOption, customMinutes: Int? = null) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerOption.value = option

        if (option == SleepTimerOption.OFF) {
            _sleepTimerRemainingMs.value = null
            effectsController.applyVolumeSettings(mediaPlayer, _currentTrack.value)
            return
        }

        val totalMinutes = if (option == SleepTimerOption.CUSTOM) customMinutes ?: 15 else option.minutes

        if (option == SleepTimerOption.FINISH_TRACK) {
            _sleepTimerRemainingMs.value = null
            return
        }

        if (totalMinutes == null || totalMinutes <= 0) {
            _sleepTimerRemainingMs.value = null
            return
        }

        val totalMs = totalMinutes * 60 * 1000L
        _sleepTimerRemainingMs.value = totalMs

        sleepTimerJob = scope.launch {
            var remaining = totalMs
            val fadeDurationMs = 15000L
            val baseVolumeMult = VolumeNormalizerEngine.calculateVolumeGainMultiplier(
                track = _currentTrack.value,
                targetLufs = targetLufs.value,
                enabled = isVolumeNormalizerEnabled.value
            )

            while (remaining > 0 && isActive) {
                delay(1000)
                remaining -= 1000
                _sleepTimerRemainingMs.value = remaining.coerceAtLeast(0)

                if (remaining <= fadeDurationMs) {
                    val fadeFactor = (remaining.toFloat() / fadeDurationMs.toFloat()).coerceIn(0f, 1f)
                    val vol = baseVolumeMult * fadeFactor
                    try {
                        mediaPlayer?.setVolume(vol, vol)
                    } catch (e: Exception) { /* ignore */ }
                }
            }

            if (isActive) {
                if (_isPlaying.value) {
                    togglePlayPause()
                }
                _sleepTimerOption.value = SleepTimerOption.OFF
                _sleepTimerRemainingMs.value = null
                effectsController.applyVolumeSettings(mediaPlayer, _currentTrack.value)
            }
        }
    }

    fun setCrossfadeDuration(seconds: Float) = effectsController.setCrossfadeDuration(seconds)
    fun setVolumeNormalizerEnabled(enabled: Boolean) = effectsController.setVolumeNormalizerEnabled(enabled, mediaPlayer, _currentTrack.value)
    fun setTargetLufs(lufs: Float) = effectsController.setTargetLufs(lufs, mediaPlayer, _currentTrack.value)
    fun set3dAudioEnabled(enabled: Boolean) = effectsController.set3dAudioEnabled(enabled)
    fun set3dAudioStrength(strength: Float) = effectsController.set3dAudioStrength(strength)
    fun set3dAudioMode(mode: Audio3dSpeakerMode) = effectsController.set3dAudioMode(mode)
    fun setReverbEnvironment(environment: SpatialReverbEnvironment) = effectsController.setReverbEnvironment(environment)
    fun setReverbStrength(strength: Float) = effectsController.setReverbStrength(strength)
    fun setEqEnabled(enabled: Boolean) = effectsController.setEqEnabled(enabled)
    fun setBandGain(bandIndex: Int, gainDb: Float) = effectsController.setBandGain(bandIndex, gainDb)
    fun setEqPreset(preset: com.example.ui.components.player.EqPreset) = effectsController.setEqPreset(preset)
    fun resetEq() = effectsController.resetEq()

    fun peekNextTrack(): TrackEntity? = queueController.peekNextTrack()
    fun moveQueueItem(fromIndex: Int, toIndex: Int) = queueController.moveQueueItem(fromIndex, toIndex)
    fun removeQueueItem(index: Int) = queueController.removeQueueItem(index)
    fun addTrackToNext(track: TrackEntity) = queueController.addTrackToNext(track)
    fun addTrackToQueue(track: TrackEntity) = queueController.addTrackToQueue(track)
    fun playQueueIndex(index: Int) {
        val target = queueController.playQueueIndex(index)
        if (target != null) playTrack(target)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed.coerceIn(0.25f, 2.0f)
        effectsController.dspEngine.applyDspParams(mediaPlayer, _playbackSpeed.value, _playbackPitch.value)
    }

    fun setPitch(pitch: Float) {
        _playbackPitch.value = pitch.coerceIn(0.25f, 2.0f)
        effectsController.dspEngine.applyDspParams(mediaPlayer, _playbackSpeed.value, _playbackPitch.value)
    }

    fun setQueueAndPlay(tracks: List<TrackEntity>, startIndex: Int = 0) {
        val targetTrack = queueController.setQueue(tracks, startIndex)
        if (targetTrack != null) {
            playTrack(targetTrack)
        }
    }

    fun playTrack(track: TrackEntity) {
        try {
            stopAndRelease()

            _currentTrack.value = track
            _playbackError.value = null

            val uri = Uri.parse(track.uriString)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                start()
            }

            effectsController.attachEffects(mediaPlayer, track, _playbackSpeed.value, _playbackPitch.value)

            _durationMs.value = mediaPlayer?.duration?.toLong() ?: track.durationMs
            _isPlaying.value = true

            startPositionUpdates()
            notifyService()

            mediaPlayer?.setOnCompletionListener {
                onTrackCompleted()
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                _playbackError.value = "Error al reproducir el archivo. Verifica los permisos o el archivo."
                _isPlaying.value = false
                true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to play track ${track.title}", e)
            _playbackError.value = "No se pudo abrir este archivo de audio (${e.localizedMessage})"
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopPositionUpdates()
        } else {
            player.start()
            _isPlaying.value = true
            startPositionUpdates()
        }
        notifyService()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
            notifyService()
        }
    }

    fun nextTrack() {
        val next = queueController.getNextTrack(_currentPositionMs.value)
        if (next != null) {
            playTrack(next)
        } else {
            _isPlaying.value = false
            stopPositionUpdates()
        }
    }

    fun previousTrack() {
        val (resetToStart, prevTrack) = queueController.getPreviousTrack(_currentPositionMs.value)
        if (resetToStart) {
            seekTo(0)
        } else if (prevTrack != null) {
            playTrack(prevTrack)
        }
    }

    fun toggleShuffle() = queueController.toggleShuffle()
    fun toggleRepeat() = queueController.toggleRepeat()
    fun clearError() { _playbackError.value = null }

    private fun onTrackCompleted() {
        if (_sleepTimerOption.value == SleepTimerOption.FINISH_TRACK) {
            if (_isPlaying.value) {
                togglePlayPause()
            }
            _sleepTimerOption.value = SleepTimerOption.OFF
            _sleepTimerRemainingMs.value = null
            effectsController.applyVolumeSettings(mediaPlayer, _currentTrack.value)
        } else {
            nextTrack()
        }
    }

    private var isCrossfadeInProgress = false

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPos = player.currentPosition.toLong()
                        val totalDur = _durationMs.value
                        _currentPositionMs.value = currentPos

                        val xfadeSec = crossfadeDurationSec.value
                        if (xfadeSec > 0.5f && totalDur > 10000 && !isCrossfadeInProgress) {
                            val remainingMs = totalDur - currentPos
                            if (remainingMs in 100..((xfadeSec * 1000).toLong())) {
                                isCrossfadeInProgress = true
                                launch {
                                    val steps = 10
                                    val delayMs = (remainingMs / steps).coerceAtLeast(50L)
                                    for (i in 1..steps) {
                                        val volFactor = (1.0f - (i.toFloat() / steps)).coerceIn(0f, 1f)
                                        val normMult = VolumeNormalizerEngine.calculateVolumeGainMultiplier(_currentTrack.value, targetLufs.value, isVolumeNormalizerEnabled.value)
                                        val effVol = normMult * volFactor
                                        try {
                                            player.setVolume(effVol, effVol)
                                        } catch (e: Exception) { /* ignore */ }
                                        delay(delayMs)
                                    }
                                    isCrossfadeInProgress = false
                                    nextTrack()
                                }
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun stopAndRelease() {
        stopPositionUpdates()
        effectsController.release()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        notifyService()
    }

    private fun notifyService() {
        MediaPlaybackService.syncNotification(
            context = context,
            track = _currentTrack.value,
            playing = _isPlaying.value,
            positionMs = _currentPositionMs.value,
            duration = _durationMs.value,
            speed = _playbackSpeed.value
        )
    }

    fun release() {
        stopAndRelease()
    }
}
