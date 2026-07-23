# 📂 Estructura del Proyecto - SpotLocal

Este documento detalla la arquitectura de carpetas, capas de código y responsabilidades del proyecto SpotLocal.

---

## 🏗️ Árbol de Directorios Principal

```
SpotLocal/
├── .github/
│   └── workflows/
│       ├── apk-debug.yml                         # Workflow GitHub Action "apk debug" para generar APK Debug
│       └── ci-check.yml                          # Workflow GitHub Action "CI Check" para validación CI y tests unitarios
├── LICENSE                                       # Licencia de Código Visible pero No Comercial PolyForm Noncommercial 1.0.0
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Entrypoint principal, inicializa ViewModel, Launchers y delegación a Scaffold
│   │   │   │   ├── data/
│   │   │   │   │   ├── ai/
│   │   │   │   │   │   ├── StemSeparatorEngine.kt# Coordinador del estado de separación de 4-Stems (Voces, Batería, Bajo, Melodía) y Auto-Masterizador
│   │   │   │   │   │   ├── StemMode.kt           # Enums y data classes para los modos y sliders de ganancia de 4 Stems
│   │   │   │   │   │   ├── StemModelManager.kt   # Gestor y descargador asíncrono de los 4 modelos TFLite FP16 desde GitHub Release v1.0
│   │   │   │   │   │   ├── TfliteInferenceRunner.kt # Ejecutor de inferencias TensorFlow Lite delegando inspección a TfliteModelInspector
│   │   │   │   │   │   ├── TfliteModelInspector.kt # Inspector modular de verificación de estado y tamaño de los 4 modelos TFLite FP16
│   │   │   │   │   │   ├── AudioDecoderPipeline.kt # Fachada de decodificación de audio y espectrogramas STFT
│   │   │   │   │   │   ├── AudioMediaCodecDecoder.kt # Decodificador modular MediaCodec para extracción PCM multiformato
│   │   │   │   │   │   ├── StftSpectrogramCalculator.kt # Calculador modular de espectrogramas STFT con ventana Hann
│   │   │   │   │   │   └── OnnxInferenceRunner.kt# Ejecutor modular de inferencias ONNX Runtime v2 (Mobile-UNet 4-Stem HD 18.5MB)
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── TrackEntity.kt        # Entidad de canción importada
│   │   │   │   │   │   ├── PlaylistEntity.kt     # Entidad de lista de reproducción
│   │   │   │   │   │   ├── TrackDao.kt           # Data Access Object para canciones
│   │   │   │   │   │   ├── PlaylistDao.kt        # Data Access Object para playlists
│   │   │   │   │   │   └── AppDatabase.kt        # Base de datos Room SQLite
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MusicRepository.kt    # Fachada principal de repositorio
│   │   │   │   │   │   ├── PlaylistRepositoryDelegate.kt # Delegado modular para operaciones CRUD de playlists
│   │   │   │   │   │   └── LibraryExportHelper.kt# Ayudante modular para exportación de biblioteca a JSON
│   │   │   │   │   ├── storage/
│   │   │   │   │   │   ├── LocalStorageManager.kt # Fachada principal del gestor de almacenamiento en android/data/app/
│   │   │   │   │   │   ├── ImageStorageDelegate.kt# Delegado modular para conversión WebP y generación de portadas semilla
│   │   │   │   │   │   ├── JsonStorageDelegate.kt # Delegado modular para la sincronización del caché JSON
│   │   │   │   │   │   └── MediaStorageDelegate.kt# Delegado modular para videos Canvas y almacenamiento multimedia
│   │   │   │   │   ├── importer/
│   │   │   │   │   │   ├── AudioImporter.kt      # Fachada de importación delegando extracción de etiquetas
│   │   │   │   │   │   ├── TrackMetadataExtractor.kt # Extractor modular de metadatos ID3/Rust y carátulas WebP
│   │   │   │   │   │   ├── UriMetadataHelper.kt  # Ayudante modular para resolución de nombres, tamaños y formatos URI
│   │   │   │   │   │   └── SampleAudioGenerator.kt # Generador de canciones demo e imágenes semilla WebP
│   │   │   │   │   └── rust/
│   │   │   │   │       ├── RustMetadataParser.kt # Puente Kotlin para el motor de parsing seguro en Rust
│   │   │   │   │       └── RustFuzzySearchEngine.kt # Motor de búsqueda difusa y distancia Levenshtein
│   │   │   │   ├── player/
│   │   │   │   │   ├── MusicPlayerManager.kt     # Gestor modular unificado de reproducción y delegados
│   │   │   │   │   ├── SpatialReverbEnvironment.kt # Enum de presets de Reverb 3D (Room, Hall, Cathedral, Stadium)
│   │   │   │   │   ├── controllers/              # Controladores modulares de reproducción y audio
│   │   │   │   │   │   ├── QueueController.kt    # Controlador modular de colas, índices, shuffle, repetición y peekNextTrack
│   │   │   │   │   │   ├── AudioEffectsController.kt # Controlador modular de ecualizador, audio 3D, reverb spatial, crossfade y normalización
│   │   │   │   │   │   ├── EqualizerFxController.kt # Controlador modular de ecualizador de hardware y mapeo de bandas por Stem
│   │   │   │   │   │   ├── Spatial3dAudioFxController.kt # Controlador modular de efectos Virtualizer y BassBoost 3D
│   │   │   │   │   │   ├── ReverbFxController.kt # Controlador modular de reverberación espacial 3D HRTF
│   │   │   │   │   │   └── PlaybackParamsController.kt # Controlador modular de parámetros de velocidad y pitch
│   │   │   │   │   ├── VolumeController.kt       # Controlador de volumen de sistema e intercepción de hardware
│   │   │   │   │   ├── VolumeNormalizerEngine.kt # Motor de análisis y normalización de volumen EBU R128 (LUFS)
│   │   │   │   │   ├── PlaybackState.kt          # Definición de modos de repetición y estados de parámetros
│   │   │   │   │   ├── AudioDspEngine.kt         # Fachada del motor DSP para velocidad, pitch y efectos 3D
│   │   │   │   │   ├── AudioFxManager.kt         # Cooridnadador modular de hardware AudioFX
│   │   │   │   │   ├── Audio3dSpeakerMode.kt     # Enums de modo de bocinas (Single, Dual, Headphones)
│   │   │   │   │   ├── MediaNotificationManager.kt # Gestor de notificaciones MediaStyle en segundo plano
│   │   │   │   │   └── MusicPlaybackService.kt   # Servicio Foreground para reproducción continua con pantalla apagada
│   │   │   │   ├── util/
│   │   │   │   │   ├── AudioFingerprintEngine.kt# Motor de hashes acústicos y detección de duplicados
│   │   │   │   │   ├── Id3TagCleaner.kt          # Limpiador y auto-corrector inteligente de metadatos ID3
│   │   │   │   │   ├── DebugLogger.kt            # Registrador e interceptor de acciones, warnings y crashes
│   │   │   │   │   ├── LrcParser.kt              # Parser de letras sincronizadas [mm:ss.xx] LRC y texto plano
│   │   │   │   │   └── MainActivityFilePickerHelper.kt # Gestor modular de launchers y selectores de archivos en Activity
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── SpotLocalMainScaffold.kt # Scaffold modular principal con navegación y modales
│   │   │   │   │   │   ├── MiniPlayer.kt         # Reproductor en barra flotante
│   │   │   │   │   │   ├── TrackItem.kt          # Elemento individual de lista de canción
│   │   │   │   │   │   ├── SpotifyBottomNav.kt   # Barra de navegación inferior
│   │   │   │   │   │   ├── DebugLogConsoleModal.kt # Consola modal de logs en vivo para la APK Debug
│   │   │   │   │   │   ├── TrackOptionsDialog.kt # Modal de opciones de canción (Limpieza ID3, Portada, Favoritos)
│   │   │   │   │   │   ├── ImportExportDialog.kt # Diálogos de creación de listas e info
│   │   │   │   │   │   ├── dialogs/
│   │   │   │   │   │   │   ├── EditTrackMetadataDialog.kt # Modal interactivo para edición manual de etiquetas ID3 y portadas
│   │   │   │   │   │   │   └── SleepTimerSheet.kt    # Hoja modal para configuración de Temporizador de Sueño con Fade-Out
│   │   │   │   │   │   ├── library/
│   │   │   │   │   │   │   └── DuplicateDetectorModal.kt # Modal interactivo para escanear y eliminar duplicados acústicos
│   │   │   │   │   │   └── player/               # Módulos desglosados del reproductor pantalla completa
│   │   │   │   │   │       ├── CustomVolumePanelHUD.kt # Panel flotante HUD de volumen personalizado estilo Spotify
│   │   │   │   │   │       ├── PlayerBackgroundLayer.kt # Capa modular de fondo para video Canvas o gradientes
│   │   │   │   │   │       ├── PlayerTopBar.kt   # Barra superior de navegación con botón de letras y volumen
│   │   │   │   │   │       ├── PlayerHeaderArtSection.kt # Sección modular de carátula con indicador de Canvas
│   │   │   │   │   │       ├── PlayerAlbumArt.kt # Componente de portada de álbum
│   │   │   │   │   │       ├── PlayerScrollableContent.kt # Contenido central desglosado con carátula, controles, seekBar y letras
│   │   │   │   │   │       ├── PlayerLyricsSection.kt # Sección modular de cabecera e integración de letras
│   │   │   │   │   │       ├── PlayerLyricsView.kt # Vista de letras sincronizadas LRC con auto-scroll y editor
│   │   │   │   │   │       ├── PlayerTrackHeader.kt # Cabecera de título, artista y botón favorito
│   │   │   │   │   │       ├── PlayerSeekBar.kt  # Barra de progreso y tiempo de reproducción
│   │   │   │   │   │       ├── PlayerPlaybackControls.kt # Botones de reproducción (Play, Prev, Next, Shuffle, Repeat)
│   │   │   │   │   │       ├── PlayerDspControls.kt # Sliders de velocidad y tono DSP
│   │   │   │   │   │       ├── PlayerEqSheet.kt  # Hoja modal de Ecualizador Avanzado de 5 Bandas y Presets
│   │   │   │   │   │       ├── EqResponseCurveCanvas.kt # Gráfico Canvas de curva de respuesta en frecuencia Rust
│   │   │   │   │   │       ├── EqPreset.kt       # Presets de ecualización (Plano, Rock, Pop, Jazz, Bass Boost)
│   │   │   │   │   │       ├── PlayerAdvancedOptionsSheet.kt # Hoja de opciones avanzadas (Sleep Timer, ID3 Editor, Equalizer, 3D Audio, Crossfade)
│   │   │   │   │   │       ├── VideoImportProgressModal.kt # Modal flotante de progreso de importación de video Canvas
│   │   │   │   │   │       ├── PlayerAudio3dEnhancerView.kt # Componente de Mejora de Audio 3D e intensidad Reverb HRTF
│   │   │   │   │   │       ├── QueueModalSheet.kt # Modal interactivo de cola de reproducción estilo Lark con marcado automático
│   │   │   │   │   │       └── PlayerFooterBadge.kt # Badge de verificación de archivo local y cola
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt         # Pantalla principal e importación rápida
│   │   │   │   │   │   ├── SearchScreen.kt       # Búsqueda y categorías
│   │   │   │   │   │   ├── LibraryScreen.kt      # Biblioteca organizada con filtros
│   │   │   │   │   │   ├── PlaylistDetailScreen.kt # Pantalla independiente de detalle de playlist, carpetas y canciones favoritas
│   │   │   │   │   │   └── PlayerFullScreen.kt   # Reproductor expansivo coordinado por subcomponentes modulares
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   └── Theme.kt              # Paleta de colores Spotify
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       ├── PlayerViewModel.kt    # ViewModel central con StateFlows
│   │   │   │   │       └── delegates/
│   │   │   │   │           ├── NavigationDelegate.kt # Delegado modular de navegación y estados de reproductor
│   │   │   │   │           └── LibraryDelegate.kt    # Delegado modular de biblioteca, importaciones, tags e ID3
│   │   │   └── cpp/                               # Módulo nativo C++ / Oboe (Audio Engine)
│   │   │       ├── native-audio.cpp              # Implementación C++ JNI
│   │   │       └── CMakeLists.txt                # Configuración de compilación CMake NDK
│   │   ├── rust_core/                                # Módulo nativo Rust (Tag Parsing Engine)
│   │   │   ├── Cargo.toml                            # Configuración y dependencias Cargo (Lofty, JNI)
│   │   │   └── src/
│   │   │       └── lib.rs                            # Código Rust para parsing ultra-seguro de ID3, FLAC, OGG, WAV
│   │   ├── python_ai/                                # Módulo Python (AI Stem Separator / ONNX Export)
│   │   │   ├── model_config.py                       # Especificación de arquitectura y metadatos del modelo AI
│   │   │   ├── audio_processor.py                    # Funciones matemáticas de audio y ganancia de Stems
│   │   │   ├── stem_separator.py                     # Wrapper CLI modular para inferencia de separación de stems
│   │   │   └── export_onnx.py                        # Exporter modular para cuantizar modelos a ONNX
│   │   ├── gradle/
│   │   │   └── libs.versions.toml                    # Catálogo de versiones centralizado
│   │   ├── README.md                                 # Guía e introducción del proyecto
│   │   ├── ROADMAP.md                                # Planificación de fases y características
│   │   ├── STRUCTURE.md                              # Este archivo de arquitectura de archivos
│   │   ├── AI_CONTEXT.md                             # Manual contextual para asistentes de IA
│   │   └── AGENTS.md                                 # Instrucciones persistentes para agentes IA
```

