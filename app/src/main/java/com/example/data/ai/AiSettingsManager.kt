package com.example.data.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AiSettingsState(
    val isVibePlaylistGeneratorEnabled: Boolean = true,
    val isLyricsTranslatorEnabled: Boolean = true,
    val isVibeMatchEnabled: Boolean = true,
    val customApiKey: String = "",
    val selectedModel: String = "gemini-3.6-flash"
)

class AiSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("spotlocal_ai_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        AiSettingsState(
            isVibePlaylistGeneratorEnabled = prefs.getBoolean("vibe_playlist_gen", true),
            isLyricsTranslatorEnabled = prefs.getBoolean("lyrics_translator", true),
            isVibeMatchEnabled = prefs.getBoolean("vibe_match", true),
            customApiKey = prefs.getString("custom_api_key", "") ?: "",
            selectedModel = prefs.getString("selected_model", "gemini-3.6-flash") ?: "gemini-3.6-flash"
        )
    )
    val settings: StateFlow<AiSettingsState> = _settings.asStateFlow()

    fun setVibePlaylistGeneratorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibe_playlist_gen", enabled).apply()
        _settings.value = _settings.value.copy(isVibePlaylistGeneratorEnabled = enabled)
    }

    fun setLyricsTranslatorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("lyrics_translator", enabled).apply()
        _settings.value = _settings.value.copy(isLyricsTranslatorEnabled = enabled)
    }

    fun setVibeMatchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibe_match", enabled).apply()
        _settings.value = _settings.value.copy(isVibeMatchEnabled = enabled)
    }

    fun setCustomApiKey(apiKey: String) {
        prefs.edit().putString("custom_api_key", apiKey).apply()
        _settings.value = _settings.value.copy(customApiKey = apiKey)
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString("selected_model", model).apply()
        _settings.value = _settings.value.copy(selectedModel = model)
    }
}
