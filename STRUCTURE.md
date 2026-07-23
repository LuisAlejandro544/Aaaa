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
│   │   │   │   │   │   ├── StemModelManager.kt   # Gestor y descargador asíncrono del modelo ONNX 18.5 MB desde Cloudflare Pages
│   │   │   │   │   │   └── OnnxInferenceRunner.kt# Ejecutor modular de inferencias ONNX Runtime v2 (Mobile-UNet 4-Stem HD 18.5MB)
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── TrackEntity.kt        # Entidad de canción importada
│   │   │   │   │   │   ├── PlaylistEntity.kt     # Entidad de lista de reproducción
│   │   │   │   │   │   ├── TrackDao.kt           # Data Access Object para canciones
│   │   │   │   │   │   ├── PlaylistDao.kt        # Data Access Object para playlists
│   │   │   │   │   │   └── AppDatabase.kt        # Base de datos Room SQLite
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MusicRepository.kt    # Facachada principal de repositorio
│   │   │   │   │   │   ├── PlaylistRepositoryDelegate.kt # Delegado modular para operaciones CRUD de playlists
│   │   │   │   │   │   └── LibraryExportHelper.kt# Ayudante modular para exportación de biblioteca a JSON
│   │   │   │   │   ├── storage/
│   │   │   │   │   │   ├── LocalStorageManager.kt # Fachada principal del gestor de almacenamiento en android/data/app/
│   │   │   │   │   │   ├── ImageStorageDelegate.kt# Delegado modular para conversión WebP y generación de portadas semilla
│   │   │   │   │   │   ├── JsonStorageDelegate.kt # Delegado modular para la sincronización del caché JSON
│   │   │   │   │   │   └── MediaStorageDelegate.kt# Delegado modular para videos Canvas y almacenamiento multimedia
│   │   │   │   │   ├── importer/
│   │   │   │   │   │   ├── AudioImporter.kt      # Módulo de importación y extracción de etiquetas
│   │   │   │   │   │   ├── UriMetadataHelper.kt  # Ayudante modular para resolución de nombres, tamaños y formatos URI
│   │   │   │   │   │   └── SampleAudioGenerator.kt # Generador de canciones demo e imágenes semilla WebP
│   │   │   │   │   └── rust/
│   │   │   │   │       ├── RustMetadataParser.kt # Puente Kotlin para el motor de parsing seguro en Rust
│   │   │   │   │       └── RustFuzzySearchEngine.kt # Motor de búsqueda difusa y distancia Levenshtein
│   │   │   │   ├── player/
│   │   │   │   │   ├── MusicPlayerManager.kt     # Gestor modular unificado de reproducción y delegados
│   │   │   │   │   ├── SpatialReverbEnvironment.kt # Enum de presets de Reverb 3D (Room, Hall, Cathedral, Stadium)
│   │   │   │   │   ├── controllers/              # Controladores modulares de reproducción
│   │   │   │   │   │   ├── QueueController.kt    # Controlador modular de colas, índices, shuffle, repetición y peekNextTrack
│   │   │   │   │   │   └── AudioEffectsController.kt # Controlador modular de ecualizador, audio 3D, reverb spatial, crossfade y normalización
│   │   │   │   │   ├── VolumeController.kt       # Controlador de volumen de sistema e intercepción de hardware
│   │   │   │   │   ├── VolumeNormalizerEngine.kt # Motor de análisis y normalización de volumen EBU R128 (LUFS)
│   │   │   │   │   ├── PlaybackState.kt          # Definición de modos de repetición y estados de parámetros
      │   │   │   │   ├── AudioDspEngine.kt         # Fachada del motor DSP para velocidad, pitch y efectos 3D
│   │   │   │   │   ├── AudioFxManager.kt         # Gestor modular de hardware AudioFX (Equalizer, Virtualizer, BassBoost, Reverb)
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
│   │   │   │   │   │   ├── library/
│   │   │   │   │   │   │   └── DuplicateDetectorModal.kt # Modal interactivo para escanear y eliminar duplicados acústicos
│   │   │   │   │   │   └── player/               # Módulos descomprimidos del reproductor pantalla completa
│   │   │   │   │   │       ├── CustomVolumePanelHUD.kt # Panel flotante HUD de volumen personalizado estilo Spotify
│   │   │   │   │   │       ├── PlayerTopBar.kt   # Barra superior de navegación con botón de letras y volumen
│   │   │   │   │   │       ├── PlayerAlbumArt.kt # Componente de portada de álbum
│   │   │   │   │   │       ├── PlayerScrollableContent.kt # Contenido central desglosado con carátula, controles, seekBar y letras
│   │   │   │   │   │       ├── PlayerLyricsView.kt # Vista de letras sincronizadas LRC con auto-scroll y editor
│   │   │   │   │   │       ├── PlayerTrackHeader.kt # Cabecera de título, artista y botón favorito
│   │   │   │   │   │       ├── PlayerSeekBar.kt  # Barra de progreso y tiempo de reproducción
│   │   │   │   │   │       ├── PlayerPlaybackControls.kt # Botones de reproducción (Play, Prev, Next, Shuffle, Repeat)
│   │   │   │   │   │       ├── PlayerDspControls.kt # Sliders de velocidad y tono DSP
│   │   │   │   │   │       ├── PlayerEqSheet.kt  # Hoja modal de Ecualizador Avanzado de 5 Bandas y Presets
│   │   │   │   │   │       ├── EqResponseCurveCanvas.kt # Gráfico Canvas de curva de respuesta en frecuencia Rust
│   │   │   │   │   │       ├── EqPreset.kt       # Presets de ecualización (Plano, Rock, Pop, Jazz, Bass Boost)
│   │   │   │   │   │       ├── PlayerStemSelector.kt # Selector e interfaz de faders de 4 Stems IA con descargador ONNX y Auto-Masterizador
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
- **`PlayerFullScreen.kt`**: Coordinador principal simplificado.
- **`PlayerScrollableContent.kt`**: Subcomponente UI modular para el contenido del reproductor (arte de álbum, badge canvas, cabecera de título, seekbar, controles de reproducción, letras).
- **`MainActivityFilePickerHelper.kt`**: Gestor modular de launchers y selectores de archivos (música, videos, portadas WebP, respaldo JSON, notificaciones).

### 3. Capa de Dominio y Audio (`player/` y `data/`)
- **`AudioDspEngine.kt`**: Fachada de procesamiento DSP.
- **`AudioFxManager.kt`**: Gestor modular de efectos Android hardware `Equalizer`, `Virtualizer`, `BassBoost` y `PresetReverb`.

### 4. Capa de Almacenamiento e Importación (`data/storage/` y `data/importer/`)
- **`LocalStorageManager.kt`**: Fachada principal con delegados:
  - `ImageStorageDelegate.kt`: Conversión lossless WebP de portadas e imágenes semilla.
  - `JsonStorageDelegate.kt`: Sincronización de metadatos `library_cache.json` y `track_{id}.json`.
  - `MediaStorageDelegate.kt`: Importación de videos Canvas y guardado multimedia.
- **`AudioImporter.kt`**: Fachada de importación con `UriMetadataHelper.kt` para la extracción de nombres, tamaños y tipo MIME.

### 5. Capa de Repositorio (`data/repository/`)
- **`MusicRepository.kt`**: Fachada unificada de datos respaldada por `PlaylistRepositoryDelegate.kt` y `LibraryExportHelper.kt`.
