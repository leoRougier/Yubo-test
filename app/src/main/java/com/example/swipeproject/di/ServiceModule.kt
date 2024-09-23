package com.example.swipeproject.di

import com.example.swipeproject.service.SwipeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun providesSwipeApiService(retrofit: Retrofit): SwipeApiService =
        retrofit.create(SwipeApiService::class.java)
}