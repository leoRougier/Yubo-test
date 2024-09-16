package com.example.swipeproject.storage.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromEmojiList(emojis: List<String>): String {
        return Json.encodeToString(emojis)
    }

    @TypeConverter
    fun toEmojiList(emojisString: String): List<String> {
        return Json.decodeFromString(emojisString)
    }
}