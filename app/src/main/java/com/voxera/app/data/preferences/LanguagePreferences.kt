package com.voxera.app.data.preferences

import android.content.Context

class LanguagePreferences(context: Context) {
    private val appPreferences = AppPreferences(context)

    suspend fun getOrCreateLanguage(): String = appPreferences.getOrCreateLanguage()

    suspend fun setLanguage(language: String) = appPreferences.setLanguage(language)
}
