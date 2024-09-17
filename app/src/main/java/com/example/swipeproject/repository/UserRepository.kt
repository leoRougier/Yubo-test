package com.example.swipeproject.repository

import androidx.paging.PagingData
import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.model.entity.CompleteUserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepositoryContract {
    suspend fun likeUser(uid: String): ResultStatus
    suspend fun dislikeUser(uid: String): ResultStatus
    suspend fun fetchUsers(): List<UserResponse>?
    fun getPagedUsers(): Flow<PagingData<CompleteUserProfile>>
}