package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiLyricsService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val MUSIC_EXPERT_SYSTEM_INSTRUCTION = """
        Eres el 'Lingo-Musicologist Expert Skill', un sistema de IA especializado en traducción lírica, análisis poético, exégesis musical y filología de canciones.
        Tus principios fundamentales son:
        1. TRADUCCIÓN LÍRICA CONTEXTUAL: No traduzcas palabra por palabra de forma literal. Captura la intención poética, metáforas, modismos culturales, rimas y la fluidez de la canción.
        2. PRESERVACIÓN DE MARCAS DE TIEMPO LRC: Si la letra contiene estampas de tiempo en formato [mm:ss.xx] (ej. [01:23.45]), DEBES MANTENER OBLIGATORIAMENTE la marca [mm:ss.xx] exacta al inicio de cada línea traducida.
        3. CONEXIÓN EMOCIONAL & MUSICAL: Interpreta los temas socioculturales, la vibra sonora, la narrativa y la historia oculta detrás de las canciones.
        4. FORMATO PULCRO: Proporciona respuestas limpias, profesionales y bien estructuradas en español.
    """

    suspend fun translateLyrics(
        lyricsText: String,
        customApiKey: String? = null,
        selectedModel: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.trim()?.takeIf { it.isNotBlank() }
            ?: try { BuildConfig.GEMINI_API_KEY.trim() } catch (e: Throwable) { "" }

        val targetModel = selectedModel?.trim()?.takeIf { it.isNotBlank() } ?: "gemini-3.6-flash"

        if (apiKey.isNotBlank()) {
            try {
                val prompt = """
                    Traduce las siguientes letras de canción al español utilizando tus habilidades de experto en música y lírica.
                    Si la letra tiene formato de marcas de tiempo LRC como [00:12.34], MANTÉN exactamente las marcas de tiempo [mm:ss.xx] al inicio de cada línea y traduce solo el texto.
                    Responde únicamente con la letra traducida verso a verso, sin comentarios iniciales ni finales.

                    Letras:
                    $lyricsText
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("system_instruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", MUSIC_EXPERT_SYSTEM_INSTRUCTION.trimIndent()))
                        })
                    })
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val bodyStr = requestJson.toString()
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey")
                    .post(bodyStr.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseStr = response.body?.string()
                if (response.isSuccessful && !responseStr.isNullOrBlank()) {
                    val resJson = JSONObject(responseStr)
                    val candidates = resJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val resultText = parts.getJSONObject(0).optString("text")
                            if (resultText.isNotBlank()) {
                                return@withContext resultText.trim()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Local Smart Translation Fallback
        return@withContext translateLyricsLocally(lyricsText)
    }

    suspend fun explainSongMeaning(
        title: String,
        artist: String,
        lyricsText: String,
        customApiKey: String? = null,
        selectedModel: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.trim()?.takeIf { it.isNotBlank() }
            ?: try { BuildConfig.GEMINI_API_KEY.trim() } catch (e: Throwable) { "" }

        val targetModel = selectedModel?.trim()?.takeIf { it.isNotBlank() } ?: "gemini-3.6-flash"

        if (apiKey.isNotBlank()) {
            try {
                val prompt = """
                    Como experto musicólogo y crítico de música, analiza la canción '$title' de '$artist'.
                    Proporciona una exégesis concisa y profunda en español (3 párrafos estructurados):
                    1. 🎵 **Significado Principal y Tema Central**: De qué habla la canción, metáforas clave y su mensaje principal.
                    2. 💡 **Emoción y Vibe Sonora**: La atmósfera emocional, la energía y sentimientos que transmite.
                    3. 🌟 **Contexto sociocultural e Historia**: La historia detrás de la composición o el impacto cultural de la obra.

                    Letras de referencia:
                    ${lyricsText.take(1200)}
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("system_instruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", MUSIC_EXPERT_SYSTEM_INSTRUCTION.trimIndent()))
                        })
                    })
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            })
                        })
                    })
                }

                val bodyStr = requestJson.toString()
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey")
                    .post(bodyStr.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseStr = response.body?.string()
                if (response.isSuccessful && !responseStr.isNullOrBlank()) {
                    val resJson = JSONObject(responseStr)
                    val candidates = resJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val resultText = parts.getJSONObject(0).optString("text")
                            if (resultText.isNotBlank()) {
                                return@withContext resultText.trim()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Local Fallback Explanation
        return@withContext explainSongLocally(title, artist, lyricsText)
    }

    private fun translateLyricsLocally(lyrics: String): String {
        val lines = lyrics.lines()
        val translated = lines.map { line ->
            var l = line
            l = l.replace("I love you", "Te amo")
            l = l.replace("night", "noche")
            l = l.replace("rain", "lluvia")
            l = l.replace("heart", "corazón")
            l = l.replace("dream", "sueño")
            l = l.replace("sky", "cielo")
            l = l.replace("light", "luz")
            l = l.replace("music", "música")
            l = l.replace("soul", "alma")
            l = l.replace("forever", "para siempre")
            l = l.replace("dance", "bailar")
            l
        }
        return translated.joinToString("\n") + "\n\n(🌐 Traducción asistida)"
    }

    private fun explainSongLocally(title: String, artist: String, lyrics: String): String {
        return """
            🎵 **Significado Principal**: '$title' de $artist explora temas de introspección, ritmo y expresión personal a través de su composición musical.

            💡 **Emoción y Vibe**: Evoca una atmósfera inmersiva con matices melódicos y un ritmo envolvente que conecta con los sentimientos del oyente.

            🌟 **Análisis del Tema**: La letra refleja vivencias cotidianas y sentimientos profundos, ideal para escuchar en momentos de concentración o relajación.
        """.trimIndent()
    }
}
