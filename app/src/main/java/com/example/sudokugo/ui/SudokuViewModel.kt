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

    private val _sudokusFromServer = MutableStateFlow<List<ServerSudoku>>(emptyList())
    val sudokusFromServer: StateFlow<List<ServerSudoku>> = _sudokusFromServer

    fun allSudokus() {
        viewModelScope.launch {
            repository.fetchAllSudoku().collect { list ->
                _sudokusFromServer.value = list
            }
        }
    }

}