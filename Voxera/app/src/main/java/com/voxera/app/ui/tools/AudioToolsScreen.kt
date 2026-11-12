package com.voxera.app.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxera.app.R
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.presentation.tools.AudioToolsViewModel
import java.util.Locale

private val ToolCard = Color(0xFF1A2731)
private val ToolBlue = Color(0xFF35BDEB)
private val ToolMuted = Color(0xFF9AAAB5)

@Composable
fun AudioToolsScreen() {
    val vm: AudioToolsViewModel = viewModel()
    val recordings by vm.recordings.collectAsState()
    val selected by vm.selectedRecording.collectAsState()
    val gain by vm.gainDb.collectAsState()
    val echo by vm.echoMix.collectAsState()
    val delay by vm.delayMs.collectAsState()
    val progress by vm.progress.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.tools_title), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.tools_subtitle), color = ToolMuted, fontSize = 13.sp)
        Text(stringResource(R.string.tools_choose_recording), color = Color.White, fontWeight = FontWeight.SemiBold)

        if (recordings.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = ToolCard), shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.tools_no_recordings), color = ToolMuted, modifier = Modifier.padding(18.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recordings, key = { it.path }) { item ->
                    RecordingChoice(item, selected?.path == item.path, enabled = progress == null) { vm.select(item) }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = ToolCard), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(stringResource(R.string.tools_amplifier), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.tools_gain_value, String.format(Locale.US, "%.1f", gain)), color = ToolBlue, fontSize = 13.sp)
                Slider(value = gain, onValueChange = vm::setGain, valueRange = 0f..24f, enabled = selected != null && progress == null)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.tools_echo), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.tools_echo_value, (echo * 100).toInt()), color = ToolBlue, fontSize = 13.sp)
                Slider(value = echo, onValueChange = vm::setEcho, valueRange = 0f..0.70f, enabled = selected != null && progress == null)
                Text(stringResource(R.string.tools_delay_value, delay.toInt()), color = ToolMuted, fontSize = 12.sp)
                Slider(value = delay, onValueChange = vm::setDelay, valueRange = 80f..700f, enabled = selected != null && progress == null)
            }
        }

        if (progress != null) {
            LinearProgressIndicator(progress = { (progress ?: 0) / 100f }, modifier = Modifier.fillMaxWidth(), color = ToolBlue)
            Text(stringResource(R.string.tools_processing_progress, progress ?: 0), color = ToolMuted, fontSize = 12.sp)
            TextButton(onClick = vm::cancel) { Text(stringResource(R.string.cancel), color = Color(0xFFFF6576)) }
        }

        error?.let { Text(stringResource(R.string.tools_processing_failed), color = Color(0xFFFF6576), fontSize = 12.sp) }
        when {
            message == "preview_ready" -> Text(stringResource(R.string.tools_preview_started), color = ToolBlue, fontSize = 12.sp)
            message?.startsWith("export_success:") == true -> Text(stringResource(R.string.tools_export_success), color = ToolBlue, fontSize = 12.sp)
            message == "operation_cancelled" -> Text(stringResource(R.string.tools_cancelled), color = ToolMuted, fontSize = 12.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = vm::preview,
                enabled = selected != null && progress == null,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ToolBlue, contentColor = Color(0xFF07131B))
            ) { Text(stringResource(R.string.tools_preview)) }
            Button(
                onClick = vm::export,
                enabled = selected != null && progress == null,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5267), contentColor = Color.White)
            ) { Text(stringResource(R.string.tools_export)) }
        }
        TextButton(onClick = vm::stopPreview) { Text(stringResource(R.string.tools_stop_preview), color = ToolMuted) }
        Text(stringResource(R.string.tools_original_safe_note), color = ToolMuted, fontSize = 11.sp)
    }
}

@Composable
private fun RecordingChoice(item: RecordingEntity, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF24485A) else ToolCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.tools_file_size, item.sizeBytes / 1024), color = ToolMuted, fontSize = 11.sp)
            }
            if (selected) Text(stringResource(R.string.tools_selected), color = ToolBlue, fontSize = 12.sp)
        }
    }
}
