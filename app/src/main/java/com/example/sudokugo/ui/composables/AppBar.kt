package com.example.sudokugo.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.profilePic.UserPicture
import com.example.sudokugo.ui.composables.profilePic.UserPictureViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSudokuGoAppBar(navController: NavController, title: String?) {
    CenterAlignedTopAppBar(
        title = {
        if (title != null) {
            Text(
                title,
                fontWeight = FontWeight.Medium,
            )
        }
    }, navigationIcon = {
        if (title == "Sudoku Details" || title == "Settings") {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go Back")
            }
        }
    }, actions = {
        if (title != "Settings" && title != "Login" && title != "Register" && title != "Sudoku") {
            IconButton(onClick = { navController.navigate(SudokuGORoute.Settings()) }) {
                Icon(Icons.Outlined.Settings, "Settings")
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    )
}

@Composable
fun BottomSudokuGoAppBar(navController: NavController, selected: BottomNavSelected) {
    val userPictureViewModel = koinViewModel<UserPictureViewModel>()
    val userPic by userPictureViewModel.userPic.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BottomAppBar(
        actions = {
            NavigationBarItem(
                selected = selected == BottomNavSelected.PLAY,
                onClick = {
                    Log.d("BottomNav", currentRoute!!)
                    if(currentRoute != "com.example.sudokugo.ui.SudokuGORoute.Home") navController.navigate(SudokuGORoute.Home)
                          },
                icon = {
                    Box(Modifier.padding(8.dp)) {
                        Icon(Icons.Outlined.Place, contentDescription = null)
                    }
                },
                label = {
                    Text(text = "Explore")
                },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selected == BottomNavSelected.COLLECTED,
                onClick = { navController.navigate(SudokuGORoute.SudokuList) },
                icon = {
                    Box(Modifier.padding(8.dp)) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = null)
                    }
                },
                label = {
                    Text(text = "Collected")
                },
                alwaysShowLabel = true
            )
            NavigationBarItem(
                selected = selected == BottomNavSelected.USER,
                onClick = { navController.navigate(SudokuGORoute.User) },
                icon = {
                    Box(Modifier.padding(8.dp)) {
                        UserPicture(userPic, Modifier.size(24.dp))
                    }
                },
                label = {
                    Text(text = "User")
                },
                alwaysShowLabel = true
            )
        })
}

enum class BottomNavSelected {
    PLAY, COLLECTED, USER, NONE
}