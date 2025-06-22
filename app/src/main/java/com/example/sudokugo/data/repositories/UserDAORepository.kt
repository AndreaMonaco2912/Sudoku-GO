package com.example.sudokugo.data.repositories

import com.example.sudokugo.data.database.UserDAO
import com.example.sudokugo.data.database.User

class UserDAORepository(
    private val dao: UserDAO
) {
    suspend fun getAllEmails() = dao.getAllEmails()
    suspend fun upsert(user: User) = dao.upsert(user)
    suspend fun changePic(email: String, pic: String) = dao.changePic(email, pic)
    suspend fun getPictureByEmail(email: String) = dao.getPictureByEmail(email)
}