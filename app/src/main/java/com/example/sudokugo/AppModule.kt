package com.example.sudokugo


import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.example.sudokugo.data.database.SudokuGODatabase
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.ThemeRepository
import com.example.sudokugo.data.repositories.UserPictureRepository
import com.example.sudokugo.data.repositories.UserRepository
import com.example.sudokugo.ui.SudokuViewModel
import com.example.sudokugo.ui.composables.profilePic.UserPictureViewModel
import com.example.sudokugo.ui.screens.login.LoginViewModel
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val Context.themeDataStore by preferencesDataStore(name = "theme")
val Context.userDataStore by preferencesDataStore(name = "user")
//val Context.dataStore by preferencesDataStore("theme")

val appModule = module {

    single {
        databaseBuilder(
            get(),
            SudokuGODatabase::class.java,
            "sudoku-go-database"
        ).build()
    }

    single { get<Context>().themeDataStore }
    single(named("user")) { get<Context>().userDataStore }
    single(named("userPicture")) { get<Context>().userDataStore }

    single { ThemeRepository(get()) }
    single { UserRepository(get(named("user"))) }
    single { UserPictureRepository(get(named("userPicture"))) }
    single { SudokuRepository(get<SudokuGODatabase>().sudokuDAO())}

    viewModel { SettingsViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { UserPictureViewModel(get()) }
    viewModel { SudokuViewModel(get()) }
//
//    single { get<Context>().dataStore }
//    single { ThemeRepository(get()) }
//    viewModel { SettingsViewModel(get()) }
}