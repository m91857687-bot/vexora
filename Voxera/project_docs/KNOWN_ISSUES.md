# Known Issues and Release Verification Gaps

## Build and integration
1. A full Android Gradle build has not been run in the current environment because Gradle and the Android SDK are not installed.
2. Gradle Wrapper scripts/JAR are not included. The added GitHub Actions workflow installs Gradle 8.13 directly; a wrapper should still be generated from a trusted Gradle installation before local command-line distribution if desired.
3. Room KSP schema generation and Android/Compose dependency integration remain unverified until CI or Android Studio completes a real build.

## Device/runtime verification
4. No emulator or physical Android device is available in this environment. Microphone permission, AudioRecord capture, foreground-service behavior, notification behavior, background recording, pause/resume, and process interruption remain unverified on-device.
5. MediaPlayer playback, FileProvider sharing, rename/delete behavior, Room persistence, and app restart recovery require device tests.
6. Amplifier/echo listening quality, clipping, preview completion, cancellation during processing, low-storage behavior, and very long recordings require device tests.
7. Verify microphone foreground-service restrictions on Android 14+ and Android 15/16, including start while the app is visible and behavior after the app moves to background.

## Product/release preparation
8. The processed WAV copies are exported to app-specific `Music/Voxera/Processed` storage but are not listed as a separate section in the recording library.
9. A production app icon, release signing configuration, Play Store screenshots/listing, privacy policy, Data safety declaration, and final one-time paid-app setup are not included in this source package.
10. Confirm backup behavior and user expectations for locally stored recordings before release. Sharing explicitly grants a receiving app read access to the selected file URI.
11. Accessibility review (TalkBack labels, touch target sizes, contrast, font scaling) and small-screen/large-font layout checks remain pending.

## Phase 6 source changes
- MainActivity now renders the UI immediately while DataStore loads the persisted language, avoiding a blank first frame.
- WAV processing now uses Long arithmetic for progress percentages, validates declared RIFF/data sizes, and rejects truncated input instead of silently processing a shortened file.
- Recording finalization writes RIFF chunk sizes as unsigned 32-bit values and stops cleanly before the classic WAV size limit.
- If recording fails after audio samples have been written, the service attempts to finalize and preserve the partial recording instead of deleting all captured audio.
- Added JVM unit tests for gain output, echo tail/header correctness, original-file preservation, and truncated-input rejection.
- Added GitHub Actions workflow to run JVM tests and assemble a debug APK using JDK 17, Gradle 8.13, and Android SDK 36.

## Verification status
See `QA_REPORT.md` for the exact checks performed and their boundaries. Do not describe this package as a fully built or device-verified APK until CI and device tests pass.
