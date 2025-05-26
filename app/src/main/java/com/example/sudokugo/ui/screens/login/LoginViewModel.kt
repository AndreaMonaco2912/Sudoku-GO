package com.example.sudokugo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.database.User
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.UserRepository
import com.example.sudokugo.ui.screens.settings.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserState(
    val email: String
)

class LoginViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail


    fun loginUser(user: UserServer) = viewModelScope.launch {
        val otherUsers = repository.getAllEmails()
        if(otherUsers.contains(user.email)){
            _userEmail.value = user.email
        }else{
            val localUser = User(
                email = user.email,
                name = user.name,
                username = user.username,
                password = user.password,
                profilePicture = null
                )
            repository.upsert(localUser)
        }
    }
    fun logoutUser() = viewModelScope.launch {
        _userEmail.value = null
    }
}