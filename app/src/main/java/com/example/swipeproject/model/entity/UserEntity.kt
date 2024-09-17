package com.example.swipeproject.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.swipeproject.model.UserResponse

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

fun UserResponse.toUserEntity(): UserEntity {
    return UserEntity(
        uid = this.uid,
        name = this.name,
        birth = this.birth,
        age = this.age,
        gender = this.gender,
        location = this.location,
        town = this.town,
        emojis = this.emojis
    )
}