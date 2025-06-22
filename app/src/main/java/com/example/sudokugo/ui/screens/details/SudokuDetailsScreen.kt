package com.example.sudokugo.ui.screens.details


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.R
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.composables.profilePic.PictureOrDefault
import com.example.sudokugo.util.formatDate
import org.koin.androidx.compose.koinViewModel


@Composable
fun SudokuDetailsScreen(navController: NavController, sudokuId: Long) {

    val viewModel = koinViewModel<SudokuDetailsViewModel>()
    val sudoku = viewModel.sudoku.collectAsState().value

    LaunchedEffect(sudokuId) {
        viewModel.loadSudoku(sudokuId)
    }

    val ctx = LocalContext.current
    fun shareDetails() {
        if (sudoku != null) {
            val minutes = sudoku.finishTime!! / 60000
            val seconds = (sudoku.finishTime % 60000) / 1000
            val date = sudoku.solveDate?.let { formatDate(it) }?.split(" ")?.get(0)
            val time = sudoku.solveDate?.let { formatDate(it) }?.split(" ")?.get(1)
            val text =
                "On $date at $time I solved a sudoku! It takes me only $minutes minutes and $seconds seconds!"

            val intent = Intent(Intent.ACTION_SEND).apply {
                sudoku.picture?.let { uriString ->
                    try {
                        val imageUri = Uri.parse(uriString)
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        type = "image/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_TEXT, text)
                    } catch (e: Exception) {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                } ?: run {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            }
            val shareIntent = Intent.createChooser(intent, "Share Sudoku")
            if (shareIntent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(shareIntent)
            }
        }
    }

    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Sudoku Details") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.NONE) },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                FloatingActionButton(containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        viewModel.changeFav()
                    }) {
                    if (sudoku != null) {
                        Icon(
                            if (!sudoku.favourite) Icons.Outlined.FavoriteBorder
                            else Icons.Outlined.Favorite, contentDescription = "Add to favorite"
                        )
                    }
                }
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiary, onClick = ::shareDetails
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share Sudoku")
                }
            }
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            if (sudoku != null) {
                PictureOrDefault(sudoku.picture, Modifier.size(200.dp), R.drawable.done)
            }
            Text(
                "Sudoku " + sudokuId.toString(), style = MaterialTheme.typography.titleLarge
            )
            Text(sudoku?.solveDate?.let { formatDate(it) } ?: "",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.size(8.dp))
            Text(
                if (sudoku != null) {
                    "You caught a sudoku with difficulty: " + sudoku.difficulty
                } else "", style = MaterialTheme.typography.bodyMedium
            )
            Text(
                if (sudoku != null) {
                    val minutes = sudoku.finishTime!! / 60000
                    val seconds = (sudoku.finishTime % 60000) / 1000
                    "It took you: " + minutes + " minutes and " + seconds + " seconds"
                } else "", style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
