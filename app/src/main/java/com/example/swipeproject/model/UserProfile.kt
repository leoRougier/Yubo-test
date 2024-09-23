package com.example.swipeproject.model

data class UserProfile(
    val localId: Int,
    val uid: String,
    val name: String,
    val age: Int,
    val location: String,
    val town: String,
    val emojis: List<String>,
    val profilePhoto: List<String>
)