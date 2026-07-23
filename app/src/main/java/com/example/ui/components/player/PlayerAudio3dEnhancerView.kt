package com.example.ui.components.player

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.Audio3dSpeakerMode
import com.example.player.SpatialReverbEnvironment
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun PlayerAudio3dEnhancerView(
    is3dAudioEnabled: Boolean,
    audio3dStrength: Float,
    audio3dMode: Audio3dSpeakerMode,
    reverbEnvironment: SpatialReverbEnvironment = SpatialReverbEnvironment.MEDIUM_HALL,
    reverbStrength: Float = 0.7f,
    on3dAudioEnabledToggle: (Boolean) -> Unit,
    on3dStrengthChange: (Float) -> Unit,
    on3dModeSelect: (Audio3dSpeakerMode) -> Unit,
    onReverbEnvironmentSelect: (SpatialReverbEnvironment) -> Unit = {},
    onReverbStrengthChange: (Float) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_audio_3d_enhancer_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Bar + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (is3dAudioEnabled) SpotifyGreen.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.08f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejora de Audio 3D",
                            tint = if (is3dAudioEnabled) SpotifyGreen else SpotifyTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Mejora de Audio (3D Espacial)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = if (is3dAudioEnabled) "Inmersión 3D Activa" else "Desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (is3dAudioEnabled) SpotifyGreen else SpotifyTextMuted
                        )
                    }
                }

                Switch(
                    checked = is3dAudioEnabled,
                    onCheckedChange = on3dAudioEnabledToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SpotifyTextWhite,
                        checkedTrackColor = SpotifyGreen,
                        uncheckedThumbColor = SpotifyTextMuted,
                        uncheckedTrackColor = SpotifyCardGrey
                    )
                )
            }

            AnimatedVisibility(visible = is3dAudioEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Modo de Altavoces / Salida
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Configuración de Altavoces / Dispositivo",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyTextMuted
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Audio3dSpeakerMode.values().forEach { mode ->
                                val isSelected = audio3dMode == mode
                                val icon = when (mode) {
                                    Audio3dSpeakerMode.DUAL_SPEAKER -> Icons.Default.SpeakerGroup
                                    Audio3dSpeakerMode.SINGLE_SPEAKER -> Icons.Default.Speaker
                                    Audio3dSpeakerMode.HEADPHONES_3D -> Icons.Default.Headphones
                                }
                                val title = when (mode) {
                                    Audio3dSpeakerMode.DUAL_SPEAKER -> "2 Bocinas"
                                    Audio3dSpeakerMode.SINGLE_SPEAKER -> "1 Bocina"
                                    Audio3dSpeakerMode.HEADPHONES_3D -> "Auriculares"
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) SpotifyGreen.copy(alpha = 0.2f)
                                            else Color.White.copy(alpha = 0.04f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { on3dModeSelect(mode) }
                                        .padding(vertical = 12.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) SpotifyGreen else SpotifyTextMuted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) SpotifyTextWhite else SpotifyTextMuted,
                                                fontSize = 11.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Short description box for selected speaker mode
                        Text(
                            text = audio3dMode.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SpotifyTextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    // Slider de Intensidad de Espacialización 3D
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intensidad del Efecto 3D",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyTextMuted
                                )
                            )
                            Text(
                                text = "${(audio3dStrength * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyGreen
                                )
                            )
                        }

                        Slider(
                            value = audio3dStrength,
                            onValueChange = on3dStrengthChange,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = SpotifyGreen,
                                activeTrackColor = SpotifyGreen,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Simulador de Ambientes Reverb 3D (Spatial HRTF Audio)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Simulador de Ambientes Reverb 3D (Spatial HRTF)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyTextWhite
                                )
                            )
                        }

                        // Reverb Chips Horizontal Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(SpatialReverbEnvironment.values()) { env ->
                                val isSelected = reverbEnvironment == env

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { onReverbEnvironmentSelect(env) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = env.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) Color.Black else SpotifyTextWhite
                                    )
                                }
                            }
                        }

                        // Selected Reverb Description
                        Text(
                            text = reverbEnvironment.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SpotifyTextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    if (reverbEnvironment != SpatialReverbEnvironment.OFF) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Intensidad de Reverb HRTF",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SpotifyTextMuted
                                    )
                                )
                                Text(
                                    text = "${(reverbStrength * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                )
                            }
                            Slider(
                                value = reverbStrength,
                                onValueChange = onReverbStrengthChange,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = SpotifyGreen,
                                    activeTrackColor = SpotifyGreen,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

