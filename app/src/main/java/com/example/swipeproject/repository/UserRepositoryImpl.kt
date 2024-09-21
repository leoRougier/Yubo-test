package com.example.swipeproject.repository

import android.util.Log
import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserActionRequest
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.model.entity.toPhotoEntities
import com.example.swipeproject.model.entity.toUserEntity
import com.example.swipeproject.model.entity.toUserProfile
import com.example.swipeproject.service.SwipeApiService
import com.example.swipeproject.storage.database.dao.UserDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: SwipeApiService,
    private val userDao: UserDao
) : UserRepository {

    companion object {
        private const val PAGE_COUNT = 3
        private const val REFETCH_THRESHOLD = 19
    }


    override suspend fun getUserProfilesFrom(lastFetchedId: Int, pageSize: Int): List<UserProfile> =
        userDao.getUsersFrom(lastFetchedId, pageSize).map { it.toUserProfile() }

    override suspend fun likeUser(uid: String): ResultStatus {
        val request = UserActionRequest(uid)
        val response = apiService.likeUser(request)
        return if (response.isSuccessful) {
            response.body()?.result ?: ResultStatus.ERROR
        } else {
            ResultStatus.ERROR
        }
    }

    override suspend fun dislikeUser(uid: String): ResultStatus {
        val request = UserActionRequest(uid)
        val response = apiService.dislikeUser(request)
        return if (response.isSuccessful) {
            response.body()?.result ?: ResultStatus.ERROR
        } else {
            ResultStatus.ERROR
        }
    }


    override suspend fun fetchUsers() {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            val users = response.body()?.data
            users?.let {
                saveUsersToDatabase(it)
            }

        }
    }


    private suspend fun saveUsersToDatabase(users: List<UserResponse>) {
        val userEntities = users.map { it.toUserEntity() }
        userDao.insertUsers(userEntities)
        val photoEntities = users.flatMap { it.toPhotoEntities() }
        userDao.insertPhotos(photoEntities)
    }


    override suspend fun refreshUser() {
        userDao.observeUserCount()
            .collect { count ->
                if (count == REFETCH_THRESHOLD) {
                    repeat(PAGE_COUNT) {
                        fetchUsers()
                    }
                }
            }
    }

    override suspend fun removeUser(uid: String) {
        try {
            userDao.deleteUserByUid(uid)
            userDao.deletePhotosByUserId(uid)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error removing user: ${e.message}")
        }
    }
}
