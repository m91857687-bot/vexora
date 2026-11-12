# Voxera — Features Specification

## Phase 1: Foundation — prepared
- Android project scaffold.
- Compose theme and initial screen shell.
- Core project documentation.

## Phase 2: UI and localization — implemented in this package
- Main navigation: Home, Library, and Tools.
- Settings screen reachable from the home screen.
- Complete Arabic and English resources for all current UI labels and messages.
- First launch selects Arabic only when the device default language is Arabic; otherwise English.
- Manual language switching with persistent preference.
- AppCompat per-app locale integration and RTL/LTR resource direction.
- Library and Tools are clearly marked as future-phase screens; they do not display fabricated data or claim unfinished functions work.

## Phase 3: Recording engine — implemented in this source package; runtime verification pending
- Runtime microphone permission request before recording.
- Recording state machine: start, pause, resume, stop.
- Microphone foreground service with an ongoing notification.
- Live waveform sampled from actual microphone PCM input using RMS amplitude.
- 44.1 kHz, mono, PCM 16-bit WAV output with a finalized WAV header.
- Timestamp-based file names stored in the app-specific Music directory (private to the app).
- Recording errors and storage failures are surfaced in the UI; incomplete files are removed when the service catches an error.
- Recording is initiated from the visible app UI to comply with microphone foreground-service start restrictions.

## Phase 4: Recording library — implemented in this source package; runtime verification pending
- Room database stores recording metadata (path, display name, duration, size, modified time).
- Library discovers WAV recordings in the same app-private Music directory used by the recording service.
- Search by recording name and sorting by newest, oldest, name, and file size.
- Playback using MediaPlayer asynchronous preparation; one playback at a time.
- Rename and delete with validation and user confirmation.
- Share through Android Sharesheet using FileProvider content URIs.
- Details include duration, file size, modification date, and WAV encoding.
- Arabic and English resources are provided for library controls and messages.
- Room metadata.
- List, search, sort, rename, delete, share.
- Playback and file details.

## Phase 5: Offline audio processing — not started
- Non-destructive amplifier export.
- Echo effect with adjustable parameters.
- Preview, progress, cancellation, and error handling.
- Never overwrite original source by default.

## Phase 6: QA and release preparation — not started
- Unit and instrumentation tests.
- Device compatibility testing.
- Accessibility, performance, privacy, and Play policy review.

## Explicit exclusions
- Call recording.
- AI transcription or summarization.
- Live amplification or live echo while recording.
- Cloud sync or mandatory internet.


## Phase 5 — Audio processing
- Amplifier: offline gain adjustment from 0 to +24 dB.
- Echo: adjustable wet mix and delay time.
- Preview: render a temporary processed copy and play it without altering the source.
- Export: save a new WAV copy to app-specific processed audio storage.
- Processing runs off the main thread and exposes progress/cancellation.
- Initial processor input contract: Voxera-generated mono 16-bit PCM WAV.
