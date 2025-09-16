package com.maintainer.app.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "maintainer_settings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val DEFAULT_DARK_MODE = true // Default to dark mode
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        sharedPreferences.edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()
    }

    fun getDarkModePreference(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
    }
}