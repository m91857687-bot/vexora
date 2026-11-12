# Changelog

## Phase 2 review
- Reviewed Kotlin source, Gradle configuration, manifest, resources, and project documentation.
- Fixed the application theme: `AppCompatActivity` now uses `Theme.AppCompat.DayNight.NoActionBar` instead of a platform Material theme.
- Confirmed all XML resources parse successfully.
- Confirmed English and Arabic resources have matching keys (27 each).
- Confirmed ZIP archive integrity.
- Build and emulator/device tests remain pending because Gradle and Android SDK are unavailable in the review environment.

## Phase 2 implementation
- Added Home, Library, Tools, and Settings navigation.
- Added English and Arabic string resources.
- Added first-launch language selection and persistent manual language preference using DataStore.
- Added AppCompat per-app locale integration and locale configuration.
- Marked Library and Tools as future-phase placeholders.


## Phase 3 — Recording engine source implementation
- Added runtime microphone permission request.
- Added microphone foreground service with Android foreground-service type and notification channel.
- Added AudioRecord PCM16 mono 44.1 kHz capture.
- Added start, pause, resume, and stop service actions.
- Added WAV container header creation/finalization and timestamp-based app-private file output.
- Replaced the static waveform placeholder with a live RMS waveform fed by microphone samples.
- Added recording elapsed-time display, status feedback, and basic error reporting.
- Updated project architecture, structure, status, known issues, and next-task documentation.
- Build and device tests were not run; comprehensive QA remains deferred until all phases are complete by user decision.


## Phase 4
- Added Room-backed recording metadata and file discovery.
- Implemented library browsing, search, sorting, playback, rename, delete, share, and details.
- Added localized strings and FileProvider sharing configuration.


## Phase 5 — Offline audio processing
- Replaced the Tools placeholder with an audio-processing screen connected to the Room-backed recording list.
- Added PCM WAV offline processing with adjustable gain and echo/delay controls.
- Added temporary preview rendering and playback.
- Added separate processed-copy export with progress and cancellation.
- Preserved original recordings by processing from the source into a separate destination file.
- Added Arabic and English UI resources for all Phase 5 controls and feedback.
- Build and device verification remain deferred to the agreed final QA phase.

## Phase 6 — integrated QA preparation and source hardening
- Changed MainActivity to render Compose content immediately while loading the saved locale asynchronously.
- Corrected WAV processing progress arithmetic to use Long and added strict RIFF/data size validation to reject truncated WAV input.
- Hardened WAV recording finalization with unsigned 32-bit chunk-size writing and a clean stop at the RIFF size boundary.
- Added best-effort recovery of partial recordings after write/capture errors, with a localized warning.
- Added JVM tests for gain, echo tail, RIFF header sizes, original-file preservation, and truncated input.
- Added GitHub Actions workflow to execute JVM tests and build the debug APK using JDK 17, Gradle 8.13, and Android SDK 36.
- Added `QA_REPORT.md` and `RELEASE_CHECKLIST.md`.
- Standalone WAV tests, XML/resource checks, locale parity, Gradle catalog checks, and long-file progress test passed.
- Full Gradle build, CI run, emulator test, and physical-device test remain NOT RUN in this environment.
