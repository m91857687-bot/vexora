package com.voxera.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Locale

private val Context.settingsDataStore by preferencesDataStore(name = "voxera_settings")

class LanguagePreferences(private val context: Context) {
    private val languageKey = stringPreferencesKey("app_language")

    suspend fun getOrCreateLanguage(): String {
        val stored = context.settingsDataStore.data.first()[languageKey]
        if (stored == "ar" || stored == "en") return stored

        val systemLanguage = Locale.getDefault().language
        val initialLanguage = if (systemLanguage.equals("ar", ignoreCase = true)) "ar" else "en"
        context.settingsDataStore.edit { preferences ->
            preferences[languageKey] = initialLanguage
        }
        return initialLanguage
    }

    suspend fun setLanguage(language: String) {
        require(language == "ar" || language == "en") { "Unsupported language: $language" }
        context.settingsDataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }
}
