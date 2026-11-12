package com.voxera.app.ui.library

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxera.app.R
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.presentation.library.LibraryViewModel
import com.voxera.app.presentation.library.SortOrder
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Bg = androidx.compose.ui.graphics.Color(0xFF101820)
private val Accent = androidx.compose.ui.graphics.Color(0xFF35BDEB)

@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    val vm: LibraryViewModel = viewModel(key = "voxera_library") { LibraryViewModel(context.applicationContext) }
    val recordings by vm.recordings.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val sort by vm.sortOrder.collectAsState()
    val playingPath by vm.playingPath.collectAsState()
    val error by vm.error.collectAsState()
    var renameItem by remember { mutableStateOf<RecordingEntity?>(null) }
    var deleteItem by remember { mutableStateOf<RecordingEntity?>(null) }
    var detailsItem by remember { mutableStateOf<RecordingEntity?>(null) }

    Column(Modifier.fillMaxSize().background(Bg).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.library_title), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.library_count, recordings.size), color = Accent, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = vm::refresh) { Icon(Icons.Default.Refresh, stringResource(R.string.refresh), tint = Accent) }
            IconButton(onClick = vm::cycleSort) { Icon(Icons.Default.Sort, stringResource(R.string.sort), tint = Accent) }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = query, onValueChange = vm::setQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.search_recordings)) })
        Text(stringResource(sortLabel(sort)), color = Accent, modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
        if (recordings.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GraphicEq, null, tint = Accent, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(if (query.isBlank()) R.string.library_empty else R.string.library_no_results), color = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.library_empty_hint), color = androidx.compose.ui.graphics.Color.LightGray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp))
                Button(onClick = vm::refresh) { Text(stringResource(R.string.refresh)) }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recordings, key = { it.path }) { item ->
                    RecordingCard(item, playingPath == item.path,
                        onPlay = { vm.play(item) }, onRename = { renameItem = item }, onDelete = { deleteItem = item },
                        onDetails = { detailsItem = item }, onShare = { shareRecording(context, item) })
                }
            }
        }
    }

    renameItem?.let { item ->
        var name by remember(item.path) { mutableStateOf(item.name) }
        AlertDialog(onDismissRequest = { renameItem = null }, title = { Text(stringResource(R.string.rename_recording)) },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(stringResource(R.string.recording_name)) }) },
            confirmButton = { TextButton(onClick = { vm.rename(item, name); renameItem = null }) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { renameItem = null }) { Text(stringResource(R.string.cancel)) } })
    }
    deleteItem?.let { item ->
        AlertDialog(onDismissRequest = { deleteItem = null }, title = { Text(stringResource(R.string.delete_recording)) },
            text = { Text(stringResource(R.string.delete_recording_confirm, item.name)) },
            confirmButton = { TextButton(onClick = { vm.delete(item); deleteItem = null }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { deleteItem = null }) { Text(stringResource(R.string.cancel)) } })
    }
    detailsItem?.let { item ->
        AlertDialog(onDismissRequest = { detailsItem = null }, title = { Text(stringResource(R.string.recording_details)) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(stringResource(R.string.detail_name, item.name))
                Text(stringResource(R.string.detail_duration, formatDuration(item.durationMillis)))
                Text(stringResource(R.string.detail_size, formatSize(item.sizeBytes)))
                Text(stringResource(R.string.detail_date, DateFormat.getDateTimeInstance().format(Date(item.modifiedAt))))
                Text(stringResource(R.string.detail_format, "WAV / PCM 16-bit / 44.1 kHz / Mono"))
            } }, confirmButton = { TextButton(onClick = { detailsItem = null }) { Text(stringResource(R.string.close)) } })
    }
    error?.let { message -> AlertDialog(onDismissRequest = vm::clearError, title = { Text(stringResource(R.string.error_title)) }, text = { Text(message) }, confirmButton = { TextButton(onClick = vm::clearError) { Text(stringResource(R.string.close)) } }) }
}

@Composable
private fun RecordingCard(item: RecordingEntity, isPlaying: Boolean, onPlay: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit, onDetails: () -> Unit, onShare: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A2731)), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPlay) { Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, stringResource(if (isPlaying) R.string.stop_playback else R.string.play), tint = Accent) }
            Column(Modifier.weight(1f)) {
                Text(item.name, color = androidx.compose.ui.graphics.Color.White, maxLines = 1, fontWeight = FontWeight.SemiBold)
                Text("${formatDuration(item.durationMillis)}  •  ${formatSize(item.sizeBytes)}", color = androidx.compose.ui.graphics.Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onShare) { Icon(Icons.Default.Share, stringResource(R.string.share), tint = Accent) }
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, stringResource(R.string.more_actions), tint = androidx.compose.ui.graphics.Color.White) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { expanded = false; onRename() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.recording_details)) }, leadingIcon = { Icon(Icons.Default.Info, null) }, onClick = { expanded = false; onDetails() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
}

private fun shareRecording(context: android.content.Context, item: RecordingEntity) {
    val file = File(item.path)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply { type = "audio/wav"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_recording)))
}

@Composable private fun sortLabel(order: SortOrder): Int = when (order) {
    SortOrder.NEWEST -> R.string.sort_newest
    SortOrder.OLDEST -> R.string.sort_oldest
    SortOrder.NAME -> R.string.sort_name
    SortOrder.LARGEST -> R.string.sort_largest
}
private fun formatDuration(ms: Long) = String.format(Locale.US, "%02d:%02d", ms / 60_000, (ms / 1_000) % 60)
private fun formatSize(bytes: Long) = if (bytes < 1_048_576) String.format(Locale.US, "%.1f KB", bytes / 1024.0) else String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
