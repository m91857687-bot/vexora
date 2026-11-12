package com.voxera.app.presentation.library

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.data.recordings.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LibraryViewModel(context: Context) : ViewModel() {
    private val repository = RecordingRepository(context)
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SortOrder.NEWEST)
    private val mutableError = MutableStateFlow<String?>(null)
    private val mutablePlayingPath = MutableStateFlow<String?>(null)
    private var player: MediaPlayer? = null

    val error: StateFlow<String?> = mutableError
    val playingPath: StateFlow<String?> = mutablePlayingPath
    val searchQuery: StateFlow<String> = query
    val sortOrder: StateFlow<SortOrder> = sort

    val recordings: StateFlow<List<RecordingEntity>> = combine(repository.observeRecordings(), query, sort) { items, text, order ->
        val filtered = if (text.isBlank()) items else items.filter { it.name.contains(text.trim(), ignoreCase = true) }
        when (order) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.modifiedAt }
            SortOrder.OLDEST -> filtered.sortedBy { it.modifiedAt }
            SortOrder.NAME -> filtered.sortedBy { it.name.lowercase(Locale.ROOT) }
            SortOrder.LARGEST -> filtered.sortedByDescending { it.sizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    fun setQuery(value: String) { query.value = value }
    fun cycleSort() { sort.value = SortOrder.entries[(sort.value.ordinal + 1) % SortOrder.entries.size] }

    fun refresh() = viewModelScope.launch {
        runCatching { repository.refresh() }.onFailure { mutableError.value = it.message }
    }

    fun rename(item: RecordingEntity, name: String) = viewModelScope.launch {
        if (mutablePlayingPath.value == item.path) stopPlayback()
        runCatching { repository.rename(item, name) }
            .onFailure { mutableError.value = it.message }
    }

    fun delete(item: RecordingEntity) = viewModelScope.launch {
        if (mutablePlayingPath.value == item.path) stopPlayback()
        runCatching { repository.delete(item) }
            .onFailure { mutableError.value = it.message }
    }

    fun play(item: RecordingEntity) {
        if (mutablePlayingPath.value == item.path) { stopPlayback(); return }
        stopPlayback()
        runCatching {
            val mediaPlayer = MediaPlayer()
            player = mediaPlayer
            mutablePlayingPath.value = item.path
            mediaPlayer.setDataSource(item.path)
            mediaPlayer.setOnCompletionListener { stopPlayback() }
            mediaPlayer.setOnErrorListener { _, _, _ -> mutableError.value = "Playback failed"; stopPlayback(); true }
            mediaPlayer.setOnPreparedListener { prepared ->
                if (mutablePlayingPath.value == item.path) prepared.start()
            }
            mediaPlayer.prepareAsync()
        }.onFailure { mutableError.value = it.message ?: "Playback failed"; stopPlayback() }
    }

    fun stopPlayback() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        mutablePlayingPath.value = null
    }

    fun clearError() { mutableError.value = null }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }
}

enum class SortOrder { NEWEST, OLDEST, NAME, LARGEST }
