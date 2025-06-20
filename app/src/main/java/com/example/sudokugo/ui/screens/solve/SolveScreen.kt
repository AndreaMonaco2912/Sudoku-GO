package com.example.sudokugo.ui.screens.solve

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SolveScreen(navController: NavController, sudokuId: Long? = null) {
    val sudokuViewModel = koinViewModel<SolveViewModel>()
    val timeDiff by sudokuViewModel.timeDiff.collectAsStateWithLifecycle()
    val sudokuDifficulty by sudokuViewModel.sudokuDifficulty.collectAsStateWithLifecycle()
    val shouldNavigate = remember { mutableStateOf(false) }

    val id by sudokuViewModel.id.collectAsStateWithLifecycle()

    LaunchedEffect(shouldNavigate.value) {
        if (shouldNavigate.value) {
            val diff = timeDiff
            val sudokuDiff = sudokuDifficulty
            if (diff != null && sudokuDiff != null) {
                navController.navigate(
                    SudokuGORoute.Congrats(
                        sudokuViewModel.getPointsForDifficulty(sudokuDiff),
                        diff,
                        id
                    )
                )
            }
            shouldNavigate.value = false
        }
    }

    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Sudoku") }
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

            BottomControls(
                sudokuViewModel,
                shouldNavigate
            )
            Text(
                text = "Difficulty: $sudokuDifficulty"
            )
        }
    }
}


@Composable
fun SudokuGrid(sudokuId: Long?, sudokuViewModel: SolveViewModel) {
    LaunchedEffect(sudokuId) {
        if (sudokuId == null) {
            sudokuViewModel.addSudoku()
        } else {
            sudokuViewModel.loadSudoku(sudokuId)
        }
    }

    val current = sudokuViewModel.currentSudoku.collectAsStateWithLifecycle().value
    val original = sudokuViewModel.originalSudoku.collectAsStateWithLifecycle().value
    val selected = sudokuViewModel.selectedCell.collectAsStateWithLifecycle().value

    if (current == null || original == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading Sudoku...", color = MaterialTheme.colorScheme.onBackground)
            }
        }
        return
    }

    val gridSize = remember { 9 }
    val cellSize = remember { 36.dp }
    val totalSize = remember { cellSize * gridSize }
    val onBack = MaterialTheme.colorScheme.onBackground
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.primaryContainer
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
                    color = Color(onPrimary.value),
                    topLeft = Offset(x = selCol * cellPx, y = selRow * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }

            for (i in 1 until gridSize) {
                val lineWidth = if (i % 3 == 0) thickLine else thinLine
                drawLine(
                    color = onBack,
                    start = Offset(i * cellPx, 0f),
                    end = Offset(i * cellPx, size.height),
                    strokeWidth = lineWidth
                )
            }

            for (i in 1 until gridSize) {
                val lineWidth = if (i % 3 == 0) thickLine else thinLine
                drawLine(
                    color = onBack,
                    start = Offset(0f, i * cellPx),
                    end = Offset(size.width, i * cellPx),
                    strokeWidth = lineWidth
                )
            }

            drawRect(
                onBack,
                style = Stroke(thickLine)
            )
        }
        Column {
            repeat(gridSize) { row ->
                Row {
                    repeat(gridSize) { col ->
                        val originalVal = original.board.get(col, row).value
                        val currentVal = current.board.get(col, row).value
                        val isFixed = originalVal != 0

                        val displayVal = if (currentVal != 0) currentVal.toString() else ""
                        val textColor = if (isFixed) onBack else primary

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
                    }
                }
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
                        NumberPadButton("🧼") {
                            onNumberClick(0)
                        }
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
            .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun BottomControls(sudokuViewModel: SolveViewModel, shouldNavigate: MutableState<Boolean>) {

    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { sudokuViewModel.restart() }) {
            Icon(Icons.Default.Refresh, contentDescription = "Restart")
        }

        IconButton(onClick = {
            if (sudokuViewModel.checkSolution()) shouldNavigate.value = true
            else Toast.makeText(context, "Sudoku not solved!", Toast.LENGTH_SHORT).show()

        }) {
            Icon(Icons.Default.Check, contentDescription = "Validate")
        }
    }
}