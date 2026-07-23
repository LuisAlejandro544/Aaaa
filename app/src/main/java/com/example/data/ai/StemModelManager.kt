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

data class TfliteModelInfo(
    val filename: String,
    val downloadUrl: String,
    val label: String,
    val estimatedSizeMb: Float
)

data class ModelDownloadStatus(
    val isDownloading: Boolean = false,
    val currentModelIndex: Int = 0,
    val totalModels: Int = 4,
    val currentModelName: String = "",
    val progressPercent: Int = 0,
    val downloadedSizeMb: Float = 0f,
    val totalSizeMb: Float = 74.9f,
    val isDownloaded: Boolean = false,
    val showPromptDialog: Boolean = false,
    val releaseTagUrl: String = "https://github.com/LuisAlejandro544/Modelos/releases/tag/v1.0",
    val statusMessage: String = "Modelos TFLite FP16 no descargados."
)

object StemModelManager {

    private const val TAG = "StemModelManager"
    private const val PREFS_NAME = "stem_models_prefs"
    private const val KEY_USER_DISMISSED_PROMPT = "user_dismissed_prompt"

    val GITHUB_RELEASE_URL = "https://github.com/LuisAlejandro544/Modelos/releases/tag/v1.0"

    val TFLITE_MODELS = listOf(
        TfliteModelInfo(
            filename = "uvr_mdx_voc_ft_fp16.tflite",
            downloadUrl = "https://github.com/LuisAlejandro544/Modelos/releases/download/v1.0/uvr_mdx_voc_ft_fp16.tflite",
            label = "Voz (Vocals - UVR MDX)",
            estimatedSizeMb = 32.0f
        ),
        TfliteModelInfo(
            filename = "kuielab_a_bass_fp16.tflite",
            downloadUrl = "https://github.com/LuisAlejandro544/Modelos/releases/download/v1.0/kuielab_a_bass_fp16.tflite",
            label = "Bajo (Bass - Kuielab)",
            estimatedSizeMb = 14.3f
        ),
        TfliteModelInfo(
            filename = "kuielab_a_drums_fp16.tflite",
            downloadUrl = "https://github.com/LuisAlejandro544/Modelos/releases/download/v1.0/kuielab_a_drums_fp16.tflite",
            label = "Batería (Drums - Kuielab)",
            estimatedSizeMb = 14.3f
        ),
        TfliteModelInfo(
            filename = "kuielab_a_other_fp16.tflite",
            downloadUrl = "https://github.com/LuisAlejandro544/Modelos/releases/download/v1.0/kuielab_a_other_fp16.tflite",
            label = "Melodía (Other - Kuielab)",
            estimatedSizeMb = 14.3f
        )
    )

    private val _status = MutableStateFlow(ModelDownloadStatus())
    val status: StateFlow<ModelDownloadStatus> = _status.asStateFlow()

