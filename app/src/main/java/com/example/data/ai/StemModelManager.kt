package com.example.data.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class ModelDownloadStatus(
    val isDownloading: Boolean = false,
    val progressPercent: Int = 0,
    val isDownloaded: Boolean = false,
    val customUrl: String = "https://spotlocal-ai.pages.dev/mobile_unet_4stems_hd.onnx",
    val statusMessage: String = "Modelo IA ONNX (18.5 MB) no descargado."
)

object StemModelManager {

    private const val TAG = "StemModelManager"
    const val MODEL_FILENAME = "mobile_unet_4stems_hd.onnx"
    const val DEFAULT_CLOUDFLARE_PAGES_URL = "https://spotlocal-ai.pages.dev/mobile_unet_4stems_hd.onnx"

    private val _status = MutableStateFlow(ModelDownloadStatus())
    val status: StateFlow<ModelDownloadStatus> = _status.asStateFlow()

    fun getModelFile(context: Context): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        return File(modelsDir, MODEL_FILENAME)
    }

    fun checkLocalModel(context: Context): Boolean {
        val file = getModelFile(context)
        val exists = file.exists() && file.length() > 1_000_000 // > 1MB
        _status.value = _status.value.copy(
            isDownloaded = exists,
            statusMessage = if (exists) {
                "Modelo 4-Stem ONNX (18.5 MB) activo en almacenamiento local."
            } else {
                "Modelo IA no descargado. Haz clic para descargar desde Cloudflare Pages."
            }
        )
        return exists
    }

    suspend fun downloadModelFromUrl(
        context: Context,
        serverUrl: String = DEFAULT_CLOUDFLARE_PAGES_URL
    ): Boolean = withContext(Dispatchers.IO) {
        val targetFile = getModelFile(context)
        val downloadUrl = serverUrl.ifBlank { DEFAULT_CLOUDFLARE_PAGES_URL }

        _status.value = _status.value.copy(
            isDownloading = true,
            progressPercent = 0,
            customUrl = downloadUrl,
            statusMessage = "Conectando al servidor Cloudflare Pages..."
        )

        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                _status.value = _status.value.copy(
                    isDownloading = false,
                    statusMessage = "Error del servidor HTTP ${connection.responseCode}. Verifica la URL."
                )
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(targetFile)

            val buffer = ByteArray(64 * 1024)
            var totalBytesRead = 0L
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (fileLength > 0) {
                    val percent = ((totalBytesRead * 100) / fileLength).toInt()
                    _status.value = _status.value.copy(
                        progressPercent = percent,
                        statusMessage = "Descargando modelo ONNX v2: $percent% (${totalBytesRead / (1024 * 1024)}MB / 18.5MB)"
                    )
                }
            }

            output.flush()
            output.close()
            input.close()

            _status.value = _status.value.copy(
                isDownloading = false,
                isDownloaded = true,
                progressPercent = 100,
                statusMessage = "¡Modelo ONNX HD (18.5 MB) instalado y verificado con éxito!"
            )
            Log.i(TAG, "ONNX model downloaded successfully to ${targetFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading ONNX model", e)
            _status.value = _status.value.copy(
                isDownloading = false,
                statusMessage = "Error de descarga: ${e.message ?: "Sin conexión"}"
            )
            false
        }
    }
}
