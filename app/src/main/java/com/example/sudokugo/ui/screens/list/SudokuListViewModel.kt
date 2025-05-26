package com.example.sudokugo.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import io.github.ilikeyourhat.kudoku.model.Sudoku
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SudokuListViewModel (
    private val repository: SudokuRepository
) : ViewModel() {

    private val _sudokuList = MutableStateFlow<List<ServerSudoku>>(ArrayList())
    val sudokuList: StateFlow<List<ServerSudoku>> = _sudokuList


    fun getAllSudokus(){
        viewModelScope.launch {
            _sudokuList.value = repository.fetchAllSudoku()
        }
    }

}

