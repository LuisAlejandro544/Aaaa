package com.example.ui.components.player

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.SleepTimerOption
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    currentOption: SleepTimerOption,
    remainingMs: Long?,
    onSelectOption: (SleepTimerOption, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("20") }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("sleep_timer_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Temporizador de Sueño",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = "Incluye Fade-Out suave de volumen al finalizar",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = SpotifyTextMuted
                    )
                }
            }

            // Remaining Time Banner
            if (remainingMs != null && remainingMs > 0) {
                val totalSec = remainingMs / 1000
                val min = totalSec / 60
                val sec = totalSec % 60
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpotifyGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temporizador Activo",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SpotifyGreen
                            )
                            Text(
                                text = String.format("%02d min %02d seg restantes", min, sec),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SpotifyTextWhite
                            )
                        }
                        Button(
                            onClick = { onSelectOption(SleepTimerOption.OFF, null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Preset Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column {
                    val options = listOf(
                        SleepTimerOption.OFF,
                        SleepTimerOption.MIN_5,
                        SleepTimerOption.MIN_10,
                        SleepTimerOption.MIN_15,
                        SleepTimerOption.MIN_30,
                        SleepTimerOption.MIN_45,
                        SleepTimerOption.MIN_60,
                        SleepTimerOption.FINISH_TRACK
                    )

                    options.forEach { option ->
                        val isSelected = currentOption == option && !showCustomInput
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showCustomInput = false
                                    onSelectOption(option, null)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SpotifyGreen else SpotifyTextWhite
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Custom minutes row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomInput = !showCustomInput }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tiempo personalizado...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (showCustomInput) FontWeight.Bold else FontWeight.Medium,
                                color = if (showCustomInput) SpotifyGreen else SpotifyTextWhite
                            )
                        )
                    }

                    if (showCustomInput) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = customMinutesText,
                                onValueChange = { customMinutesText = it.filter { char -> char.isDigit() } },
                                label = { Text("Minutos", color = SpotifyTextMuted) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SpotifyGreen,
                                    unfocusedBorderColor = SpotifyTextMuted,
                                    focusedTextColor = SpotifyTextWhite,
                                    unfocusedTextColor = SpotifyTextWhite
                                )
                            )

                            Button(
                                onClick = {
                                    val mins = customMinutesText.toIntOrNull() ?: 15
                                    onSelectOption(SleepTimerOption.CUSTOM, mins)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Activar", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
