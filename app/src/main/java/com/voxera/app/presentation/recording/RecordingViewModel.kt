package com.voxera.app.presentation.recording

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.voxera.app.audio.RecordingService
import com.voxera.app.audio.RecordingSession
import com.voxera.app.audio.RecordingStatus
import com.voxera.app.R

class RecordingViewModel : ViewModel() {
    val state = RecordingSession.state

    fun start(context: Context) {
        RecordingSession.set(com.voxera.app.audio.RecordingUiState(status = RecordingStatus.STARTING))
        send(context, RecordingService.ACTION_START, foreground = true)
    }

    fun pause(context: Context) = send(context, RecordingService.ACTION_PAUSE)
    fun resume(context: Context) = send(context, RecordingService.ACTION_RESUME)
    fun stop(context: Context) = send(context, RecordingService.ACTION_STOP)

    fun reportPermissionDenied(message: String) {
        RecordingSession.set(com.voxera.app.audio.RecordingUiState(status = RecordingStatus.ERROR, errorMessage = message))
    }

    private fun send(context: Context, action: String, foreground: Boolean = false) {
        val intent = Intent(context, RecordingService::class.java).setAction(action)
        try {
            if (foreground) ContextCompat.startForegroundService(context, intent)
            else context.startService(intent)
        } catch (error: Exception) {
            RecordingSession.set(
                com.voxera.app.audio.RecordingUiState(
                    status = RecordingStatus.ERROR,
                    errorMessage = context.getString(R.string.recording_service_unavailable)
                )
            )
        }
    }
}
