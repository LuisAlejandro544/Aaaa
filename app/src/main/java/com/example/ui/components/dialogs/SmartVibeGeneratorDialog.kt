package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartVibeGeneratorDialog(
    onGenerate: (vibePrompt: String) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    val vibePresetChips = listOf(
        "🌌 Noche Melancólica",
        "🔥 Fiesta & Bailar",
        "☕ Estudio & Focus",
        "⚡ Gimnasio Intenso",
        "🏖️ Playa & Chillout",
        "🚗 Roadtrip",
        "🌙 Lluvia & Relax"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SpotifyBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Generador de Listas Vibe",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = SpotifyTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Describe el estado de ánimo o ambiente que buscas y crearemos una lista personalizada con tus canciones locales.",
                style = MaterialTheme.typography.bodySmall,
                color = SpotifyTextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Ej. Canciones relajantes para una tarde lluviosa", color = SpotifyTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = SpotifyCardGrey,
                    unfocusedContainerColor = SpotifyCardGrey,
                    focusedTextColor = SpotifyTextWhite,
                    unfocusedTextColor = SpotifyTextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "O elige un Vibe popular:",
                style = MaterialTheme.typography.labelMedium,
                color = SpotifyGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vibePresetChips.forEach { chipText ->
                    val isSelected = prompt.trim() == chipText
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.clickable { prompt = chipText }
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isSelected) SpotifyBlack else SpotifyTextWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalPrompt = if (prompt.isBlank()) "Vibe Especial" else prompt.trim()
                    onGenerate(finalPrompt)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SpotifyBlack)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Generar Lista Vibe",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyBlack
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
