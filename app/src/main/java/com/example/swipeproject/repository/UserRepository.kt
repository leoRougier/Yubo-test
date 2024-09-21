package com.example.swipeproject.repository

import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserProfile

interface UserRepository {
    suspend fun likeUser(uid: String): ResultStatus
    suspend fun dislikeUser(uid: String): ResultStatus
    suspend fun fetchUsers()
    suspend fun removeUser(uid: String)
    suspend fun refreshUser()
    suspend fun getUserProfilesFrom(lastFetchedId: Int, pageSize: Int): List<UserProfile>
}