package com.example.sudokugo.ui.screens.details


import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
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
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            if(sudoku!=null){
                val minutes = sudoku.time!! / 60000
                val seconds = (sudoku.time % 60000) / 1000
                putExtra(Intent.EXTRA_TEXT, "Il "+  sudoku.solveDate.orEmpty() +
                        " ho risolto questo bellissimo sudoku! Ci ho messo solo "
                        +minutes
                        + " minuti e "
                        + seconds
                        + " secondi!"
                )
            }
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Sudoku")
        if (shareIntent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(shareIntent)
        }
    }


    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Sudoku Details") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.NONE) },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.tertiary,
                onClick = ::shareDetails
            ) {
                Icon(Icons.Outlined.Share, "Share Sudoku")
            }
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(contentPadding).padding(12.dp).fillMaxSize()
        ) {
            Image(
                Icons.Outlined.Image,
                "Sudoku picture",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(36.dp)
            )
            Text(
                "Sudoku " +
                sudokuId.toString(),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                if(sudoku!=null) sudoku.solveDate!! else "",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.size(8.dp))
            Text(
                if(sudoku!=null){
                    "Hai risolto questo sudoku di difficolta`: " + sudoku.difficulty
                }else ""
                ,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                if(sudoku!=null){
                    val minutes = sudoku.time!! / 60000
                    val seconds = (sudoku.time % 60000) / 1000
                    "Ci hai messo: " +
                            minutes +
                            " minuti e " +
                            seconds +
                    " secondi"
                }else ""
                ,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
