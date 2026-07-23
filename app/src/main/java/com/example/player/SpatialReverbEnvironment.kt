package com.example.player

import android.media.audiofx.PresetReverb

enum class SpatialReverbEnvironment(
    val id: String,
    val displayName: String,
    val description: String,
    val presetValue: Short
) {
    OFF(
        id = "off",
        displayName = "Desactivado",
        description = "Sonido directo de estudio sin respuesta de sala",
        presetValue = PresetReverb.PRESET_NONE
    ),
    SMALL_ROOM(
        id = "small_room",
        displayName = "Estudio Pequeño (HRTF)",
        description = "Acústica limpia e íntima de estudio de grabación",
        presetValue = PresetReverb.PRESET_SMALLROOM
    ),
    MEDIUM_HALL(
        id = "medium_hall",
        displayName = "Sala de Conciertos 3D",
        description = "Reflejos estéreo cálidos con percepción de espacio amplio",
        presetValue = PresetReverb.PRESET_MEDIUMHALL
    ),
    LARGE_HALL(
        id = "large_hall",
        displayName = "Gran Auditorio Espacial",
        description = "Amplitud envolvente de anfiteatro con decaimiento prolongado",
        presetValue = PresetReverb.PRESET_LARGEHALL
    ),
    CATHEDRAL_SPATIAL(
        id = "cathedral",
        displayName = "Catedral HRTF 3D",
        description = "Eco tridimensional expansivo con simulación acústica de placa",
        presetValue = PresetReverb.PRESET_PLATE
    ),
    STADIUM_ARENA(
        id = "stadium",
        displayName = "Estadio al Aire Libre",
        description = "Inmersión espacial masiva con dispersión HRTF envolvente",
        presetValue = PresetReverb.PRESET_LARGEROOM
    )
}
