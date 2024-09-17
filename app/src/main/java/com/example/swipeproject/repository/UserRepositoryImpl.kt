package com.example.swipeproject.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.swipeproject.model.ResultStatus
import com.example.swipeproject.model.UserActionRequest
import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.model.entity.toPhotoEntities
import com.example.swipeproject.model.entity.toUserEntity
import com.example.swipeproject.service.SwipeApiService
import com.example.swipeproject.storage.database.dao.UserDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: SwipeApiService,
    private val userDao: UserDao,
    private val userRemoteMediator: UserRemoteMediator
) : UserRepositoryContract {

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

    override suspend fun fetchUsers(): List<UserResponse>? {
        val response = apiService.getUsers()
        return if (response.isSuccessful) {
            response.body()?.data?.onEach { user ->
                saveUserToDatabase(user)
            }
        } else {
            null
        }
    }

    private suspend fun saveUserToDatabase(user: UserResponse) {
        val userEntity = user.toUserEntity()
        val photoEntities = user.toPhotoEntities()

        userDao.insertUser(userEntity)
        photoEntities.forEach { photo ->
            userDao.insertPhoto(photo)
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedUsers(): Flow<PagingData<CompleteUserProfile>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2
            ),
            remoteMediator = userRemoteMediator,  // Handle API fetching here
            pagingSourceFactory = { userDao.getUsersPaged() }
        ).flow
    }
}
