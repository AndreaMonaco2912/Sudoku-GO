package com.example.sudokugo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.User
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userDAORepository: UserDAORepository, private val userDSRepository: UserDSRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _errorMessage.value = "Insert an email"
            return@launch
        }
        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Insert a valid email"
            return@launch
        }
        if (password.isBlank()) {
            _errorMessage.value = "Insert a password"
            return@launch
        }
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            supabase.auth.sessionStatus.collect {
                when (it) {
                    is SessionStatus.Authenticated -> {
                        if (it.source is SessionSource.SignIn) {
                            _loginSuccess.value = true
                            val otherUsers = userDAORepository.getAllEmails()
                            if (!otherUsers.contains(email)) {
                                val localUser = User(
                                    email = email, profilePicture = null
                                )
                                userDAORepository.upsert(localUser)
                            }
                            userDSRepository.setUser(email)
                        }
                    }

                    SessionStatus.Initializing -> println("Initializing")
                    is SessionStatus.RefreshFailure -> println("Refresh failure ${it.cause}")
                    is SessionStatus.NotAuthenticated -> {
                        if (it.isSignOut) {
                            println("User signed out")
                        } else {
                            println("User not signed in")
                        }
                    }
                }
            }
        } catch (e: AuthRestException) {
            if (e.error == "invalid_credentials") {
                _errorMessage.value =
                    "Invalid credentials. Check your email and password and try again."

            } else {
                _errorMessage.value = "Registration failed: ${e.message}"
            }
        }
    }

    fun clearSuccess() {
        _loginSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logoutUser() = viewModelScope.launch {
        userDSRepository.clearEmail()
    }
}