# Voxera — Android Audio Application

Voxera is an offline-first Android audio recorder with a live microphone waveform, a recording library, and non-destructive amplifier/echo processing.

## Approved scope
- Voice recording only; call recording is excluded.
- Real-time waveform driven by microphone samples.
- Background recording through an Android microphone foreground service.
- Room-backed library with playback, search, sorting, rename, delete, share, and details.
- Offline amplifier and echo processing after recording.
- Export creates a separate processed copy; the original recording is not overwritten.
- Arabic and English localization with RTL/LTR support.
- No AI features and no required internet connection for core functionality.

## Technology
- Kotlin, Jetpack Compose, Material 3.
- MVVM-oriented presentation and repository/data layers.
- Room, DataStore, Coroutines, Android AudioRecord, MediaPlayer.
- Minimum SDK 26; compile/target SDK 36.

## Build locally
Requirements: JDK 17, Android SDK Platform 36, Build Tools 36.0.0, and Gradle 8.13.

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions
`.github/workflows/android-ci.yml` installs JDK 17, Gradle 8.13, and Android SDK 36, then runs JVM unit tests and assembles a debug APK on pushes, pull requests, or manual workflow dispatch.

## Verification boundary
The source package is not a verified release APK. The current development environment used for the Phase 6 review does not contain Gradle or the Android SDK, so the full Android Gradle build and emulator/device tests could not be run here. See `project_docs/QA_REPORT.md` and `project_docs/KNOWN_ISSUES.md` for exact results and remaining checks.
