package com.voxera.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.voxera.app.data.preferences.AppPreferences
import com.voxera.app.data.preferences.VoxeraThemeMode
import com.voxera.app.ui.VoxeraApp
import com.voxera.app.ui.theme.VoxeraTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appPreferences = AppPreferences(applicationContext)

        setContent {
            val themeMode by appPreferences.themeFlow.collectAsState(initial = VoxeraThemeMode.CLASSIC)
            VoxeraTheme(themeMode = themeMode) {
                VoxeraApp()
            }
        }

        lifecycleScope.launch {
            val language = appPreferences.getOrCreateLanguage()
            applyAppLanguage(language)
        }
    }

    private fun applyAppLanguage(language: String) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current != language) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        }
    }
}
