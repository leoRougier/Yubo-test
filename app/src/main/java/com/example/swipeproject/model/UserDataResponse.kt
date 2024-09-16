package com.example.swipeproject.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDataResponse(
    val data: List<UserResponse>
)