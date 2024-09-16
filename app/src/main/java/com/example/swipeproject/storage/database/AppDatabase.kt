package com.example.swipeproject.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.swipeproject.model.entity.PhotoEntity
import com.example.swipeproject.model.entity.UserEntity
import com.example.swipeproject.storage.database.converter.Converters
import com.example.swipeproject.storage.database.dao.UserDao

@Database(
    entities = [
        UserEntity::class,
        PhotoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
}
