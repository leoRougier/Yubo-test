// UserEntity.kt
package com.example.swipeproject.model.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.swipeproject.model.UserResponse

@Entity(
    tableName = "users",
    indices = [Index(value = ["uid"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String,
    val name: String,
    val birth: String,
    val age: Int,
    val gender: String,
    val location: String,
    val town: String,
    val emojis: List<String>,
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