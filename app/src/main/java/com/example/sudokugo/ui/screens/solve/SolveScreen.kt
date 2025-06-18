package com.example.sudokugo.ui.screens.solve

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import org.koin.androidx.compose.koinViewModel

@Composable
fun SolveScreen(navController: NavController, sudokuId: String? = null) {
    val sudokuViewModel = koinViewModel<SolveViewModel>()
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "SudokuGO") }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            SudokuGrid(sudokuId, sudokuViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            NumberPad(onNumberClick = { sudokuViewModel.insertNum(it) })

            Spacer(modifier = Modifier.height(16.dp))

            BottomControls(sudokuViewModel)
        }
    }
}

@Composable
fun SudokuGrid(sudokuId: String?, sudokuViewModel: SolveViewModel) {
    if (sudokuId == null) {
        sudokuViewModel.addSudoku(Difficulty.EASY)//TODO: only when reached from home
    } else {
        sudokuViewModel.loadSudoku(sudokuId.toLong())//TODO: only when reached from list
    }

    val current = sudokuViewModel.currentSudoku.collectAsStateWithLifecycle().value
    val original = sudokuViewModel.originalSudoku.collectAsStateWithLifecycle().value
    val selected = sudokuViewModel.selectedCell.collectAsStateWithLifecycle().value

    if (current == null || original == null) throw NullPointerException("Sudoku non inizializzato")

    val gridSize = 9
    val cellSize = 36.dp
    val totalSize = cellSize * gridSize

    Box(
        modifier = Modifier
            .size(totalSize)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellPx = size.width / gridSize
            val thinLine = 1.dp.toPx()
            val thickLine = 3.dp.toPx()

            selected?.let { (selRow, selCol) ->
                drawRect(
                    color = Color(0x331976D2),
                    topLeft = Offset(x = selCol * cellPx, y = selRow * cellPx),
                    size = androidx.compose.ui.geometry.Size(cellPx, cellPx)
                )
            }

            for (i in 1 until gridSize) {
                val lineWidth = if (i % 3 == 0) thickLine else thinLine
                drawLine(
                    color = Color.Black,
                    start = Offset(i * cellPx, 0f),
                    end = Offset(i * cellPx, size.height),
                    strokeWidth = lineWidth
                )
            }

            for (i in 1 until gridSize) {
                val lineWidth = if (i % 3 == 0) thickLine else thinLine
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, i * cellPx),
                    end = Offset(size.width, i * cellPx),
                    strokeWidth = lineWidth
                )
            }

            drawRect(
                Color.Black,
                style = Stroke(thickLine))
        }
        Column{
        repeat(gridSize) { row ->
            Row {
                repeat(gridSize) { col ->
                    val originalVal = original.board.get(col, row).value
                    val currentVal = current.board.get(col, row).value
                    val isFixed = originalVal != 0

                    val displayVal = if (currentVal != 0) currentVal.toString() else ""
                    val textColor = if (isFixed) Color.Black else Color.Blue

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clickable(enabled = !isFixed) {
                                sudokuViewModel.selectCell(row, col)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = displayVal, color = textColor)
                    }
                }}
            }
        }
    }

}

@Composable
fun NumberPad(onNumberClick: (Int) -> Unit) {
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
                        NumberPadButton(numbers[index].toString()) {
                            onNumberClick(numbers[index])
                        }
                    } else if (index == 9) {
                        NumberPadButton("✏️") {
                            onNumberClick(0)
                        } // Pencil icon
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPadButton(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .padding(4.dp)
            .border(2.dp, Color.Black, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(label)
    }
}

@Composable
fun BottomControls(sudokuViewModel: SolveViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { sudokuViewModel.restart() }) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart")
        }

        IconButton(onClick = {
            if(sudokuViewModel.checkSolution())
                Log.d("Sudoku", "Solved")
        }) {
            Icon(Icons.Default.Check, contentDescription = "Validate")
        }
    }
}