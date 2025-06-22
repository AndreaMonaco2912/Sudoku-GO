package com.example.sudokugo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.repositories.VolumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    volumeRepository: VolumeRepository
) : ViewModel() {

    private val _volume = MutableStateFlow<Float?>(null)
    val volume: StateFlow<Float?> = _volume

    init {
        viewModelScope.launch {
            volumeRepository.volume.collect {
                _volume.value = it
            }
        }
    }
}