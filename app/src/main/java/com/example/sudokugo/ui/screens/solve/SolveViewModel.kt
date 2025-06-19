package com.example.sudokugo.ui.screens.solve

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.UserDSRepository
import io.github.ilikeyourhat.kudoku.generating.defaultGenerator
import io.github.ilikeyourhat.kudoku.model.Sudoku
import io.github.ilikeyourhat.kudoku.parsing.createFromString
import io.github.ilikeyourhat.kudoku.parsing.fromSingleLineString
import io.github.ilikeyourhat.kudoku.parsing.toSingleLineString
import io.github.ilikeyourhat.kudoku.rating.Difficulty
import io.github.ilikeyourhat.kudoku.solving.defaultSolver
import io.github.ilikeyourhat.kudoku.type.Classic9x9
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.time.LocalDate


class SolveViewModel(
    private val repository: SudokuRepository,
    private val repositoryUser: UserDSRepository
) : ViewModel() {
    var startTime: Long = 0L
        private set

    private val generator = Sudoku.defaultGenerator()
    private val solver = Sudoku.defaultSolver()

    private var showedSudoku: ServerSudoku? = null

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
        startTime = System.currentTimeMillis()
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
        startTime = System.currentTimeMillis()
        if (_currentSudoku.value != null) return

        viewModelScope.launch {
            val email = _email.value ?: repositoryUser.email.firstOrNull()
//            if (email == null) {
//                Log.e("SolveViewModel", "Email non disponibile. Impossibile creare sudoku.")
//                return@launch
//            }
            val result = withContext(Dispatchers.Default) {
                val localSudoku = generator.generate(Classic9x9, difficulty)
                val solution = solver.solve(localSudoku)

                val boardStr = localSudoku.toSingleLineString()
                val solutionStr = solution.toSingleLineString()

                val sudoku = ServerSudoku(
                    data = boardStr,
                    currentBoard = boardStr,
                    difficulty = difficulty.toString(),
                    solution = solutionStr,
                    userId = email,
                    picture = null,
                    solveDate = null,
                    time = null
                )

                val id = repository.insertSudoku(sudoku)
                Triple(solution, localSudoku to sudoku.copy(id = id), solutionStr)
            }

            val (solution, pair, solutionStr) = result
            val (original, stored) = pair

            _currentSudoku.value = solution
            _originalSudoku.value = original
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
            val date = LocalDate.now().toString()
            viewModelScope.launch {
                if(_email.value!=null){
                    repositoryUser.incrementScore(_email.value!!, 100)
                }
                repository.solveSudoku(showedSudoku!!.id, date, System.currentTimeMillis() - startTime)
            }
        }
        return solved
    }

    fun restart() {
        _currentSudoku.value = _originalSudoku.value
    }
}