package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiSettingsState
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun SettingsScreen(
    aiSettings: AiSettingsState,
    onToggleVibeGen: (Boolean) -> Unit,
    onToggleLyricsTranslator: (Boolean) -> Unit,
    onToggleVibeMatch: (Boolean) -> Unit,
    onUpdateCustomApiKey: (String) -> Unit = {},
    onSelectModel: (String) -> Unit = {},
    onCleanTags: () -> Unit,
    onScanDuplicates: () -> Unit,
    onExportLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var apiKeyText by remember(aiSettings.customApiKey) { mutableStateOf(aiSettings.customApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("settings_screen_column")
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Ajustes del Reproductor",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyTextWhite
                    )
                )
                Text(
                    text = "Personaliza tus funciones inteligentes e IA",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: FUNCIONES INTELIGENTES & IA
        Text(
            text = "FUNCIONES INTELIGENTES & IA",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = SpotifyGreen,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. Generador de Listas por Vibe
        AiSettingToggleCard(
            title = "Generador de Listas por Vibe",
            description = "Crea listas de reproducción personalizadas analizando el estado de ánimo, ritmo o tema que solicites.",
            icon = Icons.Default.AutoAwesome,
            isChecked = aiSettings.isVibePlaylistGeneratorEnabled,
            onCheckedChange = onToggleVibeGen,
            testTag = "toggle_vibe_playlist_gen"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Traductor y Explicador de Letras
        AiSettingToggleCard(
            title = "Traductor y Explicador de Letras",
            description = "Traduce versos en vivo en el reproductor y explica el significado profundo e historia detrás de la canción.",
            icon = Icons.Default.Language,
            isChecked = aiSettings.isLyricsTranslatorEnabled,
            onCheckedChange = onToggleLyricsTranslator,
            testTag = "toggle_lyrics_translator"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Vibe-Match
        AiSettingToggleCard(
            title = "Vibe-Match de Canciones",
            description = "Encuentra canciones de tu biblioteca con una energía y todo similar para generar mezclas continuas.",
            icon = Icons.Default.Psychology,
            isChecked = aiSettings.isVibeMatchEnabled,
            onCheckedChange = onToggleVibeMatch,
            testTag = "toggle_vibe_match"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CONFIGURACIÓN DE API KEY DE GEMINI / GOOGLE AI STUDIO
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpotifyCardGrey),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "API Key de Google AI Studio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Si deseas usar tu propia API key personal para llamadas sin límite, ingrésala aquí. Si se deja en blanco, la app usará la clave de sistema.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = {
                        apiKeyText = it
                        onUpdateCustomApiKey(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("AIzaSy...", color = SpotifyTextMuted) },
                    singleLine = true,
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar Key",
                                tint = SpotifyTextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = SpotifyTextWhite,
                        unfocusedTextColor = SpotifyTextWhite,
                        cursorColor = SpotifyGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. SELECTOR DE MODELO DE IA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpotifyCardGrey),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Modelo de IA para Música & Traducción",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Selecciona el motor de inteligencia artificial de Google AI Studio impulsado por la 'Lingo-Musicologist Skill':",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val models = listOf(
                    Triple("gemini-3.6-flash", "Gemini 3.6 Flash (Recomendado)", "Máxima velocidad, precisión y análisis musicológico profundo."),
                    Triple("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "Respuesta ultra ligera y menor consumo."),
                    Triple("gemma-4-26b-a4b-it", "Gemma 4 (26B Open Model)", "Modelo abierto optimizado para tareas de traducción y lenguaje.")
                )

                models.forEach { (modelId, modelTitle, modelDesc) ->
                    val isSelected = aiSettings.selectedModel == modelId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (isSelected) SpotifyGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SpotifyGreen else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectModel(modelId) }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectModel(modelId) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = SpotifyGreen,
                                    unselectedColor = SpotifyTextMuted
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = modelTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SpotifyGreen else SpotifyTextWhite
                                    )
                                )
                                Text(
                                    text = modelDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpotifyTextMuted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: HERRAMIENTAS DE BIBLIOTECA & MANTENIMIENTO
        Text(
            text = "HERRAMIENTAS DE BIBLIOTECA",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = SpotifyGreen,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpotifyCardGrey),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Limpiar Etiquetas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Limpiar Etiquetas ID3",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = "Elimina basurita de nombres de descarga (ej. [ytmp3.cc])",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                    Button(
                        onClick = onCleanTags,
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Limpiar", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Escanear Duplicados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detector de Duplicados",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = "Escanea huellas acústicas para eliminar canciones repetidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                    OutlinedButton(
                        onClick = onScanDuplicates,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = SpotifyGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear", fontSize = 12.sp, color = SpotifyTextWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 3: ESTADO DEL MOTOR DSP Y RUST NDK
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Motor NDK C++ Oboe & Rust Core",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                    Text(
                        text = "Procesamiento DSP de audio en tiempo real, Ecualizador Biquad 5 Bandas y Parser Rust Activo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun AiSettingToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) SpotifyCardGrey else Color.White.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isChecked) SpotifyGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked) SpotifyGreen else SpotifyTextMuted,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SpotifyTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SpotifyBlack,
                    checkedTrackColor = SpotifyGreen,
                    uncheckedThumbColor = SpotifyTextMuted,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
