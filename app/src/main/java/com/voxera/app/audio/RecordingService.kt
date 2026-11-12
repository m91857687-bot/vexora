package com.voxera.app.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import com.voxera.app.MainActivity
import com.voxera.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class RecordingService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recordingJob: Job? = null
    @Volatile private var audioRecord: AudioRecord? = null
    @Volatile private var paused = false
    @Volatile private var stopRequested = false
    @Volatile private var bytesWritten = 0L
    private val sampleRate = SAMPLE_RATE
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> beginForegroundRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun beginForegroundRecording() {
        if (recordingJob?.isActive == true) return
        stopRequested = false
        paused = false
        bytesWritten = 0L
        RecordingSession.set(RecordingUiState(status = RecordingStatus.STARTING))
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(getString(R.string.notification_recording)),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        )
        recordingJob = serviceScope.launch {
            var output: File? = null
            var stream: FileOutputStream? = null
            try {
                val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                check(minBuffer > 0) { getString(R.string.recording_unsupported_error) }
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    maxOf(minBuffer * 2, sampleRate / 2)
                )
                check(recorder.state == AudioRecord.STATE_INITIALIZED) {
                    getString(R.string.recording_microphone_init_error)
                }
                audioRecord = recorder

                val directory = getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC)
                    ?: File(filesDir, "recordings")
                if (!directory.exists()) check(directory.mkdirs()) { "Could not create recordings folder." }
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
                val recordingFile = File.createTempFile("Voxera_${timestamp}_", ".wav", directory)
                output = recordingFile
                val outputStream = FileOutputStream(recordingFile)
                stream = outputStream
                outputStream.write(ByteArray(WAV_HEADER_SIZE))
                recorder.startRecording()
                check(recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    getString(R.string.recording_microphone_start_error)
                }
                RecordingSession.set(RecordingUiState(status = RecordingStatus.RECORDING))

                val buffer = ShortArray(maxOf(1024, minBuffer / 2))
                var lastUiUpdate = 0L
                while (!stopRequested) {
                    if (paused) {
                        delay(40)
                        continue
                    }
                    val count = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                    if (count > 0) {
                        // Classic RIFF/WAV uses 32-bit chunk sizes. Stop cleanly
                        // before the data chunk can exceed its representable limit.
                        val remainingSamples = ((MAX_WAV_DATA_BYTES - bytesWritten) / 2L)
                            .coerceAtLeast(0L).coerceAtMost(count.toLong()).toInt()
                        if (remainingSamples == 0) {
                            stopRequested = true
                            continue
                        }
                        val pcmBytes = shortsToLittleEndian(buffer, remainingSamples)
                        outputStream.write(pcmBytes)
                        bytesWritten += pcmBytes.size
                        if (remainingSamples < count) stopRequested = true
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastUiUpdate >= 65L) {
                            lastUiUpdate = now
                            val rms = calculateRms(buffer, remainingSamples)
                            val normalized = (rms / 12000f).coerceIn(0.025f, 1f)
                            RecordingSession.update { current ->
                                current.copy(
                                    status = if (paused) RecordingStatus.PAUSED else RecordingStatus.RECORDING,
                                    elapsedMillis = bytesWritten * 1000L / (sampleRate * 2L),
                                    waveform = (current.waveform + normalized).takeLast(WAVEFORM_CAPACITY)
                                )
                            }
                        }
                    } else if (count == 0) {
                        delay(10)
                    } else if (!paused && !stopRequested) {
                        throw IllegalStateException(getString(R.string.recording_interrupted_error))
                    }
                }

                runCatching { if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop() }
                outputStream.flush()
                outputStream.close()
                stream = null
                writeWavHeader(recordingFile, bytesWritten)
                if (bytesWritten <= 0L) {
                    recordingFile.delete()
                    RecordingSession.set(RecordingUiState(status = RecordingStatus.ERROR, errorMessage = getString(R.string.recording_empty_error)))
                } else {
                    RecordingSession.update {
                        it.copy(status = RecordingStatus.STOPPED, outputPath = recordingFile.absolutePath, errorMessage = null)
                    }
                    serviceScope.launch {
                        runCatching {
                            com.voxera.app.data.recordings.RecordingRepository(applicationContext).refresh()
                        }
                    }
                }
            } catch (error: Exception) {
                runCatching { stream?.close() }
                stream = null
                val partialFile = output
                val recovered = partialFile != null && bytesWritten > 0L && runCatching {
                    writeWavHeader(partialFile, bytesWritten)
                    true
                }.getOrDefault(false)
                if (recovered) {
                    val savedFile = partialFile ?: return@launch
                    RecordingSession.set(
                        RecordingUiState(
                            status = RecordingStatus.STOPPED,
                            outputPath = savedFile.absolutePath,
                            errorMessage = getString(R.string.recording_partial_saved)
                        )
                    )
                    serviceScope.launch {
                        runCatching {
                            com.voxera.app.data.recordings.RecordingRepository(applicationContext).refresh()
                        }
                    }
                } else {
                    partialFile?.delete()
                    RecordingSession.set(
                        RecordingUiState(status = RecordingStatus.ERROR, errorMessage = error.message ?: getString(R.string.recording_generic_error))
                    )
                }
            } finally {
                runCatching { audioRecord?.release() }
                audioRecord = null
                stopRequested = false
                paused = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun pauseRecording() {
        val recorder = audioRecord ?: return
        if (RecordingSession.state.value.status != RecordingStatus.RECORDING) return
        paused = true
        runCatching { recorder.stop() }
        RecordingSession.update { it.copy(status = RecordingStatus.PAUSED) }
        updateNotification(getString(R.string.notification_paused))
    }

    private fun resumeRecording() {
        val recorder = audioRecord ?: return
        if (RecordingSession.state.value.status != RecordingStatus.PAUSED) return
        try {
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                recorder.startRecording()
            }
            paused = false
            RecordingSession.update { it.copy(status = RecordingStatus.RECORDING) }
            updateNotification(getString(R.string.notification_recording))
        } catch (error: Exception) {
            RecordingSession.set(RecordingUiState(status = RecordingStatus.ERROR, errorMessage = error.message ?: getString(R.string.recording_generic_error)))
            stopRequested = true
        }
    }

    private fun stopRecording() {
        if (recordingJob?.isActive != true) return
        RecordingSession.update { it.copy(status = RecordingStatus.STOPPING) }
        stopRequested = true
        runCatching { audioRecord?.let { if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) it.stop() } }
    }

    private fun shortsToLittleEndian(samples: ShortArray, count: Int): ByteArray {
        val result = ByteArray(count * 2)
        for (index in 0 until count) {
            val sample = samples[index].toInt()
            result[index * 2] = (sample and 0xFF).toByte()
            result[index * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        return result
    }

    private fun calculateRms(samples: ShortArray, count: Int): Float {
        var sum = 0.0
        for (index in 0 until count) {
            val value = samples[index].toDouble()
            sum += value * value
        }
        return sqrt(sum / count).toFloat()
    }

    private fun writeWavHeader(file: File, pcmLength: Long) {
        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            writeUInt32LE(raf, 36L + pcmLength)
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            writeIntLE(raf, 16)
            writeShortLE(raf, 1)
            writeShortLE(raf, 1)
            writeIntLE(raf, sampleRate)
            writeIntLE(raf, sampleRate * 2)
            writeShortLE(raf, 2)
            writeShortLE(raf, 16)
            raf.writeBytes("data")
            writeUInt32LE(raf, pcmLength)
        }
    }

    private fun writeUInt32LE(file: RandomAccessFile, value: Long) {
        repeat(4) { shift -> file.write(((value shr (shift * 8)) and 0xFFL).toInt()) }
    }

    private fun writeIntLE(file: RandomAccessFile, value: Int) {
        file.write(value and 0xFF)
        file.write((value shr 8) and 0xFF)
        file.write((value shr 16) and 0xFF)
        file.write((value shr 24) and 0xFF)
    }

    private fun writeShortLE(file: RandomAccessFile, value: Int) {
        file.write(value and 0xFF)
        file.write((value shr 8) and 0xFF)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(message: String): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(openApp)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(openApp)
                .setOngoing(true)
                .build()
        }
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(message))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopRequested = true
        runCatching {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) it.stop()
                it.release()
            }
        }
        audioRecord = null
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.voxera.app.action.START_RECORDING"
        const val ACTION_PAUSE = "com.voxera.app.action.PAUSE_RECORDING"
        const val ACTION_RESUME = "com.voxera.app.action.RESUME_RECORDING"
        const val ACTION_STOP = "com.voxera.app.action.STOP_RECORDING"
        private const val CHANNEL_ID = "voxera_recording"
        private const val NOTIFICATION_ID = 4101
        private const val SAMPLE_RATE = 44100
        private const val WAV_HEADER_SIZE = 44
        private const val WAVEFORM_CAPACITY = 48
        private const val MAX_WAV_DATA_BYTES = 0xFFFF_FFFFL - 36L
    }
}
