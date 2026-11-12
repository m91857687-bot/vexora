package com.voxera.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.voxeraDataStore by preferencesDataStore(name = "voxera_settings")

enum class VoxeraThemeMode(val key: String) {
    CLASSIC("classic"),
    MIDNIGHT("midnight"),
    ROYAL("royal"),
    SYSTEM("system");

    companion object {
        fun fromKey(key: String?): VoxeraThemeMode =
            entries.find { it.key == key } ?: CLASSIC
    }
}

class AppPreferences(private val context: Context) {
    private val languageKey = stringPreferencesKey("app_language")
    private val themeKey = stringPreferencesKey("app_theme")

    val themeFlow: Flow<VoxeraThemeMode> = context.voxeraDataStore.data.map { prefs ->
        VoxeraThemeMode.fromKey(prefs[themeKey])
    }

    suspend fun getOrCreateLanguage(): String {
        val stored = context.voxeraDataStore.data.first()[languageKey]
        if (stored == "ar" || stored == "en") return stored

        val systemLanguage = Locale.getDefault().language
        val initialLanguage = if (systemLanguage.equals("ar", ignoreCase = true)) "ar" else "en"
        context.voxeraDataStore.edit { preferences ->
            preferences[languageKey] = initialLanguage
        }
        return initialLanguage
    }

    suspend fun setLanguage(language: String) {
        require(language == "ar" || language == "en") { "Unsupported language: $language" }
        context.voxeraDataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }

    suspend fun getTheme(): VoxeraThemeMode {
        val stored = context.voxeraDataStore.data.first()[themeKey]
        return VoxeraThemeMode.fromKey(stored)
    }

    suspend fun setTheme(theme: VoxeraThemeMode) {
        context.voxeraDataStore.edit { preferences ->
            preferences[themeKey] = theme.key
        }
    }
}
