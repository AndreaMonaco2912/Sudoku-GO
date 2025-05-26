package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.UserDAO
import com.example.sudokugo.data.database.User

class UserRepository(
    private val dao: UserDAO
) {
    suspend fun getAllEmails() = dao.getAllEmails()
    suspend fun getUserByEmail(email: String) = dao.getByEmail(email)
    suspend fun upsert(user: User) = dao.upsert(user)
    suspend fun delete(user: User) = dao.delete(user)
}