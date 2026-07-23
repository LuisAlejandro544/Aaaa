# AGENTS.md - Instrucciones Persistentes para Agentes IA

Consulte siempre los archivos de documentación:
- `README.md`
- `ROADMAP.md`
- `STRUCTURE.md`
- `AI_CONTEXT.md`

## Reglas del Stack
1. **Kotlin**: Jetpack Compose, Room DB, ViewModels, StateFlow, `DebugLogger` (registrador de acciones, advertencias y excepciones).
2. **C++**: Motor nativo NDK con Oboe para procesamiento DSP de audio en tiempo real.
3. **Rust**: Módulo nativo `rust_core/` para parsing ultra-seguro y rápido de metadatos (ID3, FLAC, OGG, WAV).
4. **Python**: Módulo `python_ai/` para exportación y ejecución de IA de separación de audio (Stems: Voces e Instrumental) en ONNX.
5. **GitHub Actions**: Workflows optimizados con filtro inteligente de archivos (`paths-ignore` para `.md`): `apk debug` en `.github/workflows/apk-debug.yml` (ensamblado de APK) y `CI Check` en `.github/workflows/ci-check.yml` (verificación de código y pruebas unitarias).

## Reglas de Arquitectura
- Conservar el diseño oscuro estilo Spotify (`#121212`, `#1DB954`).
- Controles DSP de Velocidad y Tono (Pitch) siempre sincronizados mediante `MusicPlayerManager`.
- Selector de Stems (Original, Voces, Instrumental, Karaoke) gestionado mediante `StemSeparatorEngine`.
- Soporte para letras sincronizadas (formato LRC `[mm:ss.xx]` y texto) con desplazamiento automático y resaltado activo en `PlayerLyricsView` parseado por `LrcParser`.
- Pantallas independientes de playlists, carpetas y canciones favoritas navegables mediante `PlaylistDetailScreen`.
- Menú contextual desplegable por pulsación larga (`combinedClickable`) en canciones (`TrackItem`).
- Sincronización en tiempo real del estado de favorito en el reproductor a tamaño completo, mini reproductor y listas.
- Consola de logs debug (`DebugLogConsoleModal`) activa en compilaciones `BuildConfig.DEBUG` con filtrado de Acciones, Warnings y Crashes.
- Filtrado anti-basura: sólo se añaden audios explícitamente importados por el usuario.
- Toda importación de audios utiliza `RustMetadataParser` para validar etiquetas de forma libre de desbordamientos de memoria.
- Estructura de almacenamiento limpia en `android/data/nuestraapp/files/`: subcarpetas `music/`, `images/` (carátulas WebP máxima compresión) y `json/` (`library_cache.json` y `track_{id}.json`).
- Al desinstalar la aplicación, el sistema Android elimina automáticamente todo el contenido de `android/data/nuestraapp/files/`, dejando cero archivos huérfanos o residuos en el almacenamiento del usuario.
- Si una canción no trae carátula, se genera automáticamente una portada artística usando colores e iniciales derivados por semilla de metadatos.
- La conversión a WebP de carátulas personalizadas o importadas se realiza obligatoriamente en un hilo de fondo (`Dispatchers.IO`) para mantener la interfaz fluida.
- Soporte para "Abrir con..." desde exploradores de archivos externos (`ACTION_VIEW` / `ACTION_SEND` para audio) que importa y reproduce la canción de inmediato.
- Notificación de medios nativa de Android (`MediaStyle` + `MediaSessionCompat`) para controlar la reproducción en segundo plano y pantalla de bloqueo.
- Control de "Mejora de Audio 3D" (`Audio3dSpeakerMode`) con modos de optimización para 1 bocina (Single), 2 bocinas (Stereo) y audífonos con regulador de intensidad en `PlayerAudio3dEnhancerView`.
- Detector de canciones duplicadas mediante huella acústica (`AudioFingerprintEngine`) y modal interactivo de gestión `DuplicateDetectorModal`.
- Limpiador y auto-corrector de etiquetas ID3 (`Id3TagCleaner`) para eliminar basurita de sitios de descarga y corregir títulos/artistas.
- Normalización automática de volumen EBU R128 (`VolumeNormalizerEngine`) a -14 LUFS objetivo en `MusicPlayerManager`.
- Fundido cruzado (Crossfade) de 0 a 10 segundos configurado en opciones avanzadas y gestionado por corrutinas en `MusicPlayerManager`.
- Panel flotante de volumen personalizado (`CustomVolumePanelHUD`) con `VolumeController` que intercepta las teclas del hardware en `MainActivity` para mostrar una barra estética con gradiente, ondas de nivel y accesos directos.
- Motor de Búsqueda Difusa (`RustFuzzySearchEngine`) con algoritmo Levenshtein para sugerencias e inspección aproximada en tiempo real.
- Extraer e inspeccionar marcos de carátula embebidos APIC / PICTURE prioritariamente vía `RustMetadataParser` en `AudioImporter`.
- Importación de Video a Música + Canvas (`importVideoAsTrackAndCanvas`) en hilo secundario (`Dispatchers.IO`) con buffers de 64KB y modal flotante de progreso (`VideoImportProgressModal`) para procesar videos largos MP4/MKV como pistas con carátula WebP y video Canvas de fondo sincronizado.
- Descargador del Modelo IA 4-Stem ONNX HD (18.5 MB) gestionado por `StemModelManager` para descargar y actualizar el archivo `.onnx` de forma asíncrona desde un servidor CDN de Cloudflare Pages.
- Cola de reproducción interactiva estilo Lark (`QueueController` y `QueueModalSheet`) con marcado automático de pista siguiente ("A CONTINUACIÓN"), reordenamiento manual y eliminación de canciones.
- Simulador de Ambientes Reverb 3D (Spatial HRTF Audio) impulsado por `SpatialReverbEnvironment` y `PresetReverb` en `AudioDspEngine` con opciones de Habitación, Salón, Catedral y Estadio.
- **Política de contribuciones**: Repositorio cerrado a contribuciones de terceros. No se aceptan Pull Requests ni colaboraciones externas.
