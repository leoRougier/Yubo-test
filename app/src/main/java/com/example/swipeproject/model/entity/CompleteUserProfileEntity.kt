package com.example.swipeproject.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.swipeproject.model.UserProfile

data class CompleteUserProfileEntity(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "userId"
    )
    val photos: List<PhotoEntity>,
)

fun CompleteUserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
        uid = user.uid ,
        name = user.name,
        age = user.age,
        profilePhoto = photos.firstOrNull()?.url ?: ""
    )
}