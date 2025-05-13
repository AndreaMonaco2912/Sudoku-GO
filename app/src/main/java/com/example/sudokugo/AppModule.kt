package com.example.sudokugo


import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.sudokugo.data.repositories.ThemeRepository
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val Context.dataStore by preferencesDataStore("theme")

val appModule = module {
    single { get<Context>().dataStore }
    single { ThemeRepository(get()) }
    viewModel { SettingsViewModel(get()) }
}