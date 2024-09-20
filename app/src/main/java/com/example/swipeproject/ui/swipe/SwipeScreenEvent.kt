package com.example.swipeproject.ui.swipe

sealed class SwipeScreenEvent {

    data class OnSwipe(val uid: String?) : SwipeScreenEvent()
    data class Like(val uid: String?) : SwipeScreenEvent()
    data class DisLike(val uid: String?) : SwipeScreenEvent()
}