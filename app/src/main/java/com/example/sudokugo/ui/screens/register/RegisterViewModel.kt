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
        try {
            // Validazione separata per ogni campo
            if (email.isBlank()) {
                _errorMessage.value = "Inserire un'email"
                return@launch
            }
            if (!email.contains("@") || !email.contains(".")) {
                _errorMessage.value = "Inserire un'email valida"
                return@launch
            }
            if (name.isBlank()) {
                _errorMessage.value = "Il nome non può essere vuoto"
                return@launch
            }
            if (username.isBlank()) {
                _errorMessage.value = "Lo username non può essere vuoto"
                return@launch
            }
            if (password.isBlank()) {
                _errorMessage.value = "La password non può essere vuota"
                return@launch
            }

            val result = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UserServer>()

            if (result.isNotEmpty()) {
                _errorMessage.value = "Utente con questa mail già inserita, cambiare mail"
                return@launch
            }

            // Nessun errore: registra l'utente
            val newUser = UserServer(email = email, name = name, username = username, password = password, points = 0)
            supabase.from("users").insert(newUser)

            _errorMessage.value = null // Tutto ok
            Log.d("Register", "Utente registrato con successo")

            _registrationSuccess.value = true

        } catch (e: Exception) {
            Log.e("Register", "Errore durante la registrazione", e)
            _errorMessage.value = "Errore imprevisto: ${e.localizedMessage}"
        }

    }
    fun clearSuccess() {
        _registrationSuccess.value = false
    }
    fun clearError() {
        _errorMessage.value = null
    }
}
