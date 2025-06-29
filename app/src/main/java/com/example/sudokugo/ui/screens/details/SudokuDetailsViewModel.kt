package com.example.sudokugo.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SudokuDetailsViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _sudoku = MutableStateFlow<ServerSudoku?>(null)
    val sudoku: StateFlow<ServerSudoku?> = _sudoku

    fun loadSudoku(id: Long) {
        viewModelScope.launch {
            val result = repository.fetchSudokuById(id)
            _sudoku.value = result
        }
    }

    fun changeFav() {
        viewModelScope.launch {
            _sudoku.value?.let {
                repository.changeFav(it.id, !it.favourite)
                _sudoku.value = repository.fetchSudokuById(it.id)
            }
        }
    }
}



