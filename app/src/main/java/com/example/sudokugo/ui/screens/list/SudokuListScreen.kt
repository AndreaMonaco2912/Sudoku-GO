package com.example.sudokugo.ui.screens.list


import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.data.database.ServerSudoku

import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.R
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.composables.profilePic.PictureOrDefault
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel


@Composable
fun SudokuListScreen(navController: NavController) {
    val sudokuListViewModel = koinViewModel<SudokuListViewModel>()
    val sudokus = sudokuListViewModel.sudokuList.collectAsStateWithLifecycle().value
    val email by sudokuListViewModel.email.collectAsStateWithLifecycle()
    val fav = remember{mutableStateOf(false)}
    val filteredSudokus = if (fav.value) sudokus.filter { it.favourite  } else sudokus

    LaunchedEffect(Unit) {
        sudokuListViewModel.refreshList()
    }

    LaunchedEffect(email) {
        delay(100)
        if (email == null) {
            navController.navigate(SudokuGORoute.Login) {
                popUpTo(SudokuGORoute.SudokuList) { inclusive = true }
            }
        }
    }
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Sudoku List") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.COLLECTED) },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    fav.value = !fav.value
                }
            ) {
                Icon(
                    if(!fav.value)
                        Icons.Outlined.FavoriteBorder
                    else Icons.Outlined.Favorite,
                    contentDescription = "Show favourites")
            }
        }
    ) { contentPadding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
            modifier = Modifier.padding(contentPadding)
        ) {

            items(filteredSudokus.size) { index ->
                val sudoku = filteredSudokus[index]
                SudokuItem(
                    item = sudoku, // Make sure difficulty is a String or convert it
                    onClick = {
                        if (sudoku.solved)
                            navController.navigate(SudokuGORoute.SudokuDetails(sudoku.id))
                        else navController.navigate(SudokuGORoute.Solve(sudoku.id))
                    }
                )
            }

//            sudokus.forEach { sudoku -> {
//                SudokuItem(
//                    item = sudoku.difficulty,
//                    onClick = { navController.navigate(SudokuGORoute.SudokuDetails(sudoku.id.toString())) }
//                )
//            }

        }
    }
//            SudokuItem(
//                item = sudokuFromServer?.toString() ?: "No sudoku found",
//                onClick = { navController.navigate(SudokuGORoute.SudokuDetails("Sudoku 0")) }
//            )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuItem(item: ServerSudoku, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(150.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Image(
//                Icons.Outlined.Image,
//                "Sudoku picture",
//                contentScale = ContentScale.Fit,
//                colorFilter = ColorFilter.tint(color = if (item.solved) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant),
//                modifier = Modifier
//                    .size(72.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primaryContainer)
//                    .padding(20.dp)
//            )
            if(!item.solved){
                Image(
                    painter = painterResource(id = R.drawable.todo), // Usa l'avatar utente reale
                    contentDescription = "Sudoku to be resolved",
                    modifier = Modifier.size(72.dp)
                )
            }else{
                PictureOrDefault(item.picture, Modifier.size(72.dp), R.drawable.done)
            }
            Spacer(Modifier.size(8.dp))
            Text(
                "Sudoku ${item.id.toString()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
