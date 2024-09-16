package com.example.swipeproject.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserActionResponse(
    @SerialName("result") val result: ResultStatus
)

@Serializable
enum class ResultStatus {
    @SerialName("ok")
    SUCCESS,

    @SerialName("ko")
    ERROR
}