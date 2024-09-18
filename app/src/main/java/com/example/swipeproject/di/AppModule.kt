package com.example.swipeproject.di

import com.example.swipeproject.model.UserResponse
import com.example.swipeproject.repository.UserRemoteMediator
import com.example.swipeproject.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  AppModule {


    @Provides
    @Singleton
    fun provideUserRemoteMediator(
        fetchUsers: suspend () -> List<UserResponse>? // Inject the fetchUsers lambda directly
    ): UserRemoteMediator {
        return UserRemoteMediator(fetchUsers)
    }

    @Provides
    @Singleton
    fun provideFetchUsersLambda(userRepository: UserRepository): suspend () -> List<UserResponse>? {
        return { userRepository.fetchUsers() }
    }

}