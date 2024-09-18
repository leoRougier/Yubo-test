package com.example.swipeproject.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.model.entity.CompleteUserProfile
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator (
    private val userRepository: UserRepository,
) : RemoteMediator<Int, CompleteUserProfile>() {

    private var users : List<UserResponse>? = emptyList()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CompleteUserProfile>
    ): MediatorResult {
        return try {
            users = when (loadType) {
                LoadType.REFRESH -> {
                    // On refresh, start from the first batch
                    userRepository.fetchUsers()
                }

                LoadType.PREPEND -> {
                    // Not needed since we only append
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    userRepository.fetchUsers()
                }
            }

            // Fetch users from the repository
            val endOfPaginationReached = users?.size?.let {
                it < 20
            } ?: true

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
