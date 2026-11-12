# Changelog

## Phase 6 — Comprehensive Review, Code Hardening, and Build Finalization (Completed)
- **GitHub Import & Secrets Build Fix:** Configured valid `.env` and `.env.example` placeholders required by the Secrets Gradle Plugin, resolving an unhandled evaluation exception and preventing invalid `BuildConfig` generation (`GEMINI_API_KEY = ;`).
- **CI Workflow Modernization:** Upgraded `.github/workflows/android-ci.yml` from JDK 17 / Gradle 8.13 to JDK 21 / Gradle 9.3.1 to strictly match AGP 9.1.1 toolchain requirements.
- **Code Cleanliness & Modern Compose API:** Modernized `LibraryScreen.kt` using `Icons.AutoMirrored.Filled.Sort` and simplified smart-cast conditions in `RecordingService.kt`, eliminating all compiler warnings.
- **Automated Test Resolution:** Fixed `WavAudioProcessorTest` where Kotlin expression-body test methods returned `Boolean` from `deleteRecursively()`, causing JUnit 4 `InvalidTestClassError: Method should be void`. Added explicit Unit return types and expanded test coverage to include 24 dB gain clipping limits and 0 dB unity gain preservation.
- **AudioRecord Read Resiliency:** Fixed potential crash in `RecordingService` when `AudioRecord.read()` returns `0` (zero samples available on transition or buffer latency) by introducing a short delay instead of throwing `IllegalStateException`.
- **Immediate Room Synchronization:** Configured `RecordingService` to asynchronously trigger `RecordingRepository.refresh()` immediately upon recording finalization (both normal stop and partial recovery), making newly recorded files instantly available in the Library tab.
- **Recursive Audio Discovery:** Enhanced `RecordingRepository.refresh()` using recursive `walkTopDown()` scanning across both external Music and internal file directories, automatically discovering and indexing processed audio exported to `Music/Voxera/Processed`.
- **Room Storage Safety:** Guarded Room entity deletions during refresh so that database entries are pruned only if the audio file physically no longer exists on disk (`!File(path).exists()`), preventing data loss during temporary I/O anomalies.
- **Post-Export Indexing:** Added automatic `repository.refresh()` in `AudioToolsViewModel.export()` and guaranteed directory creation for `Voxera/Processed` before writing.
- **Approved Multi-Theme Architecture:** Implemented all four approved themes:
  - `Classic`: Sky blue and crimson on dark luxury slate.
  - `Midnight`: Electric cyan on AMOLED pitch black.
  - `Royal`: Imperial violet with gold accents.
  - `System`: Dynamic adaptation to Android light/dark modes.
  Added `AppPreferences` with unified DataStore persistence, integrated theme selection into `SettingsScreen`, and updated all screens to consume dynamic M3 `MaterialTheme.colorScheme` tokens.
- **Localization Expansion:** Added 10 new strings for theme names and descriptions with 100% key parity (110 English strings and 110 Arabic strings).
- **Navigation & Accessibility Hardening:** Added `BackHandler` in `SettingsScreen` to handle system back gestures, updated `POST_NOTIFICATIONS` runtime permission requests on Android 13+, ensured 48dp touch targets, and assigned Compose `testTag` IDs across all interactive components.
- **Custom Adaptive App Icon:** Replaced default template launcher icon with custom adaptive cyan and crimson waveform microphone icon in vector and 5-density raster mipmaps.
- **Robolectric Integration:** Added comprehensive local JVM Robolectric test suite (`VoxeraAppLogicTest`) covering DataStore preferences, Room CRUD operations, `RecordingSession` state transitions, and file rename validation.
- **APK Verification:** Successfully executed `:app:assembleDebug` producing signed `app-debug.apk` (20 MB).

## Phase 5 — Offline audio processing
- Replaced the Tools placeholder with an audio-processing screen connected to the Room-backed recording list.
- Added PCM WAV offline processing with adjustable gain and echo/delay controls.
- Added temporary preview rendering and playback.
- Added separate processed-copy export with progress and cancellation.
- Preserved original recordings by processing from the source into a separate destination file.
- Added Arabic and English UI resources for all Phase 5 controls and feedback.

## Phase 4 — Library and Playback
- Added Room-backed recording metadata and file discovery.
- Implemented library browsing, search, sorting, playback, rename, delete, share, and details.
- Added localized strings and FileProvider sharing configuration.

## Phase 3 — Recording engine
- Added runtime microphone permission request.
- Added microphone foreground service with Android foreground-service type and notification channel.
- Added AudioRecord PCM16 mono 44.1 kHz capture.
- Added start, pause, resume, and stop service actions.
- Added WAV container header creation/finalization and timestamp-based app-private file output.
- Replaced the static waveform placeholder with a live RMS waveform fed by microphone samples.
- Added recording elapsed-time display, status feedback, and basic error reporting.

## Phase 2 — UI and localization
- Added Home, Library, Tools, and Settings navigation.
- Added English and Arabic string resources.
- Added first-launch language selection and persistent manual language preference using DataStore.
- Added AppCompat per-app locale integration and locale configuration.

## Phase 1 — Foundation
- Android project foundation, Kotlin/Compose setup, and core documentation.
