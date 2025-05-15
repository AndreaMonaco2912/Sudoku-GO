package com.example.sudokugo.ui.composables.profilePic

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.sudokugo.data.repositories.UserPictureRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserPictureViewModel(
    private val repository: UserPictureRepository
) : ViewModel() {

    val userPic = repository.userPic.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    fun setUserPic(userPic: Uri) = viewModelScope.launch {
        repository.setUserPic(userPic.toString())
    }

    fun clearUserPic() = viewModelScope.launch {
        repository.setUserPic("")
    }
}
