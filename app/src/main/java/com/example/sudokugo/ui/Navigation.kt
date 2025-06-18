package com.example.sudokugo.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.sudokugo.ui.screens.HomeScreen
import com.example.sudokugo.ui.screens.login.LoginScreen
import com.example.sudokugo.ui.screens.register.RegisterScreen
import com.example.sudokugo.ui.screens.settings.SettingsScreen
import com.example.sudokugo.ui.screens.solve.SolveScreen
import com.example.sudokugo.ui.screens.SudokuDetailsScreen
import com.example.sudokugo.ui.screens.user.UserScreen
import com.example.sudokugo.ui.screens.list.SudokuListScreen
import com.example.sudokugo.ui.screens.login.LoginViewModel
import com.example.sudokugo.ui.screens.register.RegisterViewModel
import com.example.sudokugo.ui.screens.settings.SettingsState
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import com.example.sudokugo.ui.screens.user.UserScreenViewModel
import kotlinx.serialization.Serializable

sealed interface SudokuGORoute {
    @Serializable data object Home: SudokuGORoute
    @Serializable data object SudokuList: SudokuGORoute
    @Serializable data class SudokuDetails(val sudokuId: String): SudokuGORoute
    @Serializable data object Login: SudokuGORoute
    @Serializable data object Register: SudokuGORoute
    @Serializable data object User: SudokuGORoute
    @Serializable data class Solve(val sudokuId: String? = null): SudokuGORoute
    @Serializable data class Settings(val userId: String): SudokuGORoute
}

@Composable
fun SudokuGONavGraph(navController: NavHostController,
                     settingsViewModel: SettingsViewModel,
                     themeState: SettingsState,
                     loginViewModel: LoginViewModel,
                     registerViewModel: RegisterViewModel,
                     userScreenViewModel: UserScreenViewModel
){
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
            LoginScreen(navController, loginViewModel::loginUser)
        }
        composable<SudokuGORoute.Register> {
            RegisterScreen(navController, registerViewModel)
        }
        composable<SudokuGORoute.User> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.User>()
            UserScreen(navController,  userScreenViewModel)
        }
        composable<SudokuGORoute.Solve> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Solve>()
            SolveScreen(navController, route.sudokuId)
        }
        composable<SudokuGORoute.Settings> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Settings>()
            SettingsScreen(navController, route.userId, themeState, settingsViewModel::changeTheme, loginViewModel::logoutUser)
        }

    }
}