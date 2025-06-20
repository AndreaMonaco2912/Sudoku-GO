package com.example.sudokugo.ui.screens.solve

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.UserDSRepository
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.Sudoku
import io.github.ilikeyourhat.kudoku.parsing.fromSingleLineString
import io.github.ilikeyourhat.kudoku.parsing.toSingleLineString
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import io.github.ilikeyourhat.kudoku.rating.defaultRater
import io.github.ilikeyourhat.kudoku.solving.defaultSolver
import io.github.ilikeyourhat.kudoku.type.Classic9x9
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date


class SolveViewModel(
    private val repository: SudokuRepository,
    private val repositoryUser: UserDSRepository
) : ViewModel() {
    private lateinit var initialTime: Date

    private val _sudokuDifficulty = MutableStateFlow<Difficulty>(Difficulty.EASY)
    val sudokuDifficulty: StateFlow<Difficulty?> = _sudokuDifficulty

    private val _timeDiff = MutableStateFlow<Long?>(null)
    val timeDiff: StateFlow<Long?> = _timeDiff

    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()
    private val rater = Sudoku.defaultRater()
    private var showedSudoku: ServerSudoku? = null

    private val _id = MutableStateFlow<Long>(-1)
    val id: StateFlow<Long> = _id

    private val _email = MutableStateFlow<String?>(null)

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

    init {
        viewModelScope.launch {
            repositoryUser.email.collect { savedEmail ->
                _email.value = savedEmail
            }
        }
    }

    fun loadSudoku(id: Long) {
        if (_currentSudoku.value != null) return
        viewModelScope.launch {
            showedSudoku = repository.fetchSudokuById(id)
            initialTime = repository.fetchSudokuById(id).initTime
            _sudokuDifficulty.value = Difficulty.valueOf(repository.fetchSudokuById(id).difficulty)

            val currentBoard = showedSudoku?.currentBoard
                ?: throw NullPointerException("Nessun sudoku caricato")
            val originalBoard = showedSudoku?.data
                ?: throw NullPointerException("Nessun sudoku caricato")
            val solution = showedSudoku?.solution
                ?: throw NullPointerException("Nessun sudoku caricato")
            val identifier = showedSudoku?.id
                ?: throw NullPointerException("Nessun sudoku caricato")

            _currentSudoku.value = Sudoku.fromSingleLineString(currentBoard)
            _originalSudoku.value = Sudoku.fromSingleLineString(originalBoard)
            _id.value = identifier
            solutionSudoku = solution
        }
    }

    fun addSudoku() {
        if (_currentSudoku.value != null) return
        viewModelScope.launch {
            Log.d("SolveViewModel", "addSudoku called")
            val email = _email.value ?: repositoryUser.email.firstOrNull()

            val result = withContext(Dispatchers.Default) {
                val localSudoku = generator.generate(Classic9x9)
                _sudokuDifficulty.value = rater.rate(localSudoku)
                val solution = solver.solve(localSudoku)
                val boardStr = localSudoku.toSingleLineString()
                val solutionStr = solution.toSingleLineString()

                initialTime = Calendar.getInstance().time

                val sudoku = ServerSudoku(
                    data = boardStr,
                    currentBoard = boardStr,
                    difficulty = _sudokuDifficulty.value.toString(),
                    solution = solutionStr,
                    userId = email ?: "default",
                    picture = null,
                    solveDate = null,
                    initTime = initialTime,
                    finishTime = null
                )

                val id = if (email != null) repository.insertSudoku(sudoku) else -1
                Triple(solution, localSudoku to sudoku.copy(id = id), solutionStr)

            }

            val (solution, pair, solutionStr) = result
            val (original, stored) = pair

            _currentSudoku.value = solution
            _originalSudoku.value = original
            _id.value = stored.id
            solutionSudoku = solutionStr
            showedSudoku = stored
        }
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
        if (solved) {
            val timeDiff = Calendar.getInstance().time.time - initialTime.time
            _timeDiff.value = timeDiff
            viewModelScope.launch {
                if (_email.value != null) {
                    repositoryUser.incrementScore(
                        _email.value!!,
                        getPointsForDifficulty(_sudokuDifficulty.value)
                    )
                }
                repository.solveSudoku(showedSudoku!!.id, Calendar.getInstance().time, timeDiff)
            }
        }
        return solved
    }

    fun getPointsForDifficulty(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.EASY -> 50
            Difficulty.MEDIUM -> 100
            Difficulty.HARD -> 150
            Difficulty.VERY_HARD -> 200
            Difficulty.UNSOLVABLE -> 1000
        }
    }

    fun restart() {
        _currentSudoku.value = _originalSudoku.value
    }
}