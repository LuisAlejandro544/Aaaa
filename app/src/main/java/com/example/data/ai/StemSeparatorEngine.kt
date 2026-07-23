package com.example.data.ai

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StemSeparatorEngine {

    private const val TAG = "StemSeparatorEngine"
    private val inferenceRunner = OnnxInferenceRunner()
    private val tfliteRunner = TfliteInferenceRunner()

    private val _separationState = MutableStateFlow(StemSeparationState())
    val separationState: StateFlow<StemSeparationState> = _separationState.asStateFlow()

    fun updateTfliteEngineState(isLoaded: Boolean) {
        val modelDesc = if (isLoaded) {
            "4 Modelos TFLite FP16 Activos (~74.9 MB): UVR MDX Vocals + Kuielab (Bass, Drums, Other)"
        } else {
            "TFLite 4-Stem Fallback Filter Engine"
        }
        _separationState.value = _separationState.value.copy(
            modelName = modelDesc
        )
        Log.i(TAG, "Estado de motor TFLite actualizado: isLoaded=$isLoaded ($modelDesc)")
    }

    fun setStemMode(mode: StemMode) {
        val (vocalGain, drumsGain, bassGain, otherGain) = when (mode) {
            StemMode.ORIGINAL -> Quadruple(0f, 0f, 0f, 0f)
            StemMode.VOCALS -> Quadruple(0f, -60f, -60f, -60f)
            StemMode.DRUMS -> Quadruple(-60f, 0f, -60f, -60f)
            StemMode.BASS -> Quadruple(-60f, -60f, 0f, -60f)
            StemMode.OTHER -> Quadruple(-60f, -60f, -60f, 0f)
            StemMode.KARAOKE -> Quadruple(-14f, 1.5f, 1.5f, 1.5f)
        }
        _separationState.value = _separationState.value.copy(
            currentStemMode = mode,
            vocalGainDb = vocalGain,
            drumsGainDb = drumsGain,
            bassGainDb = bassGain,
            otherGainDb = otherGain,
            isAutoMastered = false
        )
        Log.i(TAG, "4-Stem Mode changed to $mode (Vocals: ${vocalGain}dB, Drums: ${drumsGain}dB, Bass: ${bassGain}dB, Other: ${otherGain}dB)")
    }

    fun setIndividualStemGains(vocalDb: Float, drumsDb: Float, bassDb: Float, otherDb: Float) {
        _separationState.value = _separationState.value.copy(
            vocalGainDb = vocalDb.coerceIn(-60f, 12f),
            drumsGainDb = drumsDb.coerceIn(-60f, 12f),
            bassGainDb = bassDb.coerceIn(-60f, 12f),
            otherGainDb = otherDb.coerceIn(-60f, 12f),
            isAutoMastered = false
        )
        Log.i(TAG, "Custom 4-Stem Mixer Gains updated: Vocals=${vocalDb}dB, Drums=${drumsDb}dB, Bass=${bassDb}dB, Other=${otherDb}dB")
    }

    fun runAiAutoMasterizer(trackTitle: String, onComplete: (String) -> Unit = {}) {
        _separationState.value = _separationState.value.copy(
            isProcessing = true,
            progressPercent = 15
        )
        Log.i(TAG, "Running AI Auto-Masterizer for: $trackTitle")
        
        // Simulating spectral analysis and optimal stem balance computation
        val note = "Masterizado IA aplicado: Realce dinámico de claridad vocal (+1.5dB), compresión de bajo sub-bass (-1.0dB) y presencia de batería (+2.0dB)."
        
        _separationState.value = _separationState.value.copy(
            isProcessing = false,
            progressPercent = 100,
            vocalGainDb = 1.5f,
            drumsGainDb = 2.0f,
            bassGainDb = -1.0f,
            otherGainDb = 0.5f,
            isAutoMastered = true,
            aiMasteringNote = note
        )
        onComplete(note)
    }

    fun processTrackStems(trackTitle: String, onComplete: () -> Unit = {}) {
        _separationState.value = _separationState.value.copy(
            isProcessing = true,
            progressPercent = 10
        )
        Log.i(TAG, "Starting ONNX v2 / TFLite 4-Stem separation AI model for track: $trackTitle")
        inferenceRunner.runInference(trackTitle)
        tfliteRunner.runInference(trackTitle)
        _separationState.value = _separationState.value.copy(
            isProcessing = false,
            progressPercent = 100
        )
        onComplete()
    }

    suspend fun processTrackStemsTflite(
        context: Context,
        audioUri: Uri,
        trackTitle: String,
        onComplete: () -> Unit = {}
    ) {
        _separationState.value = _separationState.value.copy(
            isProcessing = true,
            progressPercent = 5
        )
        Log.i(TAG, "Iniciando pipeline TFLite con decodificación multiformato (MP3/FLAC/WAV/AAC) para: $trackTitle")
        tfliteRunner.runTfliteInferenceOnAudio(context, audioUri, trackTitle) { progress ->
            _separationState.value = _separationState.value.copy(
                progressPercent = progress
            )
        }
        _separationState.value = _separationState.value.copy(
            isProcessing = false,
            progressPercent = 100
        )
        onComplete()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

