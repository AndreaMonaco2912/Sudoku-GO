package com.example.sudokugo.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDateTime
import java.util.Date

@Dao
interface SudokuDAO{
    @Query("SELECT * FROM serversudoku WHERE userId = :email")
    suspend fun getAllByUser(email: String?): List<ServerSudoku>

    @Query("SELECT * FROM serversudoku WHERE id = :id")//TODO: it was flow don't know if now it's right
    suspend fun getById(id: Long): ServerSudoku

    @Insert
    suspend fun insert(sudoku: ServerSudoku): Long

    @Query("UPDATE serversudoku SET currentBoard = :newBoard WHERE id = :id")
    suspend fun update(id: Long, newBoard: String)

    @Delete
    suspend fun delete(sudoku: ServerSudoku)

    @Query("UPDATE serversudoku SET solved = 1, solveDate = :solveDate,  finishTime= :finishTime WHERE id = :id")
    suspend fun solve(id: Long, solveDate: Date, finishTime: Long)

    @Query("UPDATE serversudoku SET picture = :newPic WHERE id = :id")
    suspend fun changePic(id: Long, newPic: String)
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

    @Query("UPDATE user SET profilePicture = :pic WHERE email = :email")
    suspend fun changePic(email: String, pic: String)

    @Query("SELECT profilePicture FROM user WHERE email = :email")
    suspend fun getPictureByEmail(email: String): String?
}