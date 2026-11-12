# Project Structure

```text
Voxera/
├── app/
│   ├── src/main/
│   │   ├── java/com/voxera/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── audio/
│   │   │   │   ├── RecordingService.kt
│   │   │   │   └── RecordingUiState.kt
│   │   │   ├── data/preferences/LanguagePreferences.kt
│   │   │   ├── presentation/recording/RecordingViewModel.kt
│   │   │   └── ui/
│   │   │       ├── VoxeraApp.kt
│   │   │       └── theme/Theme.kt
│   │   ├── res/
│   │   │   ├── values/strings.xml
│   │   │   ├── values-ar/strings.xml
│   │   │   └── xml/locales_config.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── project_docs/
└── root Gradle configuration
```

Phase 2 adds navigation, Settings, persistent language preferences, and Android per-app locale configuration. Phase 3 adds microphone capture, foreground service, WAV writing, recording state, and live waveform. Room-backed library and DSP remain in later phases.
