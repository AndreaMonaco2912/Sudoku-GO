package com.example.sudokugo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ServerSudoku::class, User::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class SudokuGODatabase : RoomDatabase() {
    abstract fun sudokuDAO(): SudokuDAO
    abstract fun userDAO(): UserDAO
}