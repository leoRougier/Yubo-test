package com.example.swipeproject.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.swipeproject.model.UserProfile

data class CompleteUserProfileEntity(
    @Embedded val user: UserEntity? = null,
    @Relation(
        parentColumn = "uid",
        entityColumn = "userId"
    )
    val photos: List<PhotoEntity>? = null,
)

fun CompleteUserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
        uid = user?.uid ?: "",
        name = user?.name ?: "Unknown",
        age = user?.age ?: 0,
        profilePhoto = photos?.firstOrNull()?.url ?: ""
    )
}