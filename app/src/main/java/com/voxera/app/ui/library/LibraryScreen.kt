package com.voxera.app.ui.library

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.library_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.library_count, recordings.size),
                    color = primaryColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("recordings_count")
                )
            }
            IconButton(
                onClick = vm::refresh,
                modifier = Modifier.testTag("library_refresh_button").size(48.dp)
            ) {
                Icon(Icons.Default.Refresh, stringResource(R.string.refresh), tint = primaryColor)
            }
            IconButton(
                onClick = vm::cycleSort,
                modifier = Modifier.testTag("library_sort_button").size(48.dp)
            ) {
                Icon(Icons.Default.Sort, stringResource(R.string.sort), tint = primaryColor)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = vm::setQuery,
            modifier = Modifier.fillMaxWidth().testTag("library_search_input"),
            singleLine = true,
            label = { Text(stringResource(R.string.search_recordings)) }
        )
        Text(
            stringResource(sortLabel(sort)),
            color = primaryColor,
            modifier = Modifier.padding(vertical = 8.dp).testTag("library_sort_indicator"),
            style = MaterialTheme.typography.labelMedium
        )
        if (recordings.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.GraphicEq, null, tint = primaryColor, modifier = Modifier.size(54.dp))
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(if (query.isBlank()) R.string.library_empty else R.string.library_no_results),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.library_empty_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = vm::refresh,
                    modifier = Modifier.testTag("library_empty_refresh_button")
                ) {
                    Text(stringResource(R.string.refresh))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.testTag("recordings_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recordings, key = { it.path }) { item ->
                    RecordingCard(
                        item = item,
                        isPlaying = playingPath == item.path,
                        onPlay = { vm.play(item) },
                        onRename = { renameItem = item },
                        onDelete = { deleteItem = item },
                        onDetails = { detailsItem = item },
                        onShare = { shareRecording(context, item) }
                    )
                }
            }
        }
    }

    renameItem?.let { item ->
        var name by remember(item.path) { mutableStateOf(item.name) }
        AlertDialog(
            onDismissRequest = { renameItem = null },
            title = { Text(stringResource(R.string.rename_recording)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.recording_name)) },
                    modifier = Modifier.testTag("rename_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.rename(item, name); renameItem = null },
                    modifier = Modifier.testTag("rename_save_button")
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameItem = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text(stringResource(R.string.delete_recording)) },
            text = { Text(stringResource(R.string.delete_recording_confirm, item.name)) },
            confirmButton = {
                TextButton(
                    onClick = { vm.delete(item); deleteItem = null },
                    modifier = Modifier.testTag("delete_confirm_button")
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    detailsItem?.let { item ->
        AlertDialog(
            onDismissRequest = { detailsItem = null },
            title = { Text(stringResource(R.string.recording_details)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.detail_name, item.name))
                    Text(stringResource(R.string.detail_duration, formatDuration(item.durationMillis)))
                    Text(stringResource(R.string.detail_size, formatSize(item.sizeBytes)))
                    Text(stringResource(R.string.detail_date, DateFormat.getDateTimeInstance().format(Date(item.modifiedAt))))
                    Text(stringResource(R.string.detail_format, "WAV / PCM 16-bit / 44.1 kHz / Mono"))
                }
            },
            confirmButton = {
                TextButton(onClick = { detailsItem = null }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    error?.let { message ->
        AlertDialog(
            onDismissRequest = vm::clearError,
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = vm::clearError) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

@Composable
private fun RecordingCard(
    item: RecordingEntity,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    onShare: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("recording_card_${item.name}")
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onPlay,
                modifier = Modifier.testTag("play_button_${item.name}").size(48.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = stringResource(if (isPlaying) R.string.stop_playback else R.string.play),
                    tint = primaryColor
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    item.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    "${formatDuration(item.durationMillis)}  •  ${formatSize(item.sizeBytes)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier.testTag("share_button_${item.name}").size(48.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.share),
                    tint = primaryColor
                )
            }
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.testTag("more_button_${item.name}").size(48.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { expanded = false; onRename() },
                        modifier = Modifier.testTag("menu_rename_${item.name}")
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.recording_details)) },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        onClick = { expanded = false; onDetails() },
                        modifier = Modifier.testTag("menu_details_${item.name}")
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.secondary) },
                        onClick = { expanded = false; onDelete() },
                        modifier = Modifier.testTag("menu_delete_${item.name}")
                    )
                }
            }
        }
    }
}

private fun shareRecording(context: Context, item: RecordingEntity) {
    val file = File(item.path)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "audio/wav"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_recording)))
}

@Composable
private fun sortLabel(order: SortOrder): Int = when (order) {
    SortOrder.NEWEST -> R.string.sort_newest
    SortOrder.OLDEST -> R.string.sort_oldest
    SortOrder.NAME -> R.string.sort_name
    SortOrder.LARGEST -> R.string.sort_largest
}

private fun formatDuration(ms: Long) = String.format(Locale.US, "%02d:%02d", ms / 60_000, (ms / 1_000) % 60)
private fun formatSize(bytes: Long) = if (bytes < 1_048_576) String.format(Locale.US, "%.1f KB", bytes / 1024.0) else String.format(Locale.US, "%.2f MB", bytes / 1_048_576.0)
