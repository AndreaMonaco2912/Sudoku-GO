package com.example.sudokugo.ui.screens.solve

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
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
import io.github.ilikeyourhat.kudoku.solving.defaultSolver
import io.github.ilikeyourhat.kudoku.type.Classic9x9
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.time.Duration
import java.util.Calendar
import java.util.Date


class SolveViewModel(
    private val repository: SudokuRepository,
    private val repositoryUser: UserDSRepository
) : ViewModel() {
    private lateinit var initialTime: Date
    private lateinit var sudokuDiff: Difficulty

    private val _timeDiff = MutableStateFlow<Long?>(null)
    val timeDiff: StateFlow<Long?> = _timeDiff

    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()

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
            sudokuDiff = Difficulty.valueOf(repository.fetchSudokuById(id).difficulty)

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

    fun addSudoku(difficulty: Difficulty) {
        if (_currentSudoku.value != null) return
        viewModelScope.launch {
            Log.d("SolveViewModel", "addSudoku called")
            val email = _email.value ?: repositoryUser.email.firstOrNull()
            val result = withContext(Dispatchers.Default) {
                sudokuDiff = difficulty
                val localSudoku = generator.generate(Classic9x9)
                val solution = solver.solve(localSudoku)

                val boardStr = localSudoku.toSingleLineString()
                val solutionStr = solution.toSingleLineString()

                initialTime = Calendar.getInstance().time
                val sudoku = ServerSudoku(
                    data = boardStr,
                    currentBoard = boardStr,
                    difficulty = difficulty.toString(),
                    solution = solutionStr,
                    userId = email,
                    picture = null,
                    solveDate = null,
                    initTime = initialTime,
                    finishTime = null
                )

                val id = repository.insertSudoku(sudoku)
                Triple(solution, localSudoku to sudoku.copy(id = id), solutionStr)
            }

            val (solution, pair, solutionStr) = result
            val (original, stored) = pair

            _currentSudoku.value = solution
            _originalSudoku.value = original
            _id.value = stored.id
            solutionSudoku = solutionStr
            showedSudoku = stored
//            val localSudoku = generator.generate(Classic9x9, difficulty)
//            val solution = solver.solve(localSudoku)
//
//            val boardStr = localSudoku.toSingleLineString()
//            val solutionStr = solution.toSingleLineString()
//
//            val sudoku = ServerSudoku(
//                data = boardStr,
//                currentBoard = boardStr,
//                difficulty = difficulty.toString(),
//                solution = solutionStr,
//                userId = email,
//                picture = null
//            )
//
//            _currentSudoku.value = solution
//            _originalSudoku.value = localSudoku
//            solutionSudoku = solutionStr
//
//            val id = repository.insertSudoku(sudoku)
//            showedSudoku = sudoku.copy(id = id)
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
        if (solved) {
            val timeDiff = Calendar.getInstance().time.time - initialTime.time
            _timeDiff.value = timeDiff
//            val timeDiff = Duration.between(initialTime, LocalDateTime.now()).seconds
            viewModelScope.launch {
                if(_email.value!=null){
                    repositoryUser.incrementScore(_email.value!!, getPointsForDifficulty(sudokuDiff))
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