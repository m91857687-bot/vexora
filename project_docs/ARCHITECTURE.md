# Architecture

Planned dependency direction:

`presentation -> domain <- data`

- `presentation`: Compose screens, navigation, ViewModels, UI state, and resource-based localization.
- `domain`: use cases and core models; no Android UI dependencies.
- `data`: Room, DataStore, file storage, repository implementations.
- `audio`: recording engine and offline DSP implementation.

## Phase 2 implementation notes
- `MainActivity` loads the persisted language before composing the app.
- `LanguagePreferences` stores the selected `ar` or `en` locale in Preferences DataStore.
- `AppCompatDelegate` applies the selected per-app locale, allowing Android resource resolution and RTL/LTR layout direction to follow the locale.
- All visible UI copy is stored in `strings.xml` and `values-ar/strings.xml`; strings are not hardcoded in Composables, except the purely decorative record glyph and waveform geometry.
- Navigation uses Navigation Compose. Home, Library, Tools, and Settings are separate destinations.
- Library and Tools screens are honest phase placeholders until their functional phases are implemented.

Audio capture and offline effects must remain separate. UI must never perform blocking file or DSP work on the main thread. Use coroutines/dispatchers and explicit lifecycle handling.


## Phase 3 recording implementation
- `audio/RecordingService.kt` owns `AudioRecord`, microphone capture, WAV output, foreground notification, and pause/resume/stop commands.
- `audio/RecordingUiState.kt` defines the recording state and an in-process `StateFlow` bridge from the service to the UI.
- `presentation/recording/RecordingViewModel.kt` exposes recording state and sends service commands; Compose does not perform audio I/O.
- `VoxeraApp.kt` requests `RECORD_AUDIO` at runtime, renders the real sampled waveform, timer, and recording controls.
- Audio is captured as mono 44.1 kHz PCM16 and wrapped in a WAV container after stopping. The source recording is not modified by any processing effect.
- Output is saved under the app-specific external Music directory, falling back to the app's internal files directory. It is not yet indexed in a user-facing library; that belongs to Phase 4.
- The foreground service uses the microphone service type and required Android permissions. Start is triggered while the Activity is visible.


## Phase 5 audio processing flow
1. `AudioToolsScreen` observes the recording list and user-selected processing parameters.
2. `AudioToolsViewModel` requests preview/export operations and runs file work on `Dispatchers.IO`.
3. `WavAudioProcessor` reads the source PCM WAV, applies gain and delayed feedback echo, and writes a separate temporary/output WAV.
4. Preview output is placed in cache and played through `MediaPlayer`; export output is placed under app-specific `Music/Voxera/Processed`.
5. The source recording is never opened for writing. Cancellation removes the partial output.
