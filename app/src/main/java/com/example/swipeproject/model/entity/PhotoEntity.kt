package com.example.swipeproject.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.swipeproject.model.UserResponse


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

fun UserResponse.toPhotoEntities(): List<PhotoEntity> {
    return this.photos.map { photo ->
        PhotoEntity(
            userId = this.uid,
            type = photo.type,
            url = photo.url
        )
    }
}
