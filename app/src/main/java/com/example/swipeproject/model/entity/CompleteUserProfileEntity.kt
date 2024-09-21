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
        localId = user.id,
        uid = user.uid,
        name = user.name,
        age = user.age,
        location = user.location,
        town = user.town,
        emojis = user.emojis,
        profilePhoto = photos.map { it.url },
    )
}