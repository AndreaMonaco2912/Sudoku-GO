package com.example.sudokugo.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sudoku(
    @PrimaryKey val id: Int,
    @ColumnInfo val data: String,
    @ColumnInfo val solution: String
)

@Entity
data class User(
    @PrimaryKey val email: String,
    @ColumnInfo val name: String,
    @ColumnInfo val username: String,
    @ColumnInfo val password: String
)