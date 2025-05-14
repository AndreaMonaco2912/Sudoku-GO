package com.example.sudokugo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.data.models.User
import com.example.sudokugo.data.repositories.UserRepository
import com.example.sudokugo.ui.screens.settings.SettingsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserState(
    val email: String
)

class LoginViewModel(
    private val repository: UserRepository
) : ViewModel() {
    val state = repository.user.map { UserState(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UserState("")
    )

    fun setUser(user: User) = viewModelScope.launch {
        repository.setUser(user)
    }
    fun logoutUser() = viewModelScope.launch {
        repository.logoutUser()
    }
}