package com.example.swipeproject.storage.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.swipeproject.model.entity.CompleteUserProfileEntity
import com.example.swipeproject.model.entity.PhotoEntity
import com.example.swipeproject.model.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Transaction
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getUsersPaged(): PagingSource<Int, CompleteUserProfileEntity>

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUserByUid(uid: String)

    @Query("DELETE FROM photos WHERE userId = :userIds")
    suspend fun deletePhotosByUserId(userIds: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>
}
