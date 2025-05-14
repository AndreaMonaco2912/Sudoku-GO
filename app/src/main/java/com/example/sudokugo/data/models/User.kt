package com.example.sudokugo.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val name: String,
    val username: String,
    val password: String
)