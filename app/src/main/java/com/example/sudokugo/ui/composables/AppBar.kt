package com.example.sudokugo.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sudokugo.R
import com.example.sudokugo.ui.SudokuGORoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSudokuGoAppBar(navController: NavController, title: String) {
    val fakeUserId = "1"
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.Medium,
            )
        },
        navigationIcon = {
            if (title == "Sudoku Details" || title == "Settings") {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go Back")
                }
            }
        },
        actions = {
            if (title == "Sudoku List") {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
            }
            if (title != "Settings" && title != "Login" && title != "Register") {
                IconButton(onClick = { navController.navigate(SudokuGORoute.Settings(fakeUserId)) }) {
                    Icon(Icons.Outlined.Settings, "Settings")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun BottomSudokuGoAppBar(navController: NavController, selected: BottomNavSelected) {
    val fakeUserId = "1"
    BottomAppBar(
        actions = {
            NavigationBarItem(
                selected = selected == BottomNavSelected.PLAY,
                onClick = { navController.navigate(SudokuGORoute.Home) },
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
                onClick = { navController.navigate(SudokuGORoute.User(userId = fakeUserId)) },
                icon = {
                    Box(Modifier.padding(8.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.character_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                label = {
                    Text(text = "User")
                },
                alwaysShowLabel = true
            )
        }
    )
}

enum class BottomNavSelected {
    PLAY,
    COLLECTED,
    USER,
    NONE
}