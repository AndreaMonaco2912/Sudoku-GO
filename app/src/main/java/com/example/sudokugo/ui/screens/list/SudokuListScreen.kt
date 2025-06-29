package com.example.sudokugo.ui.screens.list


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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.sudokugo.ui.composables.Loading
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.composables.profilePic.PictureOrDefault
import org.koin.androidx.compose.koinViewModel


@Composable
fun SudokuListScreen(navController: NavController) {
    val sudokuListViewModel = koinViewModel<SudokuListViewModel>()
    val sudokuList = sudokuListViewModel.sudokuList.collectAsStateWithLifecycle().value
    val email by sudokuListViewModel.email.collectAsStateWithLifecycle()
    val emailRead by sudokuListViewModel.emailRead.collectAsStateWithLifecycle()

    val fav = remember { mutableStateOf(false) }
    val filteredSudoku = if (fav.value) sudokuList.filter { it.favourite } else sudokuList

    if (!emailRead) {
        Loading("Loading collected sudoku")
        return
    }

    if (email == null) {
        navController.navigate(SudokuGORoute.Login) {
            popUpTo(SudokuGORoute.SudokuList) { inclusive = true }
        }
        return
    }

    LaunchedEffect(Unit) {
        sudokuListViewModel.refreshList()
    }

    Scaffold(topBar = { TopSudokuGoAppBar(navController, title = "Sudoku List") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.COLLECTED) },
        floatingActionButton = {
            FloatingActionButton(containerColor = MaterialTheme.colorScheme.secondary, onClick = {
                fav.value = !fav.value
            }) {
                Icon(
                    if (!fav.value) Icons.Outlined.FavoriteBorder
                    else Icons.Outlined.Favorite, contentDescription = "Show favorites"
                )
            }
        }) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
            modifier = Modifier.padding(contentPadding)
        ) {
            if (sudokuList.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No sudoku caught (yet)!", style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Click on Explore and catch 'em all!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (filteredSudoku.isEmpty() && fav.value) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No favourites!", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Add sudoku favourites to see them here.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "Use the like button in the sudoku details to add it to your favourites.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            items(filteredSudoku.size) { index ->
                val sudoku = filteredSudoku[index]
                SudokuItem(item = sudoku, onClick = {
                    if (sudoku.solved) navController.navigate(SudokuGORoute.SudokuDetails(sudoku.id))
                    else navController.navigate(SudokuGORoute.Solve(sudoku.id))
                })
            }
        }
    }
}

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
            if (!item.solved) {
                Image(
                    painter = painterResource(id = R.drawable.todo),
                    contentDescription = "Sudoku to be resolved",
                    modifier = Modifier.size(72.dp)
                )
            } else {
                PictureOrDefault(item.picture, Modifier.size(72.dp), R.drawable.done)
            }
            Spacer(Modifier.size(8.dp))
            Text(
                "Sudoku ${item.id}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
