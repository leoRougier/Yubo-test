package com.example.swipeproject.ui.swipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    val usersFlow: Flow<PagingData<UserProfile>> = userRepository.getPagedUsers()
        .cachedIn(viewModelScope)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (userRepository.getUserCount() == 0) {
                userRepository.fetchUsers()
            }
        }
        refreshUser()
    }

    fun removeUser(uid: String?) {
        if (uid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.removeUser(uid)
        }
    }

    fun like(uid: String?){
        Log.i("likeUser", "viewmodel $uid")
        if (uid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.likeUser(uid)
            userRepository.removeUser(uid)
        }
    }

    fun disLike(uid: String?){
        if (uid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.dislikeUser(uid)
            userRepository.removeUser(uid)
        }
    }

    private fun refreshUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.refreshUser()
        }
    }
}