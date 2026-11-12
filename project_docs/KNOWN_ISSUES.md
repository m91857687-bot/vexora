# Known Issues and Release Verification Status

## Resolved during Final Review
1. **Resolved — Full Android Gradle Build & KSP Compilation:** Completed real Gradle builds (`:app:assembleDebug` and `:app:compileDebugKotlin`) using AGP 9.1.1 and Kotlin 2.2.10. Room KSP code-generation verified without errors.
2. **Resolved — Automated Test Failures:** Fixed JUnit 4 `InvalidTestClassError` where test methods returned `Boolean`. Implemented complete Robolectric test suite (`VoxeraAppLogicTest`) with database operations on `Dispatchers.IO`. All 9 tests now pass (100%).
3. **Resolved — Missing Import in AudioToolsScreen:** Added missing `import androidx.compose.ui.graphics.Color` to resolve compilation failure.
4. **Resolved — AudioRecord Zero-Sample Handshake:** `AudioRecord.read()` zero-return scenario handled with short delay instead of throwing `IllegalStateException`.
5. **Resolved — Library & Processed Audio Indexing:** `RecordingRepository.refresh()` upgraded to recursive directory scanning (`walkTopDown()`), discovering both standard recordings and exported audio in `Music/Voxera/Processed`.
6. **Resolved — Database Record Safety:** Guarded Room deletions during sync so records are only deleted if the file physically no longer exists on disk.
7. **Resolved — App Themes Implemented:** All 4 approved themes (`Classic`, `Midnight`, `Royal`, `System`) implemented with dynamic M3 ColorScheme, persistent DataStore settings, and localized Arabic/English descriptions.
8. **Resolved — Custom Adaptive App Icon:** Created custom adaptive vector foreground and background plus 5 raster density mipmaps (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi), removing default green launcher icon.
9. **Resolved — Android 13+ Notification Permission:** Included `POST_NOTIFICATIONS` runtime permission request alongside `RECORD_AUDIO` to ensure foreground service notification appears.

## Physical Device Verification Recommendations (Pre-Play Store Release)
1. **Physical Microphone Hardware Test:** Test on a physical handset to verify acoustic calibration under extreme noise and low-amplitude whisper conditions.
2. **Telephony Interruption:** Verify behavior when an incoming cellular phone call interrupts an active recording session on physical SIM hardware.
3. **Battery Optimization Exemptions:** For multi-hour background recordings on aggressive OEM skins (e.g., Xiaomi MIUI/HyperOS, Samsung OneUI), verify foreground service stays alive without OEM battery killer termination.
4. **Production Release Signing:** Configure production keystore and upload credentials prior to uploading to Google Play Console.
