package com.example.swipeproject.model

import kotlinx.serialization.Serializable

@Serializable
data class UserActionRequest(
    val uid: String
)
