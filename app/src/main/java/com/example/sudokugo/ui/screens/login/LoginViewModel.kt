package com.example.sudokugo.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.User
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.supabase
import com.example.sudokugo.ui.SudokuGORoute
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val DAOrepository: UserDAORepository,
    private val userDSRepository: UserDSRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _loginSucces = MutableStateFlow(false)
    val loginSucces: StateFlow<Boolean> = _loginSucces

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
            val result = supabase.from("users")
                .select(){
                    filter {
                        eq("email", email)
                        eq("password", password)
                    }
                }
                .decodeList<UserServer>()

            if (result.isNotEmpty()) {
                _loginSucces.value = true
                val otherUsers = DAOrepository.getAllEmails()
                if(!otherUsers.contains(email)){
                    val localUser = User(
                        email = email,
                        profilePicture = null
                    )
                    DAOrepository.upsert(localUser)
                }
                userDSRepository.setUser(email)
            } else {

                _errorMessage.value = "Wrong email or password"
                return@launch
            }
        } catch (e: Exception) {
            Log.e("Login", "Error during login", e)
            _errorMessage.value = "Unexpected error: ${e.localizedMessage}"
        }

    }
    fun clearSuccess() {
        _loginSucces.value = false
    }
    fun clearError() {
        _errorMessage.value = null
    }

    fun logoutUser() = viewModelScope.launch {
        userDSRepository.clearEmail()
    }
}