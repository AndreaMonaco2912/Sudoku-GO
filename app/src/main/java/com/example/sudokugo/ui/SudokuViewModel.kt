package com.example.sudokugo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.Sudoku
import com.example.sudokugo.data.models.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SudokuState(val sudokus: List<Sudoku>)

class SudokuViewModel(
    private val repository: SudokuRepository
) : ViewModel(){
    val state = repository.sudokus.map { SudokuState(sudokus = it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SudokuState(emptyList())
    )

    private val _sudokuFromServer  = MutableStateFlow<ServerSudoku?>(null)
    val sudokuFromServer: StateFlow<ServerSudoku?> = _sudokuFromServer

    private val _sudokusFromServer  = MutableStateFlow<List<ServerSudoku>>(emptyList())
    val sudokusFromServer: StateFlow<List<ServerSudoku>> = _sudokusFromServer

    fun loadSudoku(id: Int){
        viewModelScope.launch {
            _sudokuFromServer.value = repository.fetchSudokuById(id)
        }
    }

    fun allSudokus(){
        viewModelScope.launch {
            _sudokusFromServer.value = repository.fetchAllSudoku()
        }
    }

    fun addSudoku(sudoku: Sudoku) = viewModelScope.launch {
        repository.upsertSudoku(sudoku)
    }

    fun deleteSudoku(sudoku: Sudoku) = viewModelScope.launch {
        repository.deleteSudoku(sudoku)
    }
}