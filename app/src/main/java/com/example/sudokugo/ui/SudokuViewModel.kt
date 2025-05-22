package com.example.sudokugo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.Sudoku
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import io.github.ilikeyourhat.kudoku.solving.defaultSolver
import io.github.ilikeyourhat.kudoku.type.Classic9x9
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SudokuState(val sudokus: List<ServerSudoku>)

class SudokuViewModel(
    private val repository: SudokuRepository
) : ViewModel() {
    val state = repository.sudokus.map { SudokuState(sudokus = it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SudokuState(emptyList())
    )

    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()

    private var showedSudoku: ServerSudoku? = null

    private val _currentSudoku = MutableStateFlow<List<List<Char>>?>(null)
    val sudokuFromServer: StateFlow<List<List<Char>>?> = _currentSudoku

    private val _originalSudoku = MutableStateFlow<List<List<Char>>?>(null)
    val originalSudoku: StateFlow<List<List<Char>>?> = _originalSudoku

    private val _sudokusFromServer = MutableStateFlow<List<ServerSudoku>>(emptyList())
    val sudokusFromServer: StateFlow<List<ServerSudoku>> = _sudokusFromServer

    fun loadSudoku(id: Long) {
        viewModelScope.launch {
            showedSudoku = repository.fetchSudokuById(id)
            _currentSudoku.value = parseBoard(showedSudoku?.currentBoard ?: throw IllegalStateException("Nessun sudoku caricato"))
            _originalSudoku.value = parseBoard(showedSudoku?.data ?: throw IllegalStateException("Nessun sudoku caricato"))
        }
    }

    fun allSudokus() {
        viewModelScope.launch {
            repository.fetchAllSudoku().collect { list ->
                _sudokusFromServer.value = list
            }
        }
    }

    fun addSudoku(difficulty: Difficulty) = viewModelScope.launch {
        val sudokuStr = generator.generate(Classic9x9, difficulty)
        val solution = solver.solve(sudokuStr)
        val sudoku = ServerSudoku(
            data = sudokuStr.toString(),
            currentBoard = sudokuStr.toString(),
            difficulty = difficulty.toString(),
            solution = solution.toString()
        )
        _currentSudoku.value = parseBoard(sudokuStr.toString())
        _originalSudoku.value = parseBoard(sudokuStr.toString())

        val id = repository.insertSudoku(sudoku)
        showedSudoku = sudoku.copy(id = id)
    }

    fun deleteSudoku(sudoku: ServerSudoku) = viewModelScope.launch {
        repository.deleteSudoku(sudoku)
    }

    fun insertNum(row: Int, col: Int, num: Int) {
        viewModelScope.launch {
            val board = _currentSudoku.value ?: throw IllegalStateException("Sudoku non inizializzato")
            val sudokuId = showedSudoku?.id ?: throw IllegalStateException("Nessun sudoku caricato")

            if (row !in board.indices || col !in board[row].indices)
                throw IndexOutOfBoundsException("Posizione ($row, $col) fuori dai limiti.")

            val updatedBoard = board.mapIndexed { r, line ->
                if (r == row) {
                    line.toMutableList().also { it[col] = num.toString().first() }
                } else {
                    line
                }
            }

            _currentSudoku.value = updatedBoard

            val boardString = updatedBoard.joinToString("|", prefix = "|", postfix = "|") { it.joinToString("") }
            repository.updateCurrentBoard(sudokuId, boardString)
        }
    }


    private fun parseBoard(board: String): List<List<Char>> {
        return board
            .split("|")
            .filter { it.isNotEmpty() }
            .map { row -> row.toList() }
    }

}