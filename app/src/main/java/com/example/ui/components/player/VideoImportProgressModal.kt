package com.example.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun VideoImportProgressModal(
    progressText: String,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = {}) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .background(SpotifyCardGrey, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = SpotifyGreen,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Procesando Video Canvas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyTextWhite
                    )
                )
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SpotifyTextMuted,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
