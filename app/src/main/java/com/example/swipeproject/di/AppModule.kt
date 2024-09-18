package com.example.swipeproject.di

import com.example.swipeproject.repository.UserRemoteMediator
import com.example.swipeproject.repository.UserRepository
import com.example.swipeproject.service.SwipeApiService
import com.example.swipeproject.storage.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  AppModule {

}