package com.example.sudokugo.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class ServerSudoku(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo val data: String,
    @ColumnInfo val currentBoard: String,
    @ColumnInfo val difficulty: String,
    @ColumnInfo val solution: String,
    @ColumnInfo val solved: Boolean = false,
    @ColumnInfo val userId: String,
    @ColumnInfo val picture: String?,
    @ColumnInfo val solveDate: Date?,
    @ColumnInfo val initTime: Date,
    @ColumnInfo val finishTime: Long?,
    @ColumnInfo val favourite: Boolean = false
)

@Entity
data class User(
    @PrimaryKey val email: String,
    @ColumnInfo val profilePicture: String?
)