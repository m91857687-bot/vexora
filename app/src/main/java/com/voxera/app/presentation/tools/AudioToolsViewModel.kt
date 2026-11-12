package com.voxera.app.presentation.tools

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voxera.app.audio.WavAudioProcessor
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.data.recordings.RecordingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AudioToolsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = RecordingRepository(appContext)
    private val gain = MutableStateFlow(0f)
    private val echo = MutableStateFlow(0f)
    private val delay = MutableStateFlow(250f)
    private val selected = MutableStateFlow<RecordingEntity?>(null)
    private val mutableProgress = MutableStateFlow<Int?>(null)
    private val mutableError = MutableStateFlow<String?>(null)
    private val mutableMessage = MutableStateFlow<String?>(null)
    private var operation: Job? = null
    private var player: MediaPlayer? = null
    private var previewFile: File? = null

    val recordings: StateFlow<List<RecordingEntity>> = repository.observeRecordings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val selectedRecording: StateFlow<RecordingEntity?> = selected
    val gainDb: StateFlow<Float> = gain
    val echoMix: StateFlow<Float> = echo
    val delayMs: StateFlow<Float> = delay
    val progress: StateFlow<Int?> = mutableProgress
    val error: StateFlow<String?> = mutableError
    val message: StateFlow<String?> = mutableMessage

    init {
        viewModelScope.launch { runCatching { repository.refresh() } }
    }

    fun select(item: RecordingEntity) { stopPreview(); selected.value = item; clearNotices() }
    fun setGain(value: Float) { gain.value = value.coerceIn(0f, 24f) }
    fun setEcho(value: Float) { echo.value = value.coerceIn(0f, 0.70f) }
    fun setDelay(value: Float) { delay.value = value.coerceIn(80f, 700f) }

    fun preview() {
        val item = selected.value ?: return
        if (operation?.isActive == true) return
        operation = viewModelScope.launch {
            stopPreview()
            mutableError.value = null
            mutableMessage.value = null
            mutableProgress.value = 0
            val output = File(appContext.cacheDir, "voxera_preview_${System.currentTimeMillis()}.wav")
            try {
                withContext(Dispatchers.IO) {
                    WavAudioProcessor.process(repository.fileFor(item), output, settings()) { mutableProgress.value = it }
                }
                previewFile = output
                playFile(output)
                mutableMessage.value = "preview_ready"
            } catch (error: CancellationException) {
                output.delete()
                throw error
            } catch (error: Exception) {
                output.delete()
                mutableError.value = "processing_failed"
            } finally {
                mutableProgress.value = null
            }
        }
    }

    fun export() {
        val item = selected.value ?: return
        if (operation?.isActive == true) return
        operation = viewModelScope.launch {
            stopPreview()
            mutableError.value = null
            mutableMessage.value = null
            mutableProgress.value = 0
            try {
                val directory = appContext.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC)
                    ?: File(appContext.filesDir, "Music")
                val outputDirectory = File(directory, "Voxera/Processed")
                if (!outputDirectory.exists()) {
                    outputDirectory.mkdirs()
                }
                val safeBase = item.name.replace(Regex("[^A-Za-z0-9_\\- ]"), "_").take(60).ifBlank { "Recording" }
                var output = File(outputDirectory, "${safeBase}_processed.wav")
                var suffix = 2
                while (output.exists()) { output = File(outputDirectory, "${safeBase}_processed_${suffix++}.wav") }
                withContext(Dispatchers.IO) {
                    WavAudioProcessor.process(repository.fileFor(item), output, settings()) { mutableProgress.value = it }
                    repository.refresh()
                }
                mutableMessage.value = "export_success:${output.absolutePath}"
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableError.value = "processing_failed"
            } finally {
                mutableProgress.value = null
            }
        }
    }

    fun cancel() {
        operation?.cancel()
        operation = null
        mutableProgress.value = null
        mutableMessage.value = "operation_cancelled"
    }

    fun stopPreview() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        previewFile?.delete()
        previewFile = null
    }

    private fun playFile(file: File) {
        val media = MediaPlayer()
        player = media
        media.setDataSource(file.absolutePath)
        media.setOnCompletionListener { stopPreview() }
        media.setOnErrorListener { _, _, _ -> mutableError.value = "preview_failed"; stopPreview(); true }
        media.setOnPreparedListener { it.start() }
        media.prepareAsync()
    }

    private fun settings() = WavAudioProcessor.Settings(gain.value, echo.value, delay.value.toInt())
    private fun clearNotices() { mutableError.value = null; mutableMessage.value = null }

    override fun onCleared() {
        operation?.cancel()
        stopPreview()
        previewFile?.delete()
        super.onCleared()
    }
}
