package com.example.sudokugo.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.map

class VolumeRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val VOLUME_KEY =
            floatPreferencesKey("volume")
    }

    val volume = dataStore.data.map { preferences ->
        preferences[VOLUME_KEY] ?: 1.0f
    }

    suspend fun setVolume(volume: Float) =
        dataStore.edit { it[VOLUME_KEY] = volume }
}