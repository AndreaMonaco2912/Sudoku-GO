package com.example.sudokugo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.Map

import com.example.sudokugo.ui.composables.TopSudokuGoAppBar


@Composable
fun HomeScreen(navController: NavController) {
    val fakeSudokuId = "Sudoku 12"
    val fakeUserId = "Pippo"
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "SudokuGO")},
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.PLAY) }
    ) { contentPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)) {
            Map()
        }
    }
}
