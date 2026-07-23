package com.example.ui.components.player

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Psychology
import com.example.data.db.TrackEntity
import com.example.player.SleepTimerOption
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerAdvancedOptionsSheet(
    track: TrackEntity,
    playbackSpeed: Float,
    playbackPitch: Float,
    sleepTimerOption: SleepTimerOption = SleepTimerOption.OFF,
    sleepTimerRemainingMs: Long? = null,
    isEqEnabled: Boolean = true,
    bandGainsDb: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f),
    eqPreset: EqPreset = EqPreset.FLAT,
    is3dAudioEnabled: Boolean = true,
    audio3dStrength: Float = 0.8f,
    audio3dMode: com.example.player.Audio3dSpeakerMode = com.example.player.Audio3dSpeakerMode.DUAL_SPEAKER,
    reverbEnvironment: com.example.player.SpatialReverbEnvironment = com.example.player.SpatialReverbEnvironment.MEDIUM_HALL,
    reverbStrength: Float = 0.7f,
    crossfadeDurationSec: Float = 3.0f,
    isVolumeNormalizerEnabled: Boolean = true,
    targetLufs: Float = -14.0f,
    isVibeMatchEnabled: Boolean = true,
    onVibeMatchClick: (TrackEntity) -> Unit = {},
    onSpeedChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onOpenSleepTimer: () -> Unit = {},
    onOpenEditMetadata: () -> Unit = {},
    onEqEnabledToggle: (Boolean) -> Unit = {},
    onBandGainChange: (Int, Float) -> Unit = { _, _ -> },
    onPresetSelect: (EqPreset) -> Unit = {},
    onResetEq: () -> Unit = {},
    on3dAudioEnabledToggle: (Boolean) -> Unit = {},
    on3dStrengthChange: (Float) -> Unit = {},
    on3dModeSelect: (com.example.player.Audio3dSpeakerMode) -> Unit = {},
    onReverbEnvironmentSelect: (com.example.player.SpatialReverbEnvironment) -> Unit = {},
    onReverbStrengthChange: (Float) -> Unit = {},
    onCrossfadeDurationChange: (Float) -> Unit = {},
    onVolumeNormalizerToggle: (Boolean) -> Unit = {},
    onTargetLufsChange: (Float) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpotifyCardGrey,
        contentColor = SpotifyTextWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("player_advanced_options_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Opciones Avanzadas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                    Text(
                        text = "${track.title} • ${track.artist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            // Vibe-Match de la Canción Actual
            if (isVibeMatchEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onVibeMatchClick(track)
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = SpotifyGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "⚡ Vibe-Match de Canción",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyTextWhite
                                    )
                                )
                                Text(
                                    text = "Mezcla cola continua con canciones similares",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SpotifyGreen
                        ) {
                            Text(
                                text = "Iniciar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyBlack
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 1. Ecualizador Avanzado C++ & Rust
            PlayerEqualizerView(
                isEqEnabled = isEqEnabled,
                bandGainsDb = bandGainsDb,
                currentPreset = eqPreset,
                onEqEnabledToggle = onEqEnabledToggle,
                onBandGainChange = onBandGainChange,
                onPresetSelect = onPresetSelect,
                onResetEq = onResetEq
            )

            // 2. Mejora de Audio (Sonido 3D Espacial para 1 o 2 bocinas)
            PlayerAudio3dEnhancerView(
                is3dAudioEnabled = is3dAudioEnabled,
                audio3dStrength = audio3dStrength,
                audio3dMode = audio3dMode,
                reverbEnvironment = reverbEnvironment,
                reverbStrength = reverbStrength,
                on3dAudioEnabledToggle = on3dAudioEnabledToggle,
                on3dStrengthChange = on3dStrengthChange,
                on3dModeSelect = on3dModeSelect,
                onReverbEnvironmentSelect = onReverbEnvironmentSelect,
                onReverbStrengthChange = onReverbStrengthChange
            )

            // 3. Motor DSP (Velocidad y Tono)
            PlayerDspControls(
                playbackSpeed = playbackSpeed,
                playbackPitch = playbackPitch,
                onSpeedChange = onSpeedChange,
                onPitchChange = onPitchChange
            )

            // 4. Temporizador de Sueño Inteligente (Sleep Timer)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSleepTimer() },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Temporizador de Sueño",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyTextWhite
                                )
                            )
                            val timerText = if (sleepTimerOption == SleepTimerOption.OFF) {
                                "Desactivado"
                            } else if (sleepTimerRemainingMs != null && sleepTimerRemainingMs > 0) {
                                val min = (sleepTimerRemainingMs / 1000) / 60
                                val sec = (sleepTimerRemainingMs / 1000) % 60
                                "Activo: %02dm %02ds (Fade-Out)".format(min, sec)
                            } else {
                                sleepTimerOption.label
                            }
                            Text(
                                text = timerText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (sleepTimerOption != SleepTimerOption.OFF) SpotifyGreen else SpotifyTextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SpotifyGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Configurar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5. Editor Manual de Etiquetas ID3 & Portadas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEditMetadata() },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Editor de Etiquetas ID3 & Portada",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyTextWhite
                                )
                            )
                            Text(
                                text = "Modifica título, artista, álbum y carátula local",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyTextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SpotifyGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Editar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5. Normalización de Volumen Automática (EBU R128 / ReplayGain)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Normalización EBU R128",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyTextWhite
                                )
                            )
                            Text(
                                text = "Mantiene un volumen uniforme entre canciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpotifyTextMuted
                            )
                        }

                        androidx.compose.material3.Switch(
                            checked = isVolumeNormalizerEnabled,
                            onCheckedChange = onVolumeNormalizerToggle,
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = SpotifyCardGrey,
                                checkedTrackColor = SpotifyGreen
                            )
                        )
                    }

                    if (isVolumeNormalizerEnabled) {
                        Text(
                            text = "Nivel Objetivo: ${"%.1f".format(targetLufs)} LUFS",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyGreen
                        )
                        androidx.compose.material3.Slider(
                            value = targetLufs,
                            onValueChange = onTargetLufsChange,
                            valueRange = -24.0f..-8.0f,
                            colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = SpotifyGreen,
                                activeTrackColor = SpotifyGreen
                            )
                        )
                    }
                }
            }

            // 6. Fundido Cruzado (Crossfade)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fundido Cruzado (Crossfade)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = if (crossfadeDurationSec <= 0.2f) "Desactivado" else "${"%.1f".format(crossfadeDurationSec)} seg",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyGreen
                            )
                        )
                    }
                    Text(
                        text = "Transición suave de volumen entre el final y el inicio de la siguiente canción",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextMuted
                    )
                    androidx.compose.material3.Slider(
                        value = crossfadeDurationSec,
                        onValueChange = onCrossfadeDurationChange,
                        valueRange = 0.0f..12.0f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = SpotifyGreen,
                            activeTrackColor = SpotifyGreen
                        )
                    )
                }
            }

            // 5. Info del Archivo Audio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Detalles del Archivo",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ruta: ${track.uriString}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = SpotifyTextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Álbum: ${track.album}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SpotifyTextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