    fun getModelsDir(context: Context): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) modelsDir.mkdirs()
        return modelsDir
    }

    fun getModelFile(context: Context): File {
        return File(getModelsDir(context), "uvr_mdx_voc_ft_fp16.tflite")
    }

    suspend fun downloadModelFromUrl(
        context: Context,
        serverUrl: String = ""
    ): Boolean {
        return downloadAllModels(context)
    }

    fun checkLocalModels(context: Context): Boolean {
        val modelsDir = getModelsDir(context)
        var downloadedCount = 0

        for (model in TFLITE_MODELS) {
            val file = File(modelsDir, model.filename)
            if (file.exists() && file.length() > 100_000) {
                downloadedCount++
            }
        }

        val allDownloaded = downloadedCount == TFLITE_MODELS.size
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userDismissed = prefs.getBoolean(KEY_USER_DISMISSED_PROMPT, false)

        val shouldShowDialog = !allDownloaded && !userDismissed && !_status.value.isDownloading

        _status.value = _status.value.copy(
            isDownloaded = allDownloaded,
            showPromptDialog = shouldShowDialog,
            statusMessage = if (allDownloaded) {
                "¡Los 4 modelos TFLite FP16 ($downloadedCount/4) están activos localmente!"
            } else {
                "Modelos IA pendientes ($downloadedCount/4 descargados). Puedes descargarlos sin interrupciones."
            }
        )
        return allDownloaded
    }

    fun dismissPromptDialog(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USER_DISMISSED_PROMPT, true).apply()
        _status.value = _status.value.copy(showPromptDialog = false)
    }

    fun showPromptDialogManually() {
        _status.value = _status.value.copy(showPromptDialog = true)
    }

    suspend fun downloadAllModels(context: Context): Boolean = withContext(Dispatchers.IO) {
        val modelsDir = getModelsDir(context)

        _status.value = _status.value.copy(
            isDownloading = true,
            showPromptDialog = false,
            progressPercent = 0,
            statusMessage = "Conectando con GitHub Releases v1.0..."
        )

        var successCount = 0
        val totalModels = TFLITE_MODELS.size

        for ((index, model) in TFLITE_MODELS.withIndex()) {
            val targetFile = File(modelsDir, model.filename)

            // Si ya existe y es válido, omitir descarga
            if (targetFile.exists() && targetFile.length() > 100_000) {
                successCount++
                continue
            }

            _status.value = _status.value.copy(
                currentModelIndex = index + 1,
                currentModelName = model.filename,
                statusMessage = "Descargando model ${index + 1}/$totalModels: ${model.label}..."
            )

            val downloadedOk = downloadSingleFile(
                downloadUrl = model.downloadUrl,
                targetFile = targetFile,
                onProgress = { currentBytes, totalBytes ->
                    val filePercent = if (totalBytes > 0) ((currentBytes * 100) / totalBytes).toInt() else 0
                    val overallPercent = (((index * 100) + filePercent) / totalModels)
                    val currentMb = currentBytes / (1024f * 1024f)
                    val totalMb = if (totalBytes > 0) totalBytes / (1024f * 1024f) else model.estimatedSizeMb

                    _status.value = _status.value.copy(
                        progressPercent = overallPercent,
                        downloadedSizeMb = currentMb,
                        totalSizeMb = totalMb,
                        statusMessage = "Descargando ${model.filename} (${index + 1}/$totalModels): $filePercent% (${String.format("%.1f", currentMb)}MB / ${String.format("%.1f", totalMb)}MB)"
                    )
                }
            )

            if (downloadedOk) {
                successCount++
            } else {
                Log.e(TAG, "Error descargando archivo: ${model.filename}")
            }
        }

        val allOk = successCount == totalModels
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (allOk) {
            prefs.edit().putBoolean(KEY_USER_DISMISSED_PROMPT, true).apply()
        }

        _status.value = _status.value.copy(
            isDownloading = false,
            isDownloaded = allOk,
            progressPercent = if (allOk) 100 else _status.value.progressPercent,
            statusMessage = if (allOk) {
                "¡Los 4 modelos IA TFLite FP16 fueron descargados e instalados con éxito!"
            } else {
                "Descarga incompleta ($successCount/$totalModels instalados). Reintenta cuando tengas conexión."
            }
        )

        Log.i(TAG, "Resultado de descarga de modelos: $successCount / $totalModels exitosos.")
        allOk
    }

    private fun downloadSingleFile(
        downloadUrl: String,
        targetFile: File,
        onProgress: (Long, Long) -> Unit
    ): Boolean {
        var connection: HttpURLConnection? = null
        try {
            var url = URL(downloadUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 20000
            connection.readTimeout = 40000
            connection.connect()

            // Manejar redirección HTTP (GitHub Releases redirige a objetos S3 / assets)
            var responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == 307 || responseCode == 308) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                url = URL(newUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 20000
                connection.readTimeout = 40000
                connection.connect()
                responseCode = connection.responseCode
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Respuesta no OK del servidor: $responseCode para URL: $downloadUrl")
                return false
            }

            val fileLength = connection.contentLength.toLong()
            val input = connection.inputStream
            val output = FileOutputStream(targetFile)

            val buffer = ByteArray(64 * 1024)
            var totalBytesRead = 0L
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                onProgress(totalBytesRead, fileLength)
            }

            output.flush()
            output.close()
            input.close()
            return targetFile.length() > 100_000
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando $downloadUrl", e)
            return false
        } finally {
            connection?.disconnect()
        }
    }
}
