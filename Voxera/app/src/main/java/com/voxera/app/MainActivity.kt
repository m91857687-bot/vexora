package com.voxera.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import com.voxera.app.data.preferences.LanguagePreferences
import com.voxera.app.ui.VoxeraApp
import com.voxera.app.ui.theme.VoxeraTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Render the first frame immediately; load the persisted locale without
        // keeping the Activity blank while DataStore initializes.
        setContent {
            VoxeraTheme {
                VoxeraApp()
            }
        }

        lifecycleScope.launch {
            val language = LanguagePreferences(applicationContext).getOrCreateLanguage()
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
