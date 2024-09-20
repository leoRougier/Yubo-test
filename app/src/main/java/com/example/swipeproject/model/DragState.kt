package com.example.swipeproject.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DragState {
    var isDragging by mutableStateOf(false)
        private set
    var direction by mutableStateOf<Int?>(null)
        private set
    var progress by mutableFloatStateOf(0f)
        private set

    fun update(dragging: Boolean, dir: Int?, prog: Float) {
        isDragging = dragging
        direction = dir
        progress = prog
    }
}