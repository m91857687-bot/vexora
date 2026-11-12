package com.voxera.app.audio

enum class RecordingStatus {
    IDLE, STARTING, RECORDING, PAUSED, STOPPING, STOPPED, ERROR
}

data class RecordingUiState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val elapsedMillis: Long = 0L,
    val waveform: List<Float> = emptyList(),
    val outputPath: String? = null,
    val errorMessage: String? = null
)

/** In-process state bridge between the foreground service and the presentation layer. */
object RecordingSession {
    private val mutableState = kotlinx.coroutines.flow.MutableStateFlow(RecordingUiState())
    val state: kotlinx.coroutines.flow.StateFlow<RecordingUiState> = mutableState

    fun update(transform: (RecordingUiState) -> RecordingUiState) {
        mutableState.value = transform(mutableState.value)
    }

    fun set(value: RecordingUiState) {
        mutableState.value = value
    }
}
