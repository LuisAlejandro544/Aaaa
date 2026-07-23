package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.TrackEntity
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsDialog(
    track: TrackEntity,
    onDismiss: () -> Unit,
    onFavoriteToggle: (TrackEntity) -> Unit,
    onCleanTags: ((TrackEntity) -> Unit)? = null,
    onClassifyMoodAndGenre: ((TrackEntity) -> Unit)? = null,
    onDeleteTrack: (TrackEntity) -> Unit,
    onPickCustomCover: (TrackEntity) -> Unit,
    onPickCanvasVideo: ((TrackEntity) -> Unit)? = null,
    onRemoveCanvasVideo: ((TrackEntity) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpotifyCardGrey,
        contentColor = SpotifyTextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("track_options_dialog"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyTextWhite
                    )
                )
                Text(
                    text = "${track.artist}${if (!track.mood.isNullOrBlank()) " • ${track.mood}" else ""}${if (!track.genre.isNullOrBlank()) " [${track.genre}]" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    OptionRow(
                        icon = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        title = if (track.isFavorite) "Quitar de Favoritos" else "Agregar a Favoritos",
                        tint = if (track.isFavorite) SpotifyGreen else SpotifyTextWhite,
                        onClick = {
                            onFavoriteToggle(track)
                            onDismiss()
                        }
                    )

                    if (onClassifyMoodAndGenre != null) {
                        OptionRow(
                            icon = Icons.Default.Psychology,
                            title = "Clasificar Mood y Género con IA",
                            tint = SpotifyGreen,
                            onClick = {
                                onClassifyMoodAndGenre(track)
                                onDismiss()
                            }
                        )
                    }

                    if (onCleanTags != null) {
                        OptionRow(
                            icon = Icons.Default.AutoFixHigh,
                            title = "Limpiar y Corregir Etiquetas ID3",
                            tint = SpotifyGreen,
                            onClick = {
                                onCleanTags(track)
                                onDismiss()
                            }
                        )
                    }

                    OptionRow(
                        icon = Icons.Default.Image,
                        title = "Cambiar Portada Personalizada",
                        tint = SpotifyTextWhite,
                        onClick = {
                            onPickCustomCover(track)
                            onDismiss()
                        }
                    )

                    if (onPickCanvasVideo != null) {
                        OptionRow(
                            icon = Icons.Default.VideoLibrary,
                            title = if (track.canvasVideoPath != null) "Cambiar Canvas Video (Fondo)" else "Asignar Canvas Video (Fondo)",
                            tint = SpotifyGreen,
                            onClick = {
                                onPickCanvasVideo(track)
                                onDismiss()
                            }
                        )
                    }

                    if (track.canvasVideoPath != null && onRemoveCanvasVideo != null) {
                        OptionRow(
                            icon = Icons.Default.VideocamOff,
                            title = "Quitar Canvas Video",
                            tint = SpotifyTextMuted,
                            onClick = {
                                onRemoveCanvasVideo(track)
                                onDismiss()
                            }
                        )
                    }

                    OptionRow(
                        icon = Icons.Default.Delete,
                        title = "Eliminar de la Biblioteca",
                        tint = Color(0xFFEF5350),
                        onClick = {
                            onDeleteTrack(track)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        )
    }
}
