# Architecture and Product Decisions

| Decision | Approved value |
|---|---|
| Product name | Voxera |
| Platform | Android |
| Main language | Kotlin |
| UI | Jetpack Compose |
| Architecture | Clean Architecture + MVVM |
| Languages | Arabic and English |
| First launch locale | Arabic only if system default language is Arabic; otherwise English |
| Manual language | Available in Settings and persisted with Preferences DataStore |
| Locale application | AndroidX AppCompat per-app locale API |
| Theme direction | Premium/simple |
| Main accents | Sky blue and red |
| Network | Offline-first; no internet required |
| Business model | One-time purchase |
| Call recording | Excluded |
| AI | Excluded |
| Amplifier timing | Post-recording only |
| Echo timing | Post-recording only |
| Preserve source | Yes; processed output is a separate copy by default |
| Navigation | Home, Library, Tools; Settings from Home |
| Unfinished sections | Clearly labeled placeholders; no fake data or actions |
| Recording format | WAV, PCM 16-bit, mono, 44.1 kHz |
| Recording persistence | App-specific Music directory, internal files fallback |
| Recording service | Android microphone foreground service; initiated while Activity is visible |
| Waveform source | Live RMS amplitude from actual AudioRecord PCM samples |
| QA schedule | Comprehensive testing after all planned phases; source-level review continues during development |
