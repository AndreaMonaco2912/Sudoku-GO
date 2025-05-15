package com.example.sudokugo.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

class UserPictureRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val USER_PIC_KEY =
            stringPreferencesKey("UserPic")
    }

    val userPic = dataStore.data.map { it[USER_PIC_KEY] ?: "" }

    suspend fun setUserPic(userPic: String) =
        dataStore.edit { it[USER_PIC_KEY] = userPic }
}