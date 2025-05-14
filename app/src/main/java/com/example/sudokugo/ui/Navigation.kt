package com.example.sudokugo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.sudokugo.ui.screens.HomeScreen
import com.example.sudokugo.ui.screens.LoginScreen
import com.example.sudokugo.ui.screens.RegisterScreen
import com.example.sudokugo.ui.screens.settings.SettingsScreen
import com.example.sudokugo.ui.screens.SolveScreen
import com.example.sudokugo.ui.screens.SudokuDetailsScreen
import com.example.sudokugo.ui.screens.SudokuListScreen
import com.example.sudokugo.ui.screens.UserScreen
import com.example.sudokugo.ui.screens.settings.SettingsState
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import kotlinx.serialization.Serializable

sealed interface SudokuGORoute {
    @Serializable data object Home: SudokuGORoute
    @Serializable data object SudokuList: SudokuGORoute
    @Serializable data class SudokuDetails(val sudokuId: String): SudokuGORoute
    @Serializable data object Login: SudokuGORoute
    @Serializable data object Register: SudokuGORoute
    @Serializable data class User(val userId: String): SudokuGORoute
    @Serializable data class Solve(val sudokuId: String): SudokuGORoute
    @Serializable data class Settings(val userId: String): SudokuGORoute
}

@Composable
fun SudokuGONavGraph(navController: NavHostController, settingsViewModel: SettingsViewModel, themeState: SettingsState){
    NavHost(
        navController = navController,
        startDestination = SudokuGORoute.Home
    ){
        composable<SudokuGORoute.Home>{
            HomeScreen(navController)
        }
        composable<SudokuGORoute.SudokuList>{
            SudokuListScreen(navController)
        }
        composable<SudokuGORoute.SudokuDetails>{ backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.SudokuDetails>()
            SudokuDetailsScreen(navController, route.sudokuId)
        }
        composable<SudokuGORoute.Login>{
            LoginScreen(navController)
        }
        composable<SudokuGORoute.Register> {
            RegisterScreen(navController)
        }
        composable<SudokuGORoute.User> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.User>()
            UserScreen(navController, route.userId)
        }
        composable<SudokuGORoute.Solve> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Solve>()
            SolveScreen(navController, route.sudokuId)
        }
        composable<SudokuGORoute.Settings> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Settings>()
            SettingsScreen(navController, route.userId, themeState, settingsViewModel::changeTheme)
        }

    }
}