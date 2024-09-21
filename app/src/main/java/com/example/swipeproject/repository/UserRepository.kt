package com.example.swipeproject.repository

import androidx.paging.PagingData
import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun likeUser(uid: String): ResultStatus
    suspend fun dislikeUser(uid: String): ResultStatus
    suspend fun fetchUsers()
    fun getPagedUsers(): Flow<PagingData<UserProfile>>
    suspend fun removeUser(uid: String)
    suspend fun refreshUser()
    suspend fun getUserCount(): Int
    suspend fun getUserProfilesFrom(lastFetchedId: Int, pageSize: Int): List<UserProfile>
}