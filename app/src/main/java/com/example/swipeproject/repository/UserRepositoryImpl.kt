package com.example.swipeproject.repository

import android.util.Log
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

    override suspend fun fetchUsers(): List<UserResponse>? {
        val response = apiService.getUsers()
        Log.i("fetchUsers", response.toString())
        return if (response.isSuccessful) {
            response.body()?.data?.onEach { user ->
                saveUserToDatabase(user)
            }
        } else {
            null
        }
    }

    override suspend fun saveUserToDatabase(user: UserResponse) {
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
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 10
            ),
            remoteMediator = UserRemoteMediator(this),  // Handle API fetching here
            pagingSourceFactory = { userDao.getUsersPaged() }
        ).flow
    }

    override suspend fun removeUser(uid: String?) {
        uid?.let {
            userDao.deleteUserByUid(uid)
        }
    }
}
