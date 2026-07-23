package com.example.ui.components.player

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.player.RepeatMode
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyGreen

@Composable
fun PlayerScrollableContent(
    currentTrack: TrackEntity,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isUserScrubbing: Boolean,
    scrubPosition: Float,
    scrollState: ScrollState,
    onScrubChange: (Float) -> Unit,
    onScrubFinished: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onUpdateLyrics: (TrackEntity, String) -> Unit,
    onQueueClick: () -> Unit = {},
    onAudioSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Album Art / Canvas Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            PlayerAlbumArt(coverArtPath = currentTrack.coverArtPath)

            if (currentTrack.canvasVideoPath != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    color = SpotifyGreen,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = SpotifyBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "CANVAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyBlack,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Track Title & Artist Info
        PlayerTrackHeader(
            track = currentTrack,
            onToggleFavorite = onToggleFavorite
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seek Bar
        PlayerSeekBar(
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            isUserScrubbing = isUserScrubbing,
            scrubPosition = scrubPosition,
            onScrubChange = onScrubChange,
            onScrubFinished = onScrubFinished
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Playback Controls
        PlayerPlaybackControls(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            onTogglePlayPause = onTogglePlayPause,
            onNextTrack = onNextTrack,
            onPreviousTrack = onPreviousTrack,
            onToggleShuffle = onToggleShuffle,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Footer Badge
        PlayerFooterBadge(
            isSampleTrack = currentTrack.isSample,
            onQueueClick = onQueueClick,
            onAudioSettingsClick = onAudioSettingsClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Spotify-style Lyrics Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
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

        // Spotify-Style Inline Scrollable Lyrics Card
        PlayerLyricsView(
            track = currentTrack,
            currentPositionMs = currentPositionMs,
            onUpdateLyrics = { newLyrics -> onUpdateLyrics(currentTrack, newLyrics) },
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
