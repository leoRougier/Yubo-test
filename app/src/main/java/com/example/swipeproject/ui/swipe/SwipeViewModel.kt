package com.example.swipeproject.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _usersStateFlow = MutableStateFlow<PagingData<CompleteUserProfile>>(PagingData.empty())
    val usersStateFlow: StateFlow<PagingData<CompleteUserProfile>> = _usersStateFlow

    init {
        fetchPagedUsers()
        refreshUser()
    }

    fun removeUser(uid: String?) {
        viewModelScope.launch {
            userRepository.removeUser(uid)
        }
    }
    private fun refreshUser(){
        viewModelScope.launch {
            userRepository.refreshUser()
        }
    }


    private fun fetchPagedUsers() {
        viewModelScope.launch {
            userRepository.getPagedUsers()
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _usersStateFlow.value = pagingData
                }
        }
    }
}