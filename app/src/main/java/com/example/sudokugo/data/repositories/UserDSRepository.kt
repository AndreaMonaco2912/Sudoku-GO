package com.example.sudokugo.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sudokugo.data.models.Theme
import kotlinx.coroutines.flow.map


class UserDSRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val EMAIL =
            stringPreferencesKey("userDS")
    }

    val email = dataStore.data.map { preferences ->
        preferences[EMAIL]
    }

    suspend fun setUser(email: String) =
        dataStore.edit { it[EMAIL] = email }

    suspend fun clearEmail() {
        dataStore.edit { it.remove(EMAIL) }
    }
}