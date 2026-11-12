# Voxera — Master Context

## Product
Voxera is a premium, offline-first Android audio application.

## Approved scope
- Voice recording only; call recording is explicitly excluded.
- Live animated waveform driven by microphone samples.
- Background recording through Android microphone foreground-service rules.
- Amplifier and Echo are applied after recording, not live.
- Preserve the original recording when exporting processed audio.
- No AI features.
- No internet dependency for core features.
- One-time paid app model; no subscription.

## Languages
- Full Arabic and English localization.
- First launch: Arabic if the device's primary locale is Arabic; English for all other locales.
- Manual language override in settings, persisted across launches.
- Correct RTL for Arabic and LTR for English.

## Visual identity
- Cyan/sky-blue and red accents, dark premium base.
- Design direction: simple and luxurious.

## Technology
- Kotlin, Jetpack Compose, Material 3.
- MVVM-oriented presentation and repository/data layers.
- Room for recording metadata; DataStore for preferences.
- Android native audio APIs for recording and offline DSP.
- Do not claim device-wide compatibility without tests.

## Provisional identifiers
- Application ID: `com.voxera.app`
- Minimum SDK: 26
- Compile/target SDK: 36
- These can be changed before release.

## Implemented source scope
- Phase 1: Android project foundation and initial Compose shell.
- Phase 2: Navigation, Settings, Arabic/English resources, persisted language preference, AppCompat locale handling.
- Phase 3: AudioRecord PCM16 mono 44.1 kHz capture, microphone foreground service, runtime permission, start/pause/resume/stop, live RMS waveform, WAV output.
- Phase 4: Room-backed WAV library, playback, search, sorting, rename, delete, share, details.
- Phase 5: Offline mono 16-bit PCM WAV gain (0 to +24 dB), Echo mix (0 to 70%) and delay (80 to 700 ms), preview, progress, cancellation, and separate processed export.
- Phase 6 source hardening: immediate initial UI render, WAV header validation, long-safe progress, partial recording recovery, JVM tests, GitHub Actions workflow, QA/release documentation.

## Current status
Phase 6 QA preparation is in progress. Standalone WAV tests and source/resource checks passed. Full Gradle build, GitHub Actions run, emulator test, and physical-device test have NOT been performed. No verified APK exists from this environment. See `project_docs/QA_REPORT.md`, `KNOWN_ISSUES.md`, and `RELEASE_CHECKLIST.md`.

## Working rules
- Implement in approved phases.
- Do not add features outside the approved scope without asking.
- Review related files before modifying them.
- Update PROJECT_STATUS.md, NEXT_TASK.md, and CHANGELOG.md after each phase.
- Never claim a build or test passed unless it was actually run.
- The user prefers the comprehensive end-to-end test pass after all implementation phases are complete; keep interim checks focused and document them accurately.
