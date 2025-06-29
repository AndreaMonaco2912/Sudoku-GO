package com.example.sudokugo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.sudokugo.ui.screens.home.HomeScreen
import com.example.sudokugo.ui.screens.login.LoginScreen
import com.example.sudokugo.ui.screens.register.RegisterScreen
import com.example.sudokugo.ui.screens.settings.SettingsScreen
import com.example.sudokugo.ui.screens.solve.SolveScreen
import com.example.sudokugo.ui.screens.details.SudokuDetailsScreen
import com.example.sudokugo.ui.screens.user.UserScreen
import com.example.sudokugo.ui.screens.list.SudokuListScreen
import com.example.sudokugo.ui.screens.login.LoginViewModel
import com.example.sudokugo.ui.screens.register.RegisterViewModel
import com.example.sudokugo.ui.screens.settings.SettingsState
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import com.example.sudokugo.ui.screens.solve.CongratsScreen
import com.example.sudokugo.ui.screens.user.UserScreenViewModel
import kotlinx.serialization.Serializable

sealed interface SudokuGORoute {
    @Serializable
    data object Home : SudokuGORoute

    @Serializable
    data object SudokuList : SudokuGORoute

    @Serializable
    data object Login : SudokuGORoute

    @Serializable
    data object Register : SudokuGORoute

    @Serializable
    data object User : SudokuGORoute

    @Serializable
    data object Settings : SudokuGORoute

    @Serializable
    data class Solve(val sudokuId: Long? = null) : SudokuGORoute

    @Serializable
    data class Congrats(val points: Int, val duration: Long, val sudokuId: Long) : SudokuGORoute

    @Serializable
    data class SudokuDetails(val sudokuId: Long) : SudokuGORoute
}

@Composable
fun SudokuGONavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    themeState: SettingsState,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    userScreenViewModel: UserScreenViewModel,
    setVolume: (Float) -> Unit
) {
    NavHost(
        navController = navController, startDestination = SudokuGORoute.Home
    ) {
        composable<SudokuGORoute.Home> {
            HomeScreen(navController, setVolume)
        }
        composable<SudokuGORoute.SudokuList> {
            SudokuListScreen(navController)
        }
        composable<SudokuGORoute.SudokuDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.SudokuDetails>()
            SudokuDetailsScreen(navController, route.sudokuId)
        }
        composable<SudokuGORoute.Login> {
            LoginScreen(navController)
        }
        composable<SudokuGORoute.Register> {
            RegisterScreen(navController, registerViewModel)
        }
        composable<SudokuGORoute.User> {
            UserScreen(navController, userScreenViewModel)
        }
        composable<SudokuGORoute.Solve> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Solve>()
            SolveScreen(navController, route.sudokuId)
        }
        composable<SudokuGORoute.Settings> {
            SettingsScreen(
                navController,
                themeState,
                settingsViewModel::changeTheme,
                loginViewModel::logoutUser,
                setVolume
            )
        }
        composable<SudokuGORoute.Congrats> { backStackEntry ->
            val route = backStackEntry.toRoute<SudokuGORoute.Congrats>()
            CongratsScreen(
                navController, route.points, route.duration, route.sudokuId
            )
        }
    }
}