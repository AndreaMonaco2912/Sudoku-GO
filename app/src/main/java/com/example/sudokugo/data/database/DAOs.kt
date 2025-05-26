package com.example.sudokugo.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDAO{
    @Query("SELECT * FROM serversudoku")
    suspend fun getAll(): List<ServerSudoku>

    @Query("SELECT * FROM serversudoku WHERE id = :id")//TODO: it was flow don't know if now it's right
    suspend fun getById(id: Long): ServerSudoku

    @Insert
    suspend fun insert(sudoku: ServerSudoku): Long

    @Query("UPDATE serversudoku SET currentBoard = :newBoard WHERE id = :id")
    suspend fun update(id: Long, newBoard: String)

    @Delete
    suspend fun delete(sudoku: ServerSudoku)
}
@Dao
interface UserDAO{
    @Query("SELECT email FROM user")
    suspend fun getAllEmails(): List<String>

    @Query("SELECT * FROM user WHERE email = :email")
    suspend fun getByEmail(email: String): User

    @Upsert
    suspend fun upsert(user:User)

    @Delete
    suspend fun delete(user:User)

}