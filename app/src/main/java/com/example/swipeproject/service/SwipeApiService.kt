package com.example.swipeproject.service

import com.example.swipeproject.model.UserActionRequest
import com.example.swipeproject.model.UserActionResponse
import com.example.swipeproject.model.UserDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET

interface SwipeApiService {
    @GET("list")
    suspend fun getUsers(): Response<UserDataResponse>

    @GET("like")
    suspend fun likeUser(@Body request: UserActionRequest): Response<UserActionResponse>

    @GET("dislike")
    suspend fun dislikeUser(@Body request: UserActionRequest): Response<UserActionResponse>
}