package com.example.swipeproject.model

import kotlinx.serialization.Serializable

@Serializable
data class Photo(
    val type: String,
    val url: String
)