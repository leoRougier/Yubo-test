package com.example.swipeproject.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["uid"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [androidx.room.Index(value = ["userId"])]  // Adding index on foreign key column
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val photoId: Int = 0,
    val userId: String,  // Foreign key linking to UserEntity
    val type: String,
    val url: String
)
