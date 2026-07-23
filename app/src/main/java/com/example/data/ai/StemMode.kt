package com.example.data.ai

enum class StemMode {
    ORIGINAL,       // Audio completo original sin separar
    VOCALS,         // Solo la pista vocal (Vocal Isolation)
    DRUMS,          // Solo batería y percusión (Percussion Isolation)
    BASS,           // Solo bajo eléctrico / sintetizado (Low Frequency Bass)
    OTHER,          // Melodías, teclados y guitarras (Instrumental Melody)
    KARAOKE         // Pista instrumental completa con voces atenuadas
}

data class StemSeparationState(
    val isProcessing: Boolean = false,
    val progressPercent: Int = 0,
    val currentStemMode: StemMode = StemMode.ORIGINAL,
    val vocalGainDb: Float = 0f,
    val drumsGainDb: Float = 0f,
    val bassGainDb: Float = 0f,
    val otherGainDb: Float = 0f,
    val modelName: String = "Mobile-U-Net 4-Stem HD Engine v2 (PyTorch/ONNX-v2 18.5MB)",
    val isAutoMastered: Boolean = false,
    val aiMasteringNote: String? = null
)
