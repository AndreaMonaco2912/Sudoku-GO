package com.example.sudokugo.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sudokugo.data.models.User
import kotlinx.coroutines.flow.map

class UserRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val EMAIL_KEY =
            stringPreferencesKey("Email")
    }
    val user = dataStore.data.map {it[EMAIL_KEY] ?: ""}

    suspend fun setUser(user: User) =
        dataStore.edit { it[EMAIL_KEY] = user.email}

    suspend fun logoutUser() = dataStore.edit { it.remove(EMAIL_KEY) }
}