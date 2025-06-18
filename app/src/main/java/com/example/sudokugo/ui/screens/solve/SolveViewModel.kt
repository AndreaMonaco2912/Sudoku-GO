package com.example.sudokugo.ui.screens.solve

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.Sudoku
import io.github.ilikeyourhat.kudoku.parsing.createFromString
import io.github.ilikeyourhat.kudoku.parsing.fromSingleLineString
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
import java.security.KeyStore

class SolveViewModel(
    private val repository: SudokuRepository
) : ViewModel() {


    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()

    private var showedSudoku: ServerSudoku? = null

    private val _currentSudoku = MutableStateFlow<Sudoku?>(null)
    val currentSudoku: StateFlow<Sudoku?> = _currentSudoku

    private val _originalSudoku = MutableStateFlow<Sudoku?>(null)
    val originalSudoku: StateFlow<Sudoku?> = _originalSudoku

    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell

    private var solutionSudoku: String? = null

    fun selectCell(row: Int, col: Int) {
        _selectedCell.value = Pair(row, col)
    }

    fun loadSudoku(id: Long) {
        if (_currentSudoku.value != null) return
        viewModelScope.launch {
            showedSudoku = repository.fetchSudokuById(id)

            val currentBoard = showedSudoku?.currentBoard
                ?: throw NullPointerException("Nessun sudoku caricato")
            val originalBoard = showedSudoku?.data
                ?: throw NullPointerException("Nessun sudoku caricato")
            val solution = showedSudoku?.solution
                ?: throw NullPointerException("Nessun sudoku caricato")

            _currentSudoku.value = Sudoku.fromSingleLineString(currentBoard)
            _originalSudoku.value = Sudoku.fromSingleLineString(originalBoard)
            solutionSudoku = solution
        }
    }

    fun addSudoku(difficulty: Difficulty) {
        if (_currentSudoku.value != null) return
        viewModelScope.launch {
            val localSudoku = generator.generate(Classic9x9, difficulty)
            val solution = solver.solve(localSudoku)

            val boardStr = localSudoku.toSingleLineString()
            val solutionStr = solution.toSingleLineString()

            val sudoku = ServerSudoku(
                data = boardStr,
                currentBoard = boardStr,
                difficulty = difficulty.toString(),
                solution = solutionStr
            )
            _currentSudoku.value = localSudoku
            _originalSudoku.value = localSudoku
            solutionSudoku = solutionStr

            val id = repository.insertSudoku(sudoku)
            showedSudoku = sudoku.copy(id = id)
        }
    }

    fun deleteSudoku(sudoku: ServerSudoku) = viewModelScope.launch {
        repository.deleteSudoku(sudoku)
    }

    fun insertNum(num: Int) {
        val (row, col) = _selectedCell.value ?: return
        val original = _originalSudoku.value ?: return

        if (original.board.get(col, row).value != 0) return

        viewModelScope.launch {
            val current =
                _currentSudoku.value ?: throw IllegalStateException("Sudoku non inizializzato")
            val sudokuId = showedSudoku?.id ?: throw IllegalStateException("Nessun sudoku caricato")

            val currentBoardStr = current.toSingleLineString().toMutableList()
            val index = row * 9 + col
            currentBoardStr[index] = if (num in 1..9) num.digitToChar() else '0'

            val newBoardString = currentBoardStr.joinToString("")
            val updatedSudoku = Sudoku.fromSingleLineString(newBoardString)

            _currentSudoku.value = updatedSudoku

            repository.updateCurrentBoard(sudokuId, newBoardString)
        }
    }

    fun checkSolution(): Boolean {
        val solved = _currentSudoku.value!!.toSingleLineString() == solutionSudoku!!
        if (solved){
            viewModelScope.launch {
                repository.solveSudoku(showedSudoku!!.id)
            }
        }
        return solved
    }

    fun restart() {
        _currentSudoku.value = _originalSudoku.value
    }
}