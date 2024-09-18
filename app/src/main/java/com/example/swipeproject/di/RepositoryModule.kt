package com.example.swipeproject.di

import com.example.swipeproject.repository.UserRepository
import com.example.swipeproject.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun provideUserRepository(repository: UserRepositoryImpl): UserRepository

}