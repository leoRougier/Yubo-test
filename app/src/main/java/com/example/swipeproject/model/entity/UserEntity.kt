package com.example.swipeproject.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val birth: String,
    val age: Int,
    val gender: String,
    val location: String,
    val town: String,
    val emojis: List<String>
)