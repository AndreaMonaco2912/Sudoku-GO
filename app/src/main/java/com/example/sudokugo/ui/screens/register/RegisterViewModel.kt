package com.example.sudokugo.ui.screens.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    fun registerUser(email: String, name: String, username: String, password: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _errorMessage.value = "Insert an email"
            return@launch
        }
        if (!email.contains("@") || !email.contains(".")) {
            _errorMessage.value = "Insert a valid email"
            return@launch
        }
        if (name.isBlank()) {
            _errorMessage.value = "Insert a name"
            return@launch
        }
        if (username.isBlank()) {
            _errorMessage.value = "Insert a username"
            return@launch
        }
        if (password.isBlank()) {
            _errorMessage.value = "Insert a password"
            return@launch
        }
        try {
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UserServer>()

            if (result.isNotEmpty()) {
                _errorMessage.value = "Email already in use. Register with another email"
                return@launch
            }

            val newUser = UserServer(email = email, name = name, username = username, password = password, points = 0)
            supabase.from("users").insert(newUser)

            _errorMessage.value = null
            Log.d("Register", "User registered successfully")

            _registrationSuccess.value = true

        } catch (e: Exception) {
            Log.e("Register", "Error during registration", e)
            _errorMessage.value = "Unexpected error: ${e.localizedMessage}"
        }

    }
    fun clearSuccess() {
        _registrationSuccess.value = false
    }
    fun clearError() {
        _errorMessage.value = null
    }
}
