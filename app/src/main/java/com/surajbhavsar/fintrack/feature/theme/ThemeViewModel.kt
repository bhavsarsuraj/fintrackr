package com.surajbhavsar.fintrack.feature.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surajbhavsar.fintrack.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode { System, Light, Dark }

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.darkTheme
        .map { value ->
            when (value) {
                null -> ThemeMode.System
                true -> ThemeMode.Dark
                false -> ThemeMode.Light
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch {
            when (mode) {
                ThemeMode.System -> preferences.clearDarkTheme()
                ThemeMode.Light -> preferences.setDarkTheme(false)
                ThemeMode.Dark -> preferences.setDarkTheme(true)
            }
        }
    }
}
