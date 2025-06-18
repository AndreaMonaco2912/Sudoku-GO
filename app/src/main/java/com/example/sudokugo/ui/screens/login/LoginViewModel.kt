package com.example.sudokugo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.User
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val DAOrepository: UserDAORepository,
    private val userDSRepository: UserDSRepository
) : ViewModel() {

    fun loginUser(user: UserServer) = viewModelScope.launch {
        val otherUsers = DAOrepository.getAllEmails()
        if(!otherUsers.contains(user.email)){
            val localUser = User(
                email = user.email,
                profilePicture = null
            )
            DAOrepository.upsert(localUser)
        }
        userDSRepository.setUser(user.email)
    }
    fun logoutUser() = viewModelScope.launch {
        userDSRepository.clearEmail()
    }
}