package com.example.sudokugo.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.supabase
import com.example.sudokugo.util.UserState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class UserScreenViewModel(
    private val userDSRepository: UserDSRepository
) : ViewModel() {

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    private val _state = MutableStateFlow<Int?>(null)
    val state : StateFlow<Int?> = _state

    private val _userData = MutableStateFlow<UserServer?>(null)
    val userData: StateFlow<UserServer?> = _userData

    init {
        viewModelScope.launch {
            userDSRepository.email.collect { savedEmail ->
                _email.value = savedEmail
                if (savedEmail != null) {
                    getUserData(savedEmail)
                }
            }
        }
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

//    private suspend fun getUserPoints(email: String) {
//        try {
//            val points = supabase.from("users")
//                .select(columns = Columns.raw("points")) {
//                    filter { eq("email", email) }
//                }
//                .decodeSingle<Map<String, Int>>()["points"]
//            _state.value = points
//
//        } catch (e: Exception) {
//            Log.e("GetPoints", "Errore durante la ricezione dei punti", e)
//        }
//    }
}