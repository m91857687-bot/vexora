package com.voxera.app.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxera.app.R
import com.voxera.app.data.recordings.RecordingEntity
import com.voxera.app.presentation.tools.AudioToolsViewModel
import java.util.Locale

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

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            stringResource(R.string.tools_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.tools_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            stringResource(R.string.tools_choose_recording),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )

        if (recordings.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("tools_no_recordings_card")
            ) {
                Text(
                    stringResource(R.string.tools_no_recordings),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
                    .testTag("tools_recording_selector"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(recordings, key = { it.path }) { item ->
                    RecordingChoice(
                        item = item,
                        selected = selected?.path == item.path,
                        enabled = progress == null
                    ) {
                        vm.select(item)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().testTag("tools_controls_card")
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text(
                    stringResource(R.string.tools_amplifier),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.tools_gain_value, String.format(Locale.US, "%.1f", gain)),
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("gain_value_text")
                )
                Slider(
                    value = gain,
                    onValueChange = vm::setGain,
                    valueRange = 0f..24f,
                    enabled = selected != null && progress == null,
                    modifier = Modifier.testTag("slider_gain")
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.tools_echo),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.tools_echo_value, (echo * 100).toInt()),
                    color = primaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("echo_value_text")
                )
                Slider(
                    value = echo,
                    onValueChange = vm::setEcho,
                    valueRange = 0f..0.70f,
                    enabled = selected != null && progress == null,
                    modifier = Modifier.testTag("slider_echo")
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.tools_delay_value, delay.toInt()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag("delay_value_text")
                )
                Slider(
                    value = delay,
                    onValueChange = vm::setDelay,
                    valueRange = 80f..700f,
                    enabled = selected != null && progress == null,
                    modifier = Modifier.testTag("slider_delay")
                )
            }
        }

        if (progress != null) {
            LinearProgressIndicator(
                progress = { (progress ?: 0) / 100f },
                modifier = Modifier.fillMaxWidth().testTag("processing_progress_bar"),
                color = primaryColor
            )
            Text(
                stringResource(R.string.tools_processing_progress, progress ?: 0),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            TextButton(
                onClick = vm::cancel,
                modifier = Modifier.testTag("tools_cancel_button")
            ) {
                Text(stringResource(R.string.cancel), color = secondaryColor, fontWeight = FontWeight.SemiBold)
            }
        }

        error?.let {
            Text(
                stringResource(R.string.tools_processing_failed),
                color = secondaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag("tools_error_text")
            )
        }
        when {
            message == "preview_ready" -> Text(
                stringResource(R.string.tools_preview_started),
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            message?.startsWith("export_success:") == true -> Text(
                stringResource(R.string.tools_export_success),
                color = primaryColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            message == "operation_cancelled" -> Text(
                stringResource(R.string.tools_cancelled),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = vm::preview,
                enabled = selected != null && progress == null,
                modifier = Modifier.weight(1f).height(48.dp).testTag("tools_preview_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.tools_preview), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = vm::export,
                enabled = selected != null && progress == null,
                modifier = Modifier.weight(1f).height(48.dp).testTag("tools_export_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryColor,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.tools_export), fontWeight = FontWeight.SemiBold)
            }
        }
        TextButton(
            onClick = vm::stopPreview,
            modifier = Modifier.testTag("tools_stop_preview_button")
        ) {
            Text(stringResource(R.string.tools_stop_preview), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            stringResource(R.string.tools_original_safe_note),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RecordingChoice(item: RecordingEntity, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("choice_${item.name}")
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    stringResource(R.string.tools_file_size, item.sizeBytes / 1024),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            if (selected) {
                Text(
                    stringResource(R.string.tools_selected),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
