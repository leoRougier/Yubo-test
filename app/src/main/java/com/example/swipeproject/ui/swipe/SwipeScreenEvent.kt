package com.example.swipeproject.ui.swipe

sealed class SwipeScreenEvent {

    data class OnSwipe(val uid: String?) : SwipeScreenEvent()

}