package com.example.data.rust

import com.example.data.db.TrackEntity
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

/**
 * Motor de Búsqueda Difusa (Fuzzy Search Engine) optimizado con algoritmo Levenshtein.
 * Permite buscar canciones, artistas, álbumes y letras tolerando errores ortográficos y typos.
 */
object RustFuzzySearchEngine {

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas de texto.
     */
    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[len1][len2]
    }

    /**
     * Calcula el índice de similitud entre 0.0 (totalmente diferente) y 1.0 (idéntico).
     */
    fun computeSimilarity(s1: String, s2: String): Float {
        val norm1 = normalizeText(s1)
        val norm2 = normalizeText(s2)

        if (norm1.isEmpty() || norm2.isEmpty()) return 0f
        if (norm1 == norm2) return 1.0f
        if (norm1.contains(norm2) || norm2.contains(norm1)) return 0.85f

        val distance = computeLevenshteinDistance(norm1, norm2)
        val maxLen = max(norm1.length, norm2.length)
        if (maxLen == 0) return 1.0f

        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }

    /**
     * Normaliza el texto eliminando acentos, caracteres especiales y convirtiendo a minúsculas.
     */
    fun normalizeText(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()
    }

    /**
     * Filtra y ordena una lista de canciones según la consulta de búsqueda usando similitud difusa.
     */
    fun filterTracksFuzzy(
        tracks: List<TrackEntity>,
        query: String,
        threshold: Float = 0.35f
    ): List<TrackEntity> {
        val normQuery = normalizeText(query)
        if (normQuery.isBlank()) return tracks

        val queryTokens = normQuery.split("\\s+".toRegex())

        data class ScoredTrack(val track: TrackEntity, val score: Float)

        val scoredTracks = tracks.mapNotNull { track ->
            val normTitle = normalizeText(track.title)
            val normArtist = normalizeText(track.artist)
            val normAlbum = normalizeText(track.album)
            val normLyrics = track.lyrics?.let { normalizeText(it) } ?: ""

            // Combined exact substring match bonus
            if (normTitle.contains(normQuery) || normArtist.contains(normQuery) || normAlbum.contains(normQuery)) {
                return@mapNotNull ScoredTrack(track, 1.0f)
            }

            // Fuzzy matching score across query tokens
            var maxTokenScore = 0f

            for (token in queryTokens) {
                if (token.length < 2) continue

                val titleScore = computeSimilarity(normTitle, token)
                val artistScore = computeSimilarity(normArtist, token)
                val albumScore = computeSimilarity(normAlbum, token)
                val lyricsScore = if (normLyrics.contains(token)) 0.6f else 0.0f

                val bestForToken = maxOf(titleScore, artistScore, albumScore, lyricsScore)
                if (bestForToken > maxTokenScore) {
                    maxTokenScore = bestForToken
                }
            }

            // Overall string similarity match
            val fullTitleScore = computeSimilarity(normTitle, normQuery)
            val fullArtistScore = computeSimilarity(normArtist, normQuery)
            val finalScore = maxOf(maxTokenScore, fullTitleScore, fullArtistScore)

            if (finalScore >= threshold) {
                ScoredTrack(track, finalScore)
            } else {
                null
            }
        }

        return scoredTracks.sortedByDescending { it.score }.map { it.track }
    }
}
