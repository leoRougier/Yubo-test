package com.example.swipeproject.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

class DragDropController(
    private val maxTiltAngle: Float = 15f,
    private val thresholdPercentage: Float = 0.40f,
    private val onDropLeft: () -> Unit,
    private val onDropRight: () -> Unit,
    private val onDragStateChanged: (dragging: Boolean, direction: Int?, progress: Float) -> Unit
) {
    // Animatable properties
    val offset = Animatable(Offset.Zero, Offset.VectorConverter)
    val rotationAngle = Animatable(0f)

    // Internal state
    var threshold by mutableStateOf(0f)
        private set
    var cardSize by mutableStateOf(IntSize.Zero)
        private set

    // Drag state
    private var isDragging by mutableStateOf(false)
    private var dragDirection by mutableStateOf<Int?>(null)
    private var dragProgress by mutableStateOf(0f)

    // Coroutine scope for animations
    lateinit var scope: CoroutineScope

    // Update threshold based on card size
    fun onCardSizeChanged(size: IntSize) {
        cardSize = size
        threshold = size.width * thresholdPercentage
    }

    // Update rotation based on offset
    suspend fun updateRotationAngle() {
        val angle = ((offset.value.x / (cardSize.width / 2).coerceAtLeast(1)) * maxTiltAngle)
            .coerceIn(-maxTiltAngle, maxTiltAngle)
        rotationAngle.snapTo(angle)
    }

    // Handle drag gesture
    suspend fun handleDrag(change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Offset) {
        isDragging = true
        offset.snapTo(offset.value + dragAmount)
        updateRotationAngle()

        // Determine drag direction
        dragDirection = when {
            offset.value.x > 0 -> 1
            offset.value.x < 0 -> -1
            else -> null
        }

        // Calculate drag progress
        dragProgress = (abs(offset.value.x) / threshold).coerceIn(0f, 1f)

        // Notify state changes
        onDragStateChanged(isDragging, dragDirection, dragProgress)

        change.consumeAllChanges()
    }

    // Handle drag end
    suspend fun handleDragEnd() {
        isDragging = false
        if (abs(offset.value.x) > threshold) {
            // Animate card off-screen
            val maxDimension = max(cardSize.width.toFloat(), cardSize.height.toFloat())
            val normalizedOffset = offset.value / offset.value.getDistance()
            val targetOffset = normalizedOffset * (maxDimension * 2)

            scope.launch {
                offset.animateTo(
                    targetValue = offset.value + targetOffset,
                    animationSpec = tween(durationMillis = 300)
                )
            }

            // Continue rotation
            scope.launch {
                val targetRotation = (rotationAngle.value / maxTiltAngle) * 45f
                rotationAngle.animateTo(
                    targetValue = targetRotation,
                    animationSpec = tween(durationMillis = 300)
                )
            }

            // Trigger drop callbacks
            if (offset.value.x > 0) {
                onDropRight()
            } else {
                onDropLeft()
            }
        } else {
            // Animate back to original position
            scope.launch {
                offset.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // Animate rotation back to zero
            scope.launch {
                rotationAngle.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        // Reset drag direction and progress
        dragDirection = null
        dragProgress = 0f

        // Notify state changes
        onDragStateChanged(isDragging, dragDirection, dragProgress)
    }

    // Handle drag cancel
    suspend fun handleDragCancel() {
        isDragging = false
        dragDirection = null
        dragProgress = 0f

        // Animate back to original position
        scope.launch {
            offset.animateTo(
                targetValue = Offset.Zero,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Animate rotation back to zero
        scope.launch {
            rotationAngle.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Notify state changes
        onDragStateChanged(isDragging, dragDirection, dragProgress)
    }

    // Extension function to calculate distance of an Offset
    private fun Offset.getDistance(): Float = kotlin.math.sqrt(x * x + y * y)
}
