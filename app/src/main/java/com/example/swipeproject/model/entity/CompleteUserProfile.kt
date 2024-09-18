package com.example.swipeproject.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CompleteUserProfile(
    @Embedded val user: UserEntity? = null,
    @Relation(
        parentColumn = "uid",
        entityColumn = "userId"
    )
    val photos: List<PhotoEntity>? = null,
)
