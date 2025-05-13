package com.example.sudokugo.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.data.repositories.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val theme: Theme
)

class SettingsViewModel(
    private val repository: ThemeRepository
): ViewModel() {
    val state = repository.theme.map { SettingsState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsState(Theme.System)
    )

    fun changeTheme(theme: Theme) = viewModelScope.launch {
        repository.setTheme(theme)
    }
}