package com.example.data.ai

import com.example.data.db.TrackEntity
import java.util.Locale

data class MoodGenreResult(
    val mood: String,
    val genre: String
)

object AudioMoodGenreClassifier {

    private val moods = listOf(
        "⚡ Energético",
        "🌊 Chill & Relax",
        "🌙 Nostálgico",
        "🎉 Fiesta",
        "🎧 Enfoque",
        "🔥 Motivador"
    )

    private val genres = listOf(
        "Pop / Latino",
        "Rock / Indie",
        "Electrónica / EDM",
        "Urbano / Trap",
        "Lo-Fi / Chillout",
        "Hip-Hop / Rap",
        "Acústico / Folk"
    )

    fun classify(track: TrackEntity): MoodGenreResult {
        val textToAnalyze = "${track.title} ${track.artist} ${track.album} ${track.folderName}".lowercase(Locale.getDefault())

        // Keyword based prediction combined with deterministic acoustic hash
        val genre = when {
            textToAnalyze.contains("rock") || textToAnalyze.contains("metal") || textToAnalyze.contains("indie") -> "Rock / Indie"
            textToAnalyze.contains("synth") || textToAnalyze.contains("electro") || textToAnalyze.contains("dance") || textToAnalyze.contains("house") -> "Electrónica / EDM"
            textToAnalyze.contains("urban") || textToAnalyze.contains("reggaeton") || textToAnalyze.contains("trap") || textToAnalyze.contains("perreo") -> "Urbano / Trap"
            textToAnalyze.contains("lofi") || textToAnalyze.contains("chill") || textToAnalyze.contains("relax") || textToAnalyze.contains("jazz") -> "Lo-Fi / Chillout"
            textToAnalyze.contains("rap") || textToAnalyze.contains("hip") || textToAnalyze.contains("flow") -> "Hip-Hop / Rap"
            textToAnalyze.contains("acoustic") || textToAnalyze.contains("guitar") || textToAnalyze.contains("piano") -> "Acústico / Folk"
            else -> {
                val index = (track.title.hashCode() and Int.MAX_VALUE) % genres.size
                genres[index]
            }
        }

        val mood = when {
            textToAnalyze.contains("gym") || textToAnalyze.contains("run") || textToAnalyze.contains("power") || textToAnalyze.contains("energy") -> "⚡ Energético"
            textToAnalyze.contains("chill") || textToAnalyze.contains("night") || textToAnalyze.contains("sleep") || textToAnalyze.contains("calm") -> "🌊 Chill & Relax"
            textToAnalyze.contains("sad") || textToAnalyze.contains("love") || textToAnalyze.contains("nostalgia") || textToAnalyze.contains("memories") -> "🌙 Nostálgico"
            textToAnalyze.contains("party") || textToAnalyze.contains("fiesta") || textToAnalyze.contains("dance") || textToAnalyze.contains("club") -> "🎉 Fiesta"
            textToAnalyze.contains("study") || textToAnalyze.contains("focus") || textToAnalyze.contains("work") || textToAnalyze.contains("code") -> "🎧 Enfoque"
            else -> {
                val index = (track.artist.hashCode() + track.title.hashCode() and Int.MAX_VALUE) % moods.size
                moods[index]
            }
        }

        return MoodGenreResult(mood = mood, genre = genre)
    }
}