---

## ⚙️ Descripción de Capas Refactorizadas

### 1. Capa CI/CD (`.github/workflows/`)
- `apk-debug.yml`: Workflow GitHub Action llamado `apk debug` para compilar y firmar automáticamente el APK Debug en cada cambio.
- `ci-check.yml`: Workflow GitHub Action para verificación de compilación y pruebas unitarias.

### 2. Capa de Presentación (`ui/` y `util/`)
- **`PlayerFullScreen.kt`**: Coordinador principal simplificado apoyado por `PlayerBackgroundLayer.kt`.
- **`PlayerScrollableContent.kt`**: Subcomponente UI modular desglosado en `PlayerHeaderArtSection.kt` y `PlayerLyricsSection.kt`.
- **`MainActivityFilePickerHelper.kt`**: Gestor modular de launchers y selectores de archivos.

### 3. Capa de Dominio y Audio (`player/` y `data/`)
- **`AudioDspEngine.kt`**: Fachada de procesamiento DSP integrada con `PlaybackParamsController.kt`.
- **`AudioFxManager.kt`**: Coordinador modular respaldado por `EqualizerFxController.kt`, `Spatial3dAudioFxController.kt` y `ReverbFxController.kt`.

### 4. Capa de Almacenamiento, Importación e IA (`data/storage/`, `data/importer/` y `data/ai/`)
- **`LocalStorageManager.kt`**: Fachada principal con delegados `ImageStorageDelegate.kt`, `JsonStorageDelegate.kt` y `MediaStorageDelegate.kt`.
- **`AudioImporter.kt`**: Fachada de importación con `TrackMetadataExtractor.kt` y `UriMetadataHelper.kt`.
- **`TfliteInferenceRunner.kt`**: Ejecutor TFLite respaldado por `TfliteModelInspector.kt`.
- **`AudioDecoderPipeline.kt`**: Pipeline de decodificación respaldado por `AudioMediaCodecDecoder.kt` y `StftSpectrogramCalculator.kt`.

### 5. Capa de Repositorio (`data/repository/`)
- **`MusicRepository.kt`**: Fachada unificada de datos respaldada por `PlaylistRepositoryDelegate.kt` y `LibraryExportHelper.kt`.
