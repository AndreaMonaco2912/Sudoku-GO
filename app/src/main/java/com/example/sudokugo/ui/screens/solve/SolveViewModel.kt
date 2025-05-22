package com.example.sudokugo.ui.screens.solve

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.ui.SudokuState
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.Board
import io.github.ilikeyourhat.kudoku.model.Cell
import io.github.ilikeyourhat.kudoku.model.Sudoku
import io.github.ilikeyourhat.kudoku.parsing.createFromString
import io.github.ilikeyourhat.kudoku.parsing.toSingleLineString
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import io.github.ilikeyourhat.kudoku.solving.defaultSolver
import io.github.ilikeyourhat.kudoku.type.Classic9x9
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SolveViewModel (
    private val repository: SudokuRepository
) : ViewModel() {


    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()

    private var showedSudoku: ServerSudoku? = null

    private val _currentSudoku = MutableStateFlow<Sudoku?>(null)
    val currentSudoku: StateFlow<Sudoku?> = _currentSudoku

    private val _originalSudoku = MutableStateFlow<Sudoku?>(null)
    val originalSudoku: StateFlow<Sudoku?> = _originalSudoku


    fun loadSudoku(id: Long) {
        viewModelScope.launch {
            showedSudoku = repository.fetchSudokuById(id)
            _currentSudoku.value = Sudoku.createFromString(showedSudoku?.currentBoard ?: throw NullPointerException("Nessun sudoku caricato"))
            _originalSudoku.value = Sudoku.createFromString(showedSudoku?.data ?: throw NullPointerException("Nessun sudoku caricato"))
        }
    }

    fun addSudoku(difficulty: Difficulty) = viewModelScope.launch {
        val localSudoku = generator.generate(Classic9x9, difficulty)
        val solution = solver.solve(localSudoku)
        val sudokuBoard = localSudoku
        val sudoku = ServerSudoku(
            data = sudokuBoard.toSingleLineString(),
            currentBoard = sudokuBoard.toSingleLineString(),
            difficulty = difficulty.toString(),
            solution = solution.toSingleLineString()
        )
        _currentSudoku.value = sudokuBoard
        _originalSudoku.value = sudokuBoard
        Log.d("board", sudokuBoard.toSingleLineString())

        val id = repository.insertSudoku(sudoku)
        showedSudoku = sudoku.copy(id = id)
    }

    fun deleteSudoku(sudoku: ServerSudoku) = viewModelScope.launch {
        repository.deleteSudoku(sudoku)
    }

    fun insertNum(row: Int, col: Int, num: Int) {
        viewModelScope.launch {
            val current = _currentSudoku.value ?: throw IllegalStateException("Sudoku non inizializzato")
            val sudokuId = showedSudoku?.id ?: throw IllegalStateException("Nessun sudoku caricato")

            val currentBoardStr = current.toSingleLineString().toMutableList()
            val index = row * 9 + col
            currentBoardStr[index] = if (num in 0..9) num.digitToChar() else '0'

            val newBoardString = currentBoardStr.joinToString("")
            val updatedSudoku = Sudoku.createFromString(newBoardString)

            _currentSudoku.value = updatedSudoku

            repository.updateCurrentBoard(sudokuId, newBoardString)
        }
    }


    private fun parseBoard(board: String): List<List<Char>> {
        return board
            .split("|")
            .filter { it.isNotEmpty() }
            .map { row -> row.toList() }
    }

}