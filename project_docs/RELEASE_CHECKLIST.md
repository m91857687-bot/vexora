# Voxera Release Checklist

## A. Build and automated tests
- [x] GitHub Actions `Android CI` modernized with JDK 21 and Gradle 9.3.1.
- [x] `:app:testDebugUnitTest` completes successfully with real JUnit 4 & Robolectric (9/9 tests pass, 100%).
- [x] `:app:assembleDebug` completes successfully producing `app-debug.apk` (20 MB).
- [x] No unresolved dependency, KSP, manifest merger, or resource errors.
- [x] Release build type and signing configuration prepared securely without hardcoded secrets.

## B. Recording on emulator and physical phone
- [x] `enableEdgeToEdge()` integrated into `MainActivity` for modern full-bleed UX.
- [x] First launch requests microphone permission and Android 13+ `POST_NOTIFICATIONS`.
- [x] Denying permission shows a localized user-friendly message and does not crash.
- [x] Granting permission transitions state to `RECORDING` and feeds live RMS samples to waveform.
- [x] Pause, resume, and stop state machine transitions fully tested and verified via Robolectric.
- [x] AudioRecord zero-sample return handled with short delay to avoid buffer latency crashes.
- [x] Timer calculates elapsed time from actual written bytes; WAV header generation verified.
- [x] Microphone foreground service configured with `ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE`.
- [x] Ongoing notification with channel `voxera_recording` and deep link back to `MainActivity`.
- [ ] Physical device acoustic microphone test under quiet and noisy environments.
- [ ] Telephony interruption during active recording on physical SIM hardware.
- [ ] Multi-hour background recording endurance on aggressive OEM battery managers (HyperOS/OneUI).

## C. Library
- [x] New recordings appear immediately after stop and persist across app restarts.
- [x] Recursive directory sync (`walkTopDown()`) discovers recordings and `Processed/` audio.
- [x] Playback starts, stops, and releases `MediaPlayer` resources without leaks.
- [x] Search query filtering and 4-way sorting (Newest, Oldest, Name, Largest) verified.
- [x] Rename validates inputs, prevents blank/illegal characters/duplicates, and updates Room row.
- [x] Delete confirms action and removes physical file and database entry safely.
- [x] Share opens Android Sharesheet via `FileProvider` with temporary read permission.
- [x] Details dialog shows formatted duration, file size, modification date, and WAV audio format.
- [x] Resilient sync guards against I/O anomalies; records are never deleted if files exist on disk.

## D. Audio processing
- [x] Gain amplification across 0 to +24 dB with clamp limiting (-32768 to 32767).
- [x] Echo mix (0 to 70%) and delay (80 to 700 ms) with 2-second tail decay calculation.
- [x] Preview rendering, playback, and temporary cache cleanup tested.
- [x] Export creates a separate WAV copy in `Music/Voxera/Processed/` without touching the original.
- [x] Processing progress reporting is monotonic; cancellation cleans up partial output files.
- [x] Truncated and corrupt WAV files safely rejected with clear user error messages.
- [x] Exported copies automatically indexed into the recording library.

## E. Language, UI, accessibility, and privacy
- [x] Automatic language selection (Arabic if system locale is Arabic, English otherwise).
- [x] Manual language selection persisted in Jetpack DataStore across app launches.
- [x] RTL layout mirroring for Arabic and LTR for English on all screens.
- [x] All 4 approved themes implemented: Classic, Midnight, Royal, and System.
- [x] Material 3 typography and dynamic ColorScheme tokens used exclusively (no hardcoded colors).
- [x] Minimum 48dp touch targets and Compose `testTag` identifiers across all interactive elements.
- [x] Strict zero-network privacy: zero internet permissions, zero third-party telemetry, zero cloud tracking.
- [x] FileProvider grants scoped, temporary read-only access only to the shared recording.

## F. Google Play release preparation
- [x] Application ID (`com.voxera.app`), minSdk (26), and targetSdk (36) configured.
- [x] Custom adaptive app icon generated (vector foreground, solid background, 5 mipmap densities).
- [x] App title (Voxera) and metadata in sync between `strings.xml` and `metadata.json`.
- [ ] Production upload keystore generation and Play Console upload credentials.
- [ ] Google Play Data Safety form completed (confirming offline audio capture with zero data collection).
- [ ] Store listing screenshots captured in Arabic and English across phones and tablets.
- [ ] Internal testing track deployment prior to production release.
