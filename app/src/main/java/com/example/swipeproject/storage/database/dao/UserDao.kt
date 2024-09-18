package com.example.swipeproject.storage.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.model.entity.PhotoEntity
import com.example.swipeproject.model.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Transaction
    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getCompleteUserProfile(userId: String):CompleteUserProfile?

    @Transaction
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getUsersPaged(): PagingSource<Int, CompleteUserProfile>

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT uid FROM users ORDER BY name ASC LIMIT :limit")
    suspend fun getFirstNUserIds(limit: Int): List<String>

    @Query("DELETE FROM users WHERE uid IN (:userIds)")
    suspend fun deleteUsersById(userIds: List<String>)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUserByUid(uid: String)

    @Query("DELETE FROM photos WHERE userId IN (:userIds)")
    suspend fun deletePhotosByUserIds(userIds: List<String>)
}
