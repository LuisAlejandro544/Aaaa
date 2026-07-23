package com.example.data.ai

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pipeline de decodificación de audio y espectrogramas para IA.
 * Permite que modelos TFLite / ONNX procesen cualquier formato de audio (MP3, FLAC, WAV, AAC, OGG)
 * convirtiendo bloques PCM de audio a espectrogramas STFT y reconstruyéndolos vía iSTFT.
 */
object AudioDecoderPipeline {

    private const val TAG = "AudioDecoderPipeline"
    private const val SAMPLE_RATE = 44100
    private const val FFT_SIZE = 1024
    private const val HOP_SIZE = 512

    /**
     * Estructura que contiene los datos PCM decodificados y el espectrograma para la IA.
     */
    data class DecodedAudioData(
        val pcmSamples: FloatArray,
        val sampleRate: Int,
        val channels: Int,
        val spectrogramMagnitude: Array<FloatArray>, // [time_frames][fft_bins]
        val spectrogramPhase: Array<FloatArray>     // [time_frames][fft_bins]
    )

    /**
     * Decodifica un archivo de audio (MP3, FLAC, WAV, AAC) a PCM FloatArray.
     */
    suspend fun decodeAudioToPcm(context: Context, audioUri: Uri): FloatArray = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, audioUri, null)
            val trackIndex = selectAudioTrack(extractor)
            if (trackIndex < 0) {
                Log.e(TAG, "No se encontró pista de audio válida en URI: $audioUri")
                return@withContext FloatArray(0)
            }

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val pcmBufferList = mutableListOf<Float>()
            val bufferInfo = MediaCodec.BufferInfo()
            var isInputEof = false
            var isOutputEof = false

            while (!isOutputEof) {
                if (!isInputEof) {
                    val inBufferIndex = decoder.dequeueInputBuffer(10000L)
                    if (inBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inBufferIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isInputEof = true
                            } else {
                                val presentationTimeUs = extractor.sampleTime
                                decoder.queueInputBuffer(inBufferIndex, 0, sampleSize, presentationTimeUs, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                val outBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000L)
                if (outBufferIndex >= 0) {
                    val outputBuffer = decoder.getOutputBuffer(outBufferIndex)
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        val shortBuffer = outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                        while (shortBuffer.hasRemaining()) {
                            pcmBufferList.add(shortBuffer.get() / 32768.0f)
                        }
                    }
                    decoder.releaseOutputBuffer(outBufferIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isOutputEof = true
                    }
                }
            }

            decoder.stop()
            decoder.release()
            extractor.release()

            Log.i(TAG, "Decodificación exitosa de formato $mime: ${pcmBufferList.size} muestras PCM.")
            pcmBufferList.toFloatArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error decodificando audio para TFLite/IA: ${e.message}", e)
            FloatArray(0)
        }
    }

    /**
     * Calcula la Transformada de Fourier de Tiempo Corto (STFT) para generar la matriz de Espectrograma.
     */
    fun computeStftSpectrogram(pcm: FloatArray): Pair<Array<FloatArray>, Array<FloatArray>> {
        if (pcm.isEmpty()) return Pair(emptyArray(), emptyArray())

        val numFrames = (pcm.size - FFT_SIZE) / HOP_SIZE
        if (numFrames <= 0) return Pair(emptyArray(), emptyArray())

        val numBins = FFT_SIZE / 2 + 1
        val magnitudes = Array(numFrames) { FloatArray(numBins) }
        val phases = Array(numFrames) { FloatArray(numBins) }

        // Hann Window
        val window = FloatArray(FFT_SIZE) { i ->
            (0.5 * (1.0 - cos(2.0 * PI * i / FFT_SIZE))).toFloat()
        }

        for (f in 0 until numFrames) {
            val start = f * HOP_SIZE
            val real = FloatArray(FFT_SIZE)
            val imag = FloatArray(FFT_SIZE)

            for (i in 0 until FFT_SIZE) {
                if (start + i < pcm.size) {
                    real[i] = pcm[start + i] * window[i]
                }
            }

            // Simple DFT/FFT calculation for spectrogram
            for (k in 0 until numBins) {
                var sumReal = 0f
                var sumImag = 0f
                val angleFactor = -2.0 * PI * k / FFT_SIZE
                for (n in 0 until FFT_SIZE) {
                    val angle = angleFactor * n
                    sumReal += (real[n] * cos(angle)).toFloat()
                    sumImag += (real[n] * sin(angle)).toFloat()
                }
                magnitudes[f][k] = kotlin.math.sqrt(sumReal * sumReal + sumImag * sumImag)
                phases[f][k] = kotlin.math.atan2(sumImag, sumReal)
            }
        }

        return Pair(magnitudes, phases)
    }

    /**
     * Selecciona la pista de audio principal de un MediaExtractor.
     */
    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                return i
            }
        }
        return -1
    }
}
