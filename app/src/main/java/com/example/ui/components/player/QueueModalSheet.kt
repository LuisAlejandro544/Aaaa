package com.example.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.player.RepeatMode
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyDarkSlate
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueModalSheet(
    queue: List<TrackEntity>,
    currentIndex: Int,
    currentTrack: TrackEntity?,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onMoveQueueItem: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate next track automatically
    val upcomingTrack: TrackEntity? = rememberUpcomingTrack(queue, currentIndex, isShuffle, repeatMode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpotifyDarkSlate,
        contentColor = SpotifyTextWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("queue_modal_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SpotifyGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Cola de Reproducción",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = "${queue.size} canciones en lista",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Shuffle Toggle Button
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) SpotifyGreen else SpotifyTextMuted
                        )
                    }

                    // Repeat Toggle Button
                    IconButton(onClick = onToggleRepeat) {
                        val icon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repetición",
                            tint = if (repeatMode != RepeatMode.NONE) SpotifyGreen else SpotifyTextMuted
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = SpotifyTextMuted
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: En Reproducción
                item {
                    Text(
                        text = "EN REPRODUCCIÓN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = SpotifyGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (currentTrack != null) {
                        QueueTrackCard(
                            track = currentTrack,
                            badgeText = "SONANDO AHORA",
                            badgeColor = SpotifyGreen,
                            badgeTextColor = SpotifyBlack,
                            isCurrentTrack = true,
                            onPlayClick = {}
                        )
                    } else {
                        Text(
                            text = "No hay ninguna canción reproduciéndose",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                }

                // Section 2: A Continuación (Automático)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "A CONTINUACIÓN ${if (isShuffle) "(ALEATORIO)" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = SpotifyGreen
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (upcomingTrack != null) {
                        val upcomingIndex = queue.indexOf(upcomingTrack)
                        QueueTrackCard(
                            track = upcomingTrack,
                            badgeText = "SIGUIENTE PISTA",
                            badgeColor = Color(0xFF1E88E5),
                            badgeTextColor = SpotifyTextWhite,
                            isCurrentTrack = false,
                            onPlayClick = {
                                if (upcomingIndex != -1) onPlayQueueIndex(upcomingIndex)
                            }
                        )
                    } else {
                        Text(
                            text = "Fin de la cola de reproducción",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                }

                // Section 3: Resto de la Cola
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RESTO DE LA COLA DE ESPERA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = SpotifyTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                itemsIndexed(queue) { index, track ->
                    val isPlayingNow = index == currentIndex
                    val isNextUp = track.id == upcomingTrack?.id && !isPlayingNow

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPlayingNow) SpotifyGreen.copy(alpha = 0.12f)
                                else if (isNextUp) Color(0xFF1E88E5).copy(alpha = 0.12f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                width = if (isPlayingNow) 1.5.dp else 1.dp,
                                color = if (isPlayingNow) SpotifyGreen else if (isNextUp) Color(0xFF1E88E5) else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onPlayQueueIndex(index) },
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPlayingNow) SpotifyGreen else SpotifyTextMuted
                                ),
                                modifier = Modifier.width(24.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPlayingNow) SpotifyGreen else SpotifyTextWhite
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.album}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isPlayingNow) {
                                Icon(
                                    imageVector = Icons.Default.Equalizer,
                                    contentDescription = "Reproduciendo",
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (index > 0) {
                                        IconButton(
                                            onClick = { onMoveQueueItem(index, index - 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Subir",
                                                tint = SpotifyTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (index < queue.lastIndex) {
                                        IconButton(
                                            onClick = { onMoveQueueItem(index, index + 1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Bajar",
                                                tint = SpotifyTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemoveFromQueue(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar de la cola",
                                            tint = SpotifyTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueTrackCard(
    track: TrackEntity,
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    isCurrentTrack: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = badgeColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isCurrentTrack, onClick = onPlayClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SpotifyCardGrey, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrentTrack) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyTextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun rememberUpcomingTrack(
    queue: List<TrackEntity>,
    currentIndex: Int,
    isShuffle: Boolean,
    repeatMode: RepeatMode
): TrackEntity? {
    if (queue.isEmpty()) return null
    if (repeatMode == RepeatMode.ONE) {
        return queue.getOrNull(currentIndex)
    }
    if (isShuffle) {
        val validIndices = queue.indices.filter { it != currentIndex }
        if (validIndices.isNotEmpty()) {
            return queue.getOrNull(validIndices.first())
        }
    }
    val nextIdx = currentIndex + 1
    return if (nextIdx < queue.size) {
        queue[nextIdx]
    } else if (repeatMode == RepeatMode.ALL) {
        queue.firstOrNull()
    } else {
        null
    }
}
