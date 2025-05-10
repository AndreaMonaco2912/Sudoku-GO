package com.example.sudokugo.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar

import com.example.sudokugo.ui.composables.TopSudokuGoAppBar


@Composable
fun HomeScreen(navController: NavController) {
    val fakeSudokuId = "Sudoku 12"
    val fakeUserId = "Pippo"
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "SudokuGO")},
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.PLAY) }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =  Modifier.padding(contentPadding).padding(12.dp)
        ) {
        }
    }
}
