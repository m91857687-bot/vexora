# Voxera — Premium Offline Android Voice Studio

Voxera is a professional offline-first Android voice recorder and audio processor built with Kotlin, Jetpack Compose, Material 3, and Clean Architecture.

## Key Features
- **Studio-Quality Voice Recording:** Low-latency 44.1 kHz 16-bit mono PCM WAV capture using native `AudioRecord`.
- **Live Real-time Waveform:** Dynamic visual feedback driven directly by raw microphone RMS amplitudes.
- **Background Recording Service:** Persistent microphone foreground service with ongoing status notifications and notification controls (Android 13+ `POST_NOTIFICATIONS` supported).
- **Comprehensive Audio Library:** Room database persistence with automatic directory synchronization, search, 4-way sorting (Newest, Oldest, Name, Size), MediaPlayer playback, rename, delete, and FileProvider sharing.
- **Non-Destructive Audio Processing:** Offline gain amplification (0 to +24 dB) and echo effect (0 to 70% mix, 80 to 700 ms delay) with live preview, cancellation, progress reporting, clipping protection, and independent exported copies.
- **Full Localization:** Complete Arabic (العربية) and English support with automatic locale detection, persistent manual override, and proper RTL/LTR layout mirroring.
- **4 Approved Themes:** Classic (Slate & Cyan/Crimson), Midnight (AMOLED Black & Electric Cyan), Royal (Imperial Violet & Gold), and System (Dynamic device matching).
- **Zero Internet Requirement:** Completely offline, privacy-first, zero telemetry or AI dependencies.

## Technical Architecture
- **Language & Framework:** Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00), Material Design 3.
- **Architecture:** Clean MVVM with `StateFlow`, Coroutines, and repository patterns.
- **Persistence:** Room Database 2.7.0 (with KSP) for recordings metadata, Jetpack DataStore Preferences for app settings.
- **Audio Engine:** Native `AudioRecord`, `MediaPlayer`, and custom `WavAudioProcessor`.
- **SDK Targets:** Min SDK 26 (Android 8.0 Oreo), Compile & Target SDK 36 (Android 15+).

## Verification & Testing
- **Full Android Build:** Successfully compiles via `gradle :app:assembleDebug`.
- **Automated Test Suite:** 100% pass rate (9 of 9 tests passed) via `gradle :app:testDebugUnitTest`.
  - 5 WavAudioProcessor tests: Gain export, echo tail length, max gain clamping, truncated WAV rejection, unity gain preservation.
  - 4 Robolectric CUJ tests: Language & theme persistence, RecordingSession state transitions, Room CRUD & sync, recording rename validation.
- **Custom Adaptive Icon:** Generated and configured with adaptive vector foreground, solid background, and 5-density raster mipmaps.
- **Debug APK Location:** `app/build/outputs/apk/debug/app-debug.apk` (~20 MB).

## Build Commands
```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble debug APK
gradle :app:assembleDebug
```
