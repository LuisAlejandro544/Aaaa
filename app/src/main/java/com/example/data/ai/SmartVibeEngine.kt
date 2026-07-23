package com.example.data.ai

import com.example.data.db.TrackEntity
import com.example.data.rust.RustFuzzySearchEngine
import kotlin.math.abs

object SmartVibeEngine {

    /**
     * Genera una lista de reproducción de la biblioteca local que encaja con el "Vibe" o mood solicitado.
     */
    fun generateVibePlaylist(
        vibePrompt: String,
        allTracks: List<TrackEntity>,
        maxTracks: Int = 12
    ): Pair<String, List<TrackEntity>> {
        if (allTracks.isEmpty()) return Pair("Lista Vibe Vacía", emptyList())

        val promptLower = vibePrompt.lowercase().trim()

        // Deteremine Playlist Name
        val playlistName = when {
            promptLower.contains("noche") || promptLower.contains("lluvia") -> "🌌 Vibe Noche & Lluvia"
            promptLower.contains("fiesta") || promptLower.contains("bailar") -> "🔥 Vibe Fiesta Intensa"
            promptLower.contains("estudio") || promptLower.contains("focus") -> "☕ Vibe Estudio & Chill"
            promptLower.contains("gimnasio") || promptLower.contains("entreno") || promptLower.contains("fit") -> "⚡ Vibe Gym & Power"
            promptLower.contains("playa") || promptLower.contains("sol") -> "🏖️ Vibe Playa & Relax"
            promptLower.contains("chill") || promptLower.contains("relax") -> "🌿 Vibe Soft Chillout"
            promptLower.contains("rock") -> "🎸 Vibe Rock & Energia"
            promptLower.contains("pop") -> "✨ Vibe Pop Hits"
            else -> "✨ Vibe Especial: ${vibePrompt.take(20).capitalize()}"
        }

        // Score tracks based on match keywords, genre, mood, and title fuzzy distance
        val scoredTracks = allTracks.map { track ->
            var score = 0
            val titleL = track.title.lowercase()
            val artistL = track.artist.lowercase()
            val genreL = (track.genre ?: "").lowercase()
            val moodL = (track.mood ?: "").lowercase()

            if (promptLower.contains("noche") || promptLower.contains("chill") || promptLower.contains("relax") || promptLower.contains("lluvia")) {
                if (moodL.contains("relajante") || moodL.contains("chill") || moodL.contains("soft") || moodL.contains("melancólico")) score += 5
                if (genreL.contains("lo-fi") || genreL.contains("acústico") || genreL.contains("ambient")) score += 4
                if (titleL.contains("chill") || titleL.contains("rain") || titleL.contains("noche") || titleL.contains("brisa")) score += 3
            } else if (promptLower.contains("fiesta") || promptLower.contains("gym") || promptLower.contains("power") || promptLower.contains("energía")) {
                if (moodL.contains("energético") || moodL.contains("alegre") || moodL.contains("power")) score += 5
                if (genreL.contains("rock") || genreL.contains("pop") || genreL.contains("electro")) score += 4
                if (titleL.contains("dance") || titleL.contains("rock") || titleL.contains("fire") || titleL.contains("power")) score += 3
            } else {
                // General keyword / fuzzy matching
                if (titleL.contains(promptLower) || artistL.contains(promptLower) || genreL.contains(promptLower)) score += 4
                val fuzzyTitleSim = RustFuzzySearchEngine.computeSimilarity(vibePrompt, track.title)
                score += (fuzzyTitleSim * 3).toInt()
            }

            // Small tie-breaker for favorite tracks
            if (track.isFavorite) score += 1

            Pair(track, score)
        }

        val sortedTracks = scoredTracks.sortedByDescending { it.second }.map { it.first }
        val selected = sortedTracks.take(maxTracks.coerceAtMost(allTracks.size))

        return Pair(playlistName, selected)
    }

    /**
     * Vibe-Match: Encuentra canciones de la biblioteca local con energía y mood similar a la canción dada.
     */
    fun findVibeMatches(
        sourceTrack: TrackEntity,
        allTracks: List<TrackEntity>,
        limit: Int = 10
    ): List<TrackEntity> {
        val candidates = allTracks.filter { it.id != sourceTrack.id }
        if (candidates.isEmpty()) return listOf(sourceTrack)

        val srcGenre = (sourceTrack.genre ?: "").lowercase()
        val srcMood = (sourceTrack.mood ?: "").lowercase()
        val srcArtist = sourceTrack.artist.lowercase()

        val scored = candidates.map { track ->
            var score = 0
            val trGenre = (track.genre ?: "").lowercase()
            val trMood = (track.mood ?: "").lowercase()
            val trArtist = track.artist.lowercase()

            // Match genre & mood
            if (srcGenre.isNotBlank() && trGenre == srcGenre) score += 5
            if (srcMood.isNotBlank() && trMood == srcMood) score += 5
            if (srcArtist.isNotBlank() && trArtist == srcArtist) score += 3

            // Duration similarity (songs with similar length often share vibe)
            val durDiffSec = abs(sourceTrack.durationMs - track.durationMs) / 1000
            if (durDiffSec < 30) score += 2

            // Title fuzzy rhythm similarity
            val fuzzySim = RustFuzzySearchEngine.computeSimilarity(sourceTrack.title, track.title)
            score += (fuzzySim * 2).toInt()

            Pair(track, score)
        }

        val sorted = scored.sortedByDescending { it.second }.map { it.first }
        val result = mutableListOf(sourceTrack)
        result.addAll(sorted.take(limit - 1))
        return result
    }
}
