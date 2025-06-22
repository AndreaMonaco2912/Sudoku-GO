package com.example.sudokugo.data.repositories

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.supabase
import io.github.jan.supabase.postgrest.from
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

    suspend fun incrementScore(email: String, points: Int) {
        try {
            val user = supabase.from("users")
                .select() {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UserServer>()

            val newScore = user[0].points + points
            supabase.from("users")
                .update(mapOf("points" to newScore)) {
                    filter {
                        eq("email", email)
                    }
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Error incrementing score: ${e.message}")
        }
    }
}