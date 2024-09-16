package com.example.swipeproject.storage.database.dao

import androidx.room.*
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.model.entity.PhotoEntity
import com.example.swipeproject.model.entity.UserEntity

@Dao
interface UserDao {

    // Insert a user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Insert a photo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    // Get a user along with their photos and emojis
    @Transaction
    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getCompleteUserProfile(userId: String): CompleteUserProfile?

    // Delete a user (cascades to delete associated photos and emojis)
    @Delete
    suspend fun deleteUser(user: UserEntity)
}
