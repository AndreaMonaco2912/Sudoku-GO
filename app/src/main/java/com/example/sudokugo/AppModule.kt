package com.example.sudokugo

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room.databaseBuilder
import com.example.sudokugo.data.database.SudokuGODatabase
import com.example.sudokugo.data.database.UserDAO
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.ThemeRepository
import com.example.sudokugo.data.repositories.UserPictureRepository
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import com.example.sudokugo.data.repositories.MapDSRepository
import com.example.sudokugo.map.classes.MapViewModel
import com.example.sudokugo.ui.composables.profilePic.UserPictureViewModel
import com.example.sudokugo.ui.screens.details.SudokuDetailsViewModel
import com.example.sudokugo.ui.screens.list.SudokuListViewModel
import com.example.sudokugo.ui.screens.login.LoginViewModel
import com.example.sudokugo.ui.screens.register.RegisterViewModel
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import com.example.sudokugo.ui.screens.solve.SolveViewModel
import com.example.sudokugo.ui.screens.user.UserScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val Context.themeDataStore by preferencesDataStore(name = "theme")
val Context.userDataStore by preferencesDataStore(name = "user")
val Context.mapPrefsDataStore by preferencesDataStore(name = "map_prefs")

val appModule = module {

    single {
        databaseBuilder(
            get(),
            SudokuGODatabase::class.java,
            "sudoku-go-database"
        ).fallbackToDestructiveMigration(true) //TODO: remove
            .build()
    }

    single(named("userDS")) { get<Context>().userDataStore }
    single { get<Context>().themeDataStore }
    single(named("userPicture")) { get<Context>().userDataStore }
    single { MapDSRepository(get<Context>().mapPrefsDataStore) }

    single { get<SudokuGODatabase>().userDAO() }
    single { ThemeRepository(get()) }
    single { UserDAORepository(get<UserDAO>()) }
    single { UserDSRepository(get(named("userDS"))) }
    single { UserPictureRepository(get(named("userPicture"))) }
    single { SudokuRepository(get<SudokuGODatabase>().sudokuDAO()) }

    viewModel { SettingsViewModel(get()) }
    viewModel { SolveViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel() }
    viewModel { UserPictureViewModel(get(), get()) }
    viewModel { SudokuListViewModel(get(), get()) }
    viewModel { UserScreenViewModel(get()) }
    viewModel { MapViewModel(get()) }
    viewModel { SudokuDetailsViewModel(get())}
    viewModel { MapViewModel(get()) }
}