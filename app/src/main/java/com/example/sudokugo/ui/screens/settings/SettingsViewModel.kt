package com.example.sudokugo.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.ThemeRepository
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.data.repositories.VolumeRepository
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val theme: Theme
)

class SettingsViewModel(
    private val repository: ThemeRepository,
    private val repositoryUser: UserDSRepository,
    private val volumeRepository: VolumeRepository
): ViewModel() {

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    private val _userData = MutableStateFlow<UserServer?>(null)
    val userData: StateFlow<UserServer?> = _userData

    private val _volume = MutableStateFlow(0.5f)
    val volume: StateFlow<Float> = _volume

    init {
        viewModelScope.launch {
            repositoryUser.email
                .filterNotNull()
                .collect { savedEmail ->
                    _email.value = savedEmail
                    getUserData(savedEmail)
                }

            volumeRepository.volume.collect {
                _volume.value = it
            }
        }
    }
    val state = repository.theme.map { SettingsState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsState(Theme.System)
    )

    fun changeTheme(theme: Theme) = viewModelScope.launch {
        repository.setTheme(theme)
    }

    fun changeVolume(volume: Float) = viewModelScope.launch {
        volumeRepository.setVolume(volume)
    }

    private suspend fun getUserData(email: String) {
        try {
            val user = supabase.from("users")
                .select {
                    filter { eq("email", email) }
                }
                .decodeSingle<UserServer>()
            _userData.value = user

        } catch (e: Exception) {
            Log.e("GetUserData", "Errore durante la ricezione dei dati utente", e)
        }
    }
}