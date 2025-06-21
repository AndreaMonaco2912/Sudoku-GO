package com.example.sudokugo.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.Map
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavController, setVolume: (Float) -> Unit) {
    val homeViewModel = koinViewModel<HomeScreenViewModel>()
    val volume by homeViewModel.volume.collectAsStateWithLifecycle()
    val localVolume = volume

    if (localVolume != null) {
        setVolume(localVolume)
    }
    val showMap = remember { mutableStateOf(true) }

    val playSudoku = {
        showMap.value = false
        navController.navigate(SudokuGORoute.Solve())
    }

    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "SudokuGO") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.PLAY) }
    ) { contentPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            if (showMap.value) {
                Map(playSudoku = playSudoku)
            }
        }
    }
}
