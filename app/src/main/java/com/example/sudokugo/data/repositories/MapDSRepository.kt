package com.example.sudokugo.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MapDSRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val ZOOM = doublePreferencesKey("zoom")
    }

    val mapData: Flow<Triple<Double, Double, Double>?> = dataStore.data.map { prefs ->
        Triple(
            prefs[LATITUDE] ?: 0.0,
            prefs[LONGITUDE] ?: 0.0,
            prefs[ZOOM] ?: 5.0
        )
    }


    suspend fun save(lat: Double, lon: Double, zoom: Double) {
        dataStore.edit { prefs ->
            prefs[LATITUDE] = lat
            prefs[LONGITUDE] = lon
            prefs[ZOOM] = zoom
        }
    }
}