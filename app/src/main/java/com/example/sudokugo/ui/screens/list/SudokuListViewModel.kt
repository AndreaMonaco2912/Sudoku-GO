package com.example.sudokugo.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.database.ServerSudoku
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.UserDSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class SudokuListViewModel(
    private val repository: SudokuRepository,
    private val repositoryUser: UserDSRepository
) : ViewModel() {

    private val _sudokuList = MutableStateFlow<List<ServerSudoku>>(ArrayList())
    val sudokuList: StateFlow<List<ServerSudoku>> = _sudokuList

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    init {
        viewModelScope.launch {
            repositoryUser.email
                .filterNotNull()
                .collect { savedEmail ->
                    _email.value = savedEmail
                    _sudokuList.value = repository.fetchAllSudokuByUser(savedEmail)
                }
        }
    }
}

