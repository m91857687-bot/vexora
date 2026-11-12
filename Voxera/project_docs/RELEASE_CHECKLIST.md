# Voxera Release Checklist

## A. Build and automated tests
- [ ] GitHub Actions `Android CI` completes successfully.
- [ ] `:app:testDebugUnitTest` completes successfully with real JUnit/Gradle.
- [ ] `:app:assembleDebug` completes successfully.
- [ ] No unresolved dependency, KSP, manifest merger, or resource errors.
- [ ] Release build type and signing configuration are prepared securely (never commit signing keys/passwords).

## B. Recording on emulator and physical phone
- [ ] First launch requests microphone permission at the correct time.
- [ ] Denying permission shows a clear message and does not crash.
- [ ] Granting permission starts recording and the waveform responds to actual sound.
- [ ] Silence produces a low waveform; louder sound produces a stronger waveform.
- [ ] Pause, resume, and stop work repeatedly.
- [ ] Timer excludes paused time and saved WAV header/duration are correct.
- [ ] Recording continues with screen off/background while Android permits the microphone foreground service.
- [ ] Notification is visible and app can be reopened from it.
- [ ] Incoming calls/audio-focus interruptions, Bluetooth/headset changes, and process termination are handled safely or documented.
- [ ] Low storage or write failure preserves a valid partial recording when possible and reports the condition.
- [ ] Very long recording does not produce an invalid RIFF header.

## C. Library
- [ ] New recordings appear after stop and after app restart.
- [ ] Playback starts, stops, and completes without leaking MediaPlayer resources.
- [ ] Search and all sort orders work.
- [ ] Rename rejects blank/invalid/duplicate names and preserves the audio file.
- [ ] Delete requires confirmation and removes the file/Room row.
- [ ] Share opens Android Sharesheet and the receiving app can read the granted URI.
- [ ] Details show correct duration, size, date, and format.
- [ ] Missing/unreadable directory does not erase existing Room records.

## D. Audio processing
- [ ] Gain values across 0 to +24 dB sound correct and clipping is acceptable.
- [ ] Echo mix and delay controls produce expected audible results.
- [ ] Preview starts, completes, stops, and releases temporary files correctly.
- [ ] Export creates a separate WAV file and never changes the source.
- [ ] Progress remains monotonic; cancellation removes partial output.
- [ ] Low-storage and very long input failures are handled clearly.
- [ ] Processed copies can be found by the user through a documented route; currently they are in app-specific `Music/Voxera/Processed` storage and are not shown in the library list.

## E. Language, UI, accessibility, and privacy
- [ ] First launch selects Arabic for Arabic system locale and English otherwise.
- [ ] Manual language selection persists after force-stop/restart.
- [ ] Arabic is RTL and English is LTR on all screens/dialogs.
- [ ] Small screens, landscape, large font, and display scaling do not hide key controls.
- [ ] TalkBack labels and touch targets are adequate.
- [ ] No recording is uploaded; confirm there is no internet permission or hidden network dependency.
- [ ] Confirm backup behavior for recordings and settings matches the published privacy policy.
- [ ] Sharing grants read-only access only to the selected recording.

## F. Google Play release
- [ ] Final application ID, versionCode, versionName, app icon, and signing key are confirmed.
- [ ] Privacy policy is published and accurately explains microphone use, local storage, backup, and sharing.
- [ ] Data safety form and microphone permission disclosures are accurate.
- [ ] Store listing, screenshots, support contact, and content rating are complete.
- [ ] One-time paid-app price and country availability are configured in Play Console.
- [ ] Internal testing track passes before production rollout.
