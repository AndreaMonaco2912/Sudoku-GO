package com.example.sudokugo.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ServerSudoku(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo val data: String,
    @ColumnInfo val currentBoard: String,
    @ColumnInfo val difficulty: String,
    @ColumnInfo val solution: String,
    @ColumnInfo val solved: Boolean = false
)

@Entity
data class User(
    @PrimaryKey val email: String,
    @ColumnInfo val name: String,
    @ColumnInfo val username: String,
    @ColumnInfo val password: String,
    @ColumnInfo val profilePicture: String?
)