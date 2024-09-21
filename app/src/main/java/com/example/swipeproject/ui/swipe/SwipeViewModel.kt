package com.example.swipeproject.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SwipeViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 10
        private const val PREFETCH_THRESHOLD = 5
    }

    private var lastFetchedId: Int = 0
    private var isFetching = false

    private val _userStack = MutableStateFlow(SwipeUserScreenState())
    val userStack: StateFlow<SwipeUserScreenState> = _userStack

    init {
        loadMoreUsers()
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.refreshUser()
        }
    }

    private fun loadMoreUsers() {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newUsers = userRepository.getUserProfilesFrom(lastFetchedId, PAGE_SIZE)
                if (newUsers.isNotEmpty()) {
                    lastFetchedId = extractLastId(newUsers)
                    withContext(Dispatchers.Main) {
                        _userStack.update { currentState ->
                            currentState.copy(userProfiles = currentState.userProfiles + newUsers)
                        }
                    }
                } else {
                    _userStack.update { currentState ->
                        currentState.copy(isLoading = true)
                    }
                    userRepository.fetchUsers()
                    val fetchedUsers = userRepository.getUserProfilesFrom(lastFetchedId, PAGE_SIZE)
                    if (fetchedUsers.isNotEmpty()) {
                        lastFetchedId = extractLastId(fetchedUsers)
                        withContext(Dispatchers.Main) {
                            _userStack.update { currentState ->
                                currentState.copy(userProfiles = fetchedUsers, isLoading = false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetching = false
            }
        }
    }

    private fun extractLastId(users: List<UserProfile>): Int {
        return users.last().localId
    }


    private fun removeUser(uid: String?) {
        if (uid == null) return
        if (_userStack.value.userProfiles.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.removeUser(uid)
                _userStack.update { currentState ->
                    currentState.copy(userProfiles = currentState.userProfiles.filterNot { it.uid == uid })
                }
                if (userStack.value.userProfiles.size == PREFETCH_THRESHOLD) {
                    loadMoreUsers()
                }
            }
        }
    }

    fun like(uid: String?) {
        if (uid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            removeUser(uid)
            userRepository.likeUser(uid)
        }
    }

    fun disLike(uid: String?) {
        if (uid == null) return
        viewModelScope.launch(Dispatchers.IO) {
            removeUser(uid)
            userRepository.dislikeUser(uid)
        }
    }
}

data class SwipeUserScreenState(
    val isLoading: Boolean = false,
    val userProfiles: List<UserProfile> = emptyList()
)