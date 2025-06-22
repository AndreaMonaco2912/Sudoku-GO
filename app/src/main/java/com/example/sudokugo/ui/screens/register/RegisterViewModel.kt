package com.example.sudokugo.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    fun registerUser(email: String, name: String, username: String, password: String) =
        viewModelScope.launch {
            if (name.isBlank()) {
                _errorMessage.value = "Insert a name"
                return@launch
            }
            if (username.isBlank()) {
                _errorMessage.value = "Insert a username"
                return@launch
            }
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
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                supabase.auth.sessionStatus.collect {
                    when (it) {
                        is SessionStatus.Authenticated -> {
                            println("Received new authenticated session.")
                            if (it.source is SessionSource.SignUp) {
                                val newUser = UserServer(
                                    email = email,
                                    name = name,
                                    username = username,
                                    points = 0
                                )
                                try{
                                    supabase.from("users").insert(newUser)
                                    _errorMessage.value = null
                                    _registrationSuccess.value = true
                                }catch (e: PostgrestRestException){
                                    if(e.error == "duplicate_key_violation"){
                                        _errorMessage.value =
                                            "Email already registered. Try logging in or check your email."
                                    }else{
                                        _errorMessage.value = "Registration failed: ${e.message}"
                                    }
                                }
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
                when (e.error) {
                    "user_already_exists" -> {
                        _errorMessage.value =
                            "Email already registered. Try logging in or check your email."
                    }
                    "invalid_email" -> {
                        _errorMessage.value =
                            "Email invalid. Insert a valid one"
                    }
                    "weak_password" -> {
                        _errorMessage.value =
                            "Weak password. Insert a stronger one"
                    }
                    else -> {
                        _errorMessage.value = "Registration failed: ${e.message}"
                    }
                }
            }
        }

    fun clearSuccess() {
        _registrationSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
