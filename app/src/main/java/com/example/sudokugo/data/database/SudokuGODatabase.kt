package com.example.sudokugo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Sudoku::class, User::class], version = 1)
abstract class SudokuGODatabase : RoomDatabase() {
    abstract fun sudokuDAO(): SudokuDAO
    abstract fun userDAO(): UserDAO
}