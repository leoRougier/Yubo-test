package com.example.swipeproject.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val uid: String,
    val birth: String,
    val age: Int,
    val emojis: List<String>,
    val gender: String,
    val location: String,
    val name: String,
    val town: String,
    val photos: List<Photo>
)