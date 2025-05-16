package com.example.sudokugo.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDAO{
    @Query("SELECT * FROM sudoku")
    fun getAll(): Flow<List<Sudoku>>

    @Query("SELECT * FROM sudoku WHERE id = :id")
    fun getById(id: Int): Flow<Sudoku>

    @Upsert
    suspend fun upsert(sudoku: Sudoku)

    @Delete
    suspend fun delete(sudoku: Sudoku)
}
@Dao
interface UserDAO{
    @Query("SELECT * FROM user WHERE email = :email")
    fun getByEmail(email: String): Flow<User>

    @Upsert
    suspend fun upsert(user:User)

    @Delete
    suspend fun delete(user:User)

}