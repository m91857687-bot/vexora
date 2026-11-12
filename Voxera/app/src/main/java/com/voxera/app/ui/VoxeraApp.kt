package com.voxera.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voxera.app.R
import com.voxera.app.audio.RecordingStatus
import com.voxera.app.data.preferences.LanguagePreferences
import com.voxera.app.presentation.recording.RecordingViewModel
import com.voxera.app.ui.library.LibraryScreen
import com.voxera.app.ui.tools.AudioToolsScreen
import kotlinx.coroutines.launch
import java.util.Locale

private val SkyBlue = Color(0xFF35BDEB)
private val RecordRed = Color(0xFFFF4D61)
private val Background = Color(0xFF101820)
private val Muted = Color(0xFF9AAAB5)

private object Routes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
}

@Composable
fun VoxeraApp() {
    val navController = rememberNavController()
    val recordingViewModel: RecordingViewModel = viewModel()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val mainRoutes = setOf(Routes.HOME, Routes.LIBRARY, Routes.TOOLS)

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (currentRoute in mainRoutes) {
                NavigationBar(containerColor = Color(0xFF17232D)) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = { navController.navigate(Routes.HOME) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.LIBRARY,
                        onClick = { navController.navigate(Routes.LIBRARY) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = stringResource(R.string.nav_library)) },
                        label = { Text(stringResource(R.string.nav_library)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.TOOLS,
                        onClick = { navController.navigate(Routes.TOOLS) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { Icon(Icons.Default.GraphicEq, contentDescription = stringResource(R.string.nav_tools)) },
                        label = { Text(stringResource(R.string.nav_tools)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Routes.HOME, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.HOME) {
                HomeScreen(recordingViewModel, onSettingsClick = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.LIBRARY) { LibraryScreen() }
            composable(Routes.TOOLS) { AudioToolsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: RecordingViewModel, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.start(context)
        else viewModel.reportPermissionDenied(context.getString(R.string.microphone_permission_denied))
    }

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.start(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.app_name), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.home_brand_subtitle), color = SkyBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), tint = Color.White)
                }
            }

            Spacer(Modifier.height(34.dp))
            Text(stringResource(R.string.home_heading), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.home_subtitle), color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(38.dp))

            LiveWaveform(state.waveform, Modifier.fillMaxWidth().height(112.dp))
            Spacer(Modifier.height(18.dp))
            Text(formatDuration(state.elapsedMillis), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier.size(98.dp).background(Brush.linearGradient(listOf(RecordRed, Color(0xFFE83250))), CircleShape).padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        when (state.status) {
                            RecordingStatus.RECORDING -> viewModel.pause(context)
                            RecordingStatus.PAUSED -> viewModel.resume(context)
                            RecordingStatus.IDLE, RecordingStatus.STOPPED, RecordingStatus.ERROR -> startRecording()
                            else -> Unit
                        }
                    },
                    enabled = state.status !in setOf(RecordingStatus.STARTING, RecordingStatus.STOPPING),
                    modifier = Modifier.size(84.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed, contentColor = Color.White, disabledContainerColor = RecordRed.copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = when (state.status) {
                            RecordingStatus.RECORDING -> "Ⅱ"
                            RecordingStatus.PAUSED -> "▶"
                            RecordingStatus.STARTING, RecordingStatus.STOPPING -> "…"
                            else -> "●"
                        },
                        fontSize = 30.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(recordingStatusLabel(state.status), color = SkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

            if (state.status == RecordingStatus.RECORDING || state.status == RecordingStatus.PAUSED) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { viewModel.stop(context) }) {
                    Text(stringResource(R.string.stop_recording), color = RecordRed, fontSize = 15.sp)
                }
            }
            if (state.status == RecordingStatus.STOPPED && state.outputPath != null) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.recording_saved), color = SkyBlue, fontSize = 12.sp)
            }
            state.errorMessage?.let { message ->
                Spacer(Modifier.height(10.dp))
                Text(message, color = RecordRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(26.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeFeatureCard(stringResource(R.string.feature_library_title), stringResource(R.string.feature_library_subtitle), Modifier.weight(1f))
                HomeFeatureCard(stringResource(R.string.feature_tools_title), stringResource(R.string.feature_tools_subtitle), Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.home_footer), color = Color(0xFF637682), fontSize = 11.sp)
        }
    }
}

@Composable
private fun LiveWaveform(amplitudes: List<Float>, modifier: Modifier = Modifier) {
    val samples = if (amplitudes.isEmpty()) List(48) { 0.025f } else amplitudes
    Canvas(modifier = modifier) {
        val barCount = samples.size
        val gap = size.width * 0.012f
        val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(1f)
        val centerY = size.height / 2f
        samples.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude.coerceIn(0.025f, 1f) * size.height * 0.92f).coerceAtLeast(3f)
            val left = index * (barWidth + gap)
            drawRoundRect(
                color = if (index % 5 == 0) RecordRed else SkyBlue,
                topLeft = androidx.compose.ui.geometry.Offset(left, centerY - barHeight / 2f),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000L
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60L, totalSeconds % 60L)
}

@Composable
private fun recordingStatusLabel(status: RecordingStatus): String = when (status) {
    RecordingStatus.IDLE -> stringResource(R.string.recording_ready)
    RecordingStatus.STARTING -> stringResource(R.string.recording_starting)
    RecordingStatus.RECORDING -> stringResource(R.string.recording_active)
    RecordingStatus.PAUSED -> stringResource(R.string.recording_paused)
    RecordingStatus.STOPPING -> stringResource(R.string.recording_stopping)
    RecordingStatus.STOPPED -> stringResource(R.string.recording_finished)
    RecordingStatus.ERROR -> stringResource(R.string.recording_error)
}

@Composable
private fun HomeFeatureCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2731)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ComingSoonScreen(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(Background).padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(54.dp))
        Spacer(Modifier.height(20.dp))
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(description, color = Muted, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().background(Background).padding(22.dp)) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.back), color = SkyBlue) }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.settings_title), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.settings_description), color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(30.dp))
        Text(stringResource(R.string.language_title), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.language_description), color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        LanguageOption(
            title = stringResource(R.string.language_arabic), subtitle = stringResource(R.string.language_arabic_native),
            onClick = { scope.launch { LanguagePreferences(context).setLanguage("ar"); AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar")) } }
        )
        Spacer(Modifier.height(10.dp))
        LanguageOption(
            title = stringResource(R.string.language_english), subtitle = stringResource(R.string.language_english_native),
            onClick = { scope.launch { LanguagePreferences(context).setLanguage("en"); AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) } }
        )
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.settings_footer), color = Color(0xFF71838E), fontSize = 12.sp)
    }
}

@Composable
private fun LanguageOption(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2731)), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Text("›", color = SkyBlue, fontSize = 24.sp)
        }
    }
}
