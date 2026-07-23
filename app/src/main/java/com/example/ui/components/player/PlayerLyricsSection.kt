package com.example.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextWhite

/**
 * Sección modular de letras sincronizadas estilo Spotify con funciones de IA.
 */
@Composable
fun PlayerLyricsSection(
    track: TrackEntity,
    currentPositionMs: Long,
    onUpdateLyrics: (String) -> Unit,
    isLyricsTranslatorEnabled: Boolean = true,
    onTranslateLyricsClick: (TrackEntity) -> Unit = {},
    onExplainLyricsClick: (TrackEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lyrics,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LETRAS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 14.sp
                    ),
                    color = SpotifyGreen
                )
            }

            if (isLyricsTranslatorEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = { onTranslateLyricsClick(track) },
                        label = { Text("Traducir", fontSize = 11.sp, color = SpotifyTextWhite) },
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(14.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SpotifyGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    )

                    AssistChip(
                        onClick = { onExplainLyricsClick(track) },
                        label = { Text("Explicar", fontSize = 11.sp, color = SpotifyTextWhite) },
                        leadingIcon = {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(14.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SpotifyGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        PlayerLyricsView(
            track = track,
            currentPositionMs = currentPositionMs,
            onUpdateLyrics = onUpdateLyrics,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )
    }
}
