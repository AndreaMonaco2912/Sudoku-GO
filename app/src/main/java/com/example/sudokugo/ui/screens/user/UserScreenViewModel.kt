package com.example.sudokugo.ui.screens.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class UserScreenViewModel(
    private val userDSRepository: UserDSRepository
) : ViewModel() {

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    private val _userData = MutableStateFlow<UserServer?>(null)
    val userData: StateFlow<UserServer?> = _userData

    private val _topUsers = MutableStateFlow<List<UserServer>?>(null)
    val topUsers: StateFlow<List<UserServer>?> = _topUsers

    init {
        viewModelScope.launch {
            userDSRepository.email.collect { savedEmail ->
                _email.value = savedEmail
                if (savedEmail != null) {
                    getUserData(savedEmail)
                } else {
                    _userData.value = null
                }
            }
        }
    }

    private suspend fun getUserData(email: String) {
        try {
            val user = supabase.from("users").select {
                    filter { eq("email", email) }
                }.decodeSingle<UserServer>()
            _userData.value = user

        } catch (e: Exception) {
            Log.e("GetUserData", "Error on getting data", e)
        }
    }

    fun getTopUsers() {
        viewModelScope.launch {
            try {
                val topUsers = supabase.from("users").select {
                        order(column = "points", order = Order.DESCENDING)
                        limit(5)
                    }.decodeList<UserServer>()

                _topUsers.value = topUsers

            } catch (e: Exception) {
                Log.e("Query", "Error on getting data", e)
            }
        }

    }
}