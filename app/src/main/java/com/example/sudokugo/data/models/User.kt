package com.example.sudokugo.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserServer(
    val email: String,
    val name: String,
    val username: String,
    val password: String,
    val points: Long
)