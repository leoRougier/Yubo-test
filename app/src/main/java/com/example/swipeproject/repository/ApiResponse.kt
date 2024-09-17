package com.example.swipeproject.repository

sealed class ApiResponse<T>(open val data: T? = null, open val error: Exception? = null) {
    data class LOADING<T>(override val data: T? = null) : ApiResponse<T>(data)
    data class SUCCESS<T>(override val data: T) : ApiResponse<T>(data)
    data class ERROR<T>(override val error: Exception, override val data: T? = null) : ApiResponse<T>(data, error)
}