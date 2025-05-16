package com.example.sudokugo.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ServerSudoku(
    val id: Int,
    val data: String,
    val solution: String
)