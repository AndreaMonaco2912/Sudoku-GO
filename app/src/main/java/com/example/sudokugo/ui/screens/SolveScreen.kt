package com.example.sudokugo.ui.screens

import android.util.Log
import io.github.ilikeyourhat.kudoku.model.Sudoku
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.R
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.SudokuType
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import io.github.ilikeyourhat.kudoku.type.Classic9x9

@Composable
fun SolveScreen(navController: NavController, sudokuId: String) {
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = sudokuId) }
    ){  contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // Sudoku Grid
            SudokuGrid()

            Spacer(modifier = Modifier.height(16.dp))

            // Number Pad
            NumberPad()

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Controls
            BottomControls()
        }
    }
}

@Composable
fun SudokuGrid() {
    val generator = Sudoku.defaultGenerator()
    val sudoku = generator.generate(type = Classic9x9, difficulty = Difficulty.VERY_HARD)
    Log.d("cacca", sudoku.toString())
    val gridSize = 9
    val cellSize = 36.dp
    val highlightedCells = listOf(Pair(0, 0), Pair(2, 1), Pair(5, 3)) // Example positions

    Column(
        modifier = Modifier
            .border(2.dp, Color.Blue)
    ) {
        repeat(gridSize) { row ->
            Row {
                repeat(gridSize) { col ->
                    val isHighlighted = highlightedCells.contains(Pair(row, col))
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .border(0.5.dp, Color.Gray)
                            .background(if (isHighlighted) Color(0xFFDDE9F5) else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "") // Placeholder for number
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad() {
    val numbers = (1..9).toList()
    Column {
        for (row in 0 until 2) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 5) {
                    val index = row * 5 + i
                    if (index < numbers.size) {
                        NumberPadButton(numbers[index].toString())
                    } else if (index == 9) {
                        NumberPadButton("✏️") // Pencil icon
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPadButton(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .padding(4.dp)
            .border(2.dp, Color.Black, CircleShape)
    ) {
        Text(label)
    }
}

@Composable
fun BottomControls() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Restart */ }) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart")
        }

        // Placeholder for user avatar
        Image(
            painter = painterResource(id = R.drawable.character_icon), // Replace with your avatar
            contentDescription = "User",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
        )

        IconButton(onClick = { /* Validate */ }) {
            Icon(Icons.Default.Check, contentDescription = "Validate")
        }
    }
}