package com.example.swipeproject.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.model.entity.toPhotoEntities
import com.example.swipeproject.model.entity.toUserEntity
import com.example.swipeproject.service.SwipeApiService
import com.example.swipeproject.storage.database.dao.UserDao

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val apiService: SwipeApiService,
    private val userDao: UserDao
) : RemoteMediator<Int, CompleteUserProfile>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CompleteUserProfile>
    ): MediatorResult {
        return try {
            val response = apiService.getUsers()
            val users = response.body()?.data ?: emptyList()

            if (loadType == LoadType.REFRESH) {
                val seenUserIds = userDao.getFirstNUserIds(10)
                userDao.deleteUsersById(seenUserIds)
                userDao.deletePhotosByUserIds(seenUserIds)
            }

            users.forEach { user ->
                val userEntity = user.toUserEntity()
                val photoEntities = user.toPhotoEntities()

                userDao.insertUser(userEntity)
                photoEntities.forEach { photo ->
                    userDao.insertPhoto(photo)
                }
            }

            MediatorResult.Success(endOfPaginationReached = users.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

/*    private suspend fun getKeyPageData(
        state: PagingState<Int, CompleteUserProfile>,
        loadType: LoadType
    ): Int {
        return when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true).toInt()  // No prepend here
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null) {
                    return 0 // Append failed because the list is empty
                } else {
                    return state.pages.size
                }
            }
        }
    }*/
}
