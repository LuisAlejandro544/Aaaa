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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.ai.StemMode
import com.example.data.ai.StemSeparationState
import com.example.data.ai.StemSeparatorEngine
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun PlayerStemSelector(
    stemState: StemSeparationState,
    onStemModeSelected: (StemMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStemMixer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Separador IA 4-Stems v2",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                    Text(
                        text = "Aislamiento de voces e instrumentos ONNX HD",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = SpotifyGreen
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showStemMixer) SpotifyGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                    .clickable { showStemMixer = !showStemMixer }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Mixer",
                        tint = if (showStemMixer) SpotifyGreen else SpotifyTextWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showStemMixer) "Ocultar" else "Mezclador",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (showStemMixer) SpotifyGreen else SpotifyTextWhite,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Stem mode preset chips in a horizontal scrollable row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StemMode.entries.toTypedArray()) { mode ->
                val isSelected = stemState.currentStemMode == mode
                val label = when (mode) {
                    StemMode.ORIGINAL -> "Original"
                    StemMode.VOCALS -> "Solo Voces"
                    StemMode.DRUMS -> "Solo Batería"
                    StemMode.BASS -> "Solo Bajo"
                    StemMode.OTHER -> "Melodía"
                    StemMode.KARAOKE -> "Karaoke (Sin Voces)"
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f))
                        .clickable { onStemModeSelected(mode) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) Color.Black else SpotifyTextWhite
                    )
                }
            }
        }

        // Expandable Interactive 4-Stem Fader Sliders & AI Masterizer Button
        AnimatedVisibility(visible = showStemMixer) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
            val modelStatus by com.example.data.ai.StemModelManager.status.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                com.example.data.ai.StemModelManager.checkLocalModel(context)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cloudflare Pages 18.5 MB ONNX Model Download status card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (modelStatus.isDownloaded) SpotifyGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f))
                        .border(1.dp, if (modelStatus.isDownloaded) SpotifyGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (modelStatus.isDownloaded) "Modelo ONNX HD (18.5 MB) Instalado" else "Modelo ONNX HD (18.5 MB) Servidor",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (modelStatus.isDownloaded) SpotifyGreen else SpotifyTextWhite
                                )
                            )
                            if (modelStatus.isDownloading) {
                                Text(
                                    text = "${modelStatus.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SpotifyGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Text(
                            text = modelStatus.statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = SpotifyTextMuted
                            )
                        )

                        if (!modelStatus.isDownloaded || modelStatus.isDownloading) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        com.example.data.ai.StemModelManager.downloadModelFromUrl(context)
                                    }
                                },
                                enabled = !modelStatus.isDownloading,
                                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (modelStatus.isDownloading) "Descargando..." else "Descargar Modelo (18.5 MB) desde Cloudflare",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Mezclador Aislado en Tiempo Real:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SpotifyTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                StemGainSlider(
                    label = "Voces",
                    valueDb = stemState.vocalGainDb,
                    onValueChange = { db ->
                        StemSeparatorEngine.setIndividualStemGains(db, stemState.drumsGainDb, stemState.bassGainDb, stemState.otherGainDb)
                    }
                )

                StemGainSlider(
                    label = "Batería / Percusión",
                    valueDb = stemState.drumsGainDb,
                    onValueChange = { db ->
                        StemSeparatorEngine.setIndividualStemGains(stemState.vocalGainDb, db, stemState.bassGainDb, stemState.otherGainDb)
                    }
                )

                StemGainSlider(
                    label = "Bajo / Sub-Bass",
                    valueDb = stemState.bassGainDb,
                    onValueChange = { db ->
                        StemSeparatorEngine.setIndividualStemGains(stemState.vocalGainDb, stemState.drumsGainDb, db, stemState.otherGainDb)
                    }
                )

                StemGainSlider(
                    label = "Melodía / Instrumentos",
                    valueDb = stemState.otherGainDb,
                    onValueChange = { db ->
                        StemSeparatorEngine.setIndividualStemGains(stemState.vocalGainDb, stemState.drumsGainDb, stemState.bassGainDb, db)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        StemSeparatorEngine.runAiAutoMasterizer("Pista Activa")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Master",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stemState.isAutoMastered) "IA Auto-Masterizado Activo" else "Auto-Masterizar Pista con IA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                stemState.aiMasteringNote?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SpotifyGreen,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StemGainSlider(
    label: String,
    valueDb: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(color = SpotifyTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${if (valueDb >= 0) "+" else ""}${String.format("%.1f", valueDb)} dB",
                style = MaterialTheme.typography.labelSmall.copy(color = SpotifyGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            )
        }
        Slider(
            value = valueDb,
            onValueChange = onValueChange,
            valueRange = -24f..12f,
            colors = SliderDefaults.colors(
                thumbColor = SpotifyGreen,
                activeTrackColor = SpotifyGreen,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

