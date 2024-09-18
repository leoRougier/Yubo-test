package com.example.swipeproject.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserActionRequest
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.model.entity.CompleteUserProfileEntity
import com.example.swipeproject.model.entity.toPhotoEntities
import com.example.swipeproject.model.entity.toUserEntity
import com.example.swipeproject.model.entity.toUserProfile
import com.example.swipeproject.service.SwipeApiService
import com.example.swipeproject.storage.database.dao.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: SwipeApiService,
    private val userDao: UserDao
) : UserRepository {


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

    private val isFetching = AtomicBoolean(false)

    override suspend fun fetchUsers() {
        // If a fetch is already in progress, skip the new request
        if (!isFetching.compareAndSet(false, true)) return
        try {
            val response = apiService.getUsers()
            Log.i("fetchUsers", response.toString())

            if (response.isSuccessful) {
                val users = response.body()?.data
                users?.let {
                    saveUsersToDatabase(it)  // Save the entire list at once
                }

            }
        } finally {
            // Reset the flag after fetching is complete
            isFetching.set(false)
        }
    }


    private suspend fun saveUsersToDatabase(users: List<UserResponse>) {
        val userEntities = users.map { it.toUserEntity() }
        val photoEntities = users.flatMap { it.toPhotoEntities() }

        // Insert the users and their photos in bulk
        userDao.insertUsers(userEntities)
        userDao.insertPhotos(photoEntities)
    }

    override suspend fun refreshUser() {
        userDao.getUserCount()
            .collect { count ->
                if (count < 10) {
                    fetchUsers()
                }
            }
    }

    override fun getPagedUsers(): Flow<PagingData<UserProfile>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { userDao.getUsersPaged() }
        ).flow.map { pagingData ->
            pagingData.map { completeUserProfile ->
                completeUserProfile.toUserProfile()
            }
        }
    }

    override suspend fun removeUser(uid: String?) {
        uid?.let {
            userDao.deleteUserByUid(uid)
            userDao.deletePhotosByUserId(uid)
        }
    }
}
