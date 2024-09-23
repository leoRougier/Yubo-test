package com.example.swipeproject.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.joinAll
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
    private var threshold by mutableFloatStateOf(0f)
    private var cardSize by mutableStateOf(IntSize.Zero)

    // Drag state
    private var isDragging by mutableStateOf(false)
    private var dragDirection by mutableStateOf<Int?>(null)
    private var dragProgress by mutableFloatStateOf(0f)

    // Coroutine scope for animations
    lateinit var scope: CoroutineScope

    // Update threshold based on card size
    fun onCardSizeChanged(size: IntSize) {
        cardSize = size
        threshold = size.width * thresholdPercentage
    }

    // Update rotation based on offset
    private suspend fun updateRotationAngle() {
        val angle = ((offset.value.x / (cardSize.width / 2).coerceAtLeast(1)) * maxTiltAngle)
            .coerceIn(-maxTiltAngle, maxTiltAngle)
        rotationAngle.snapTo(angle)
    }

    // Handle drag gesture
    suspend fun handleDrag(change: PointerInputChange, dragAmount: Offset) {
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
        val horizontalDistance = abs(offset.value.x)
        if (horizontalDistance > threshold) {
            // Calculate target offset to animate card off-screen
            val maxDimension = max(cardSize.width.toFloat(), cardSize.height.toFloat())
            val normalizedOffset = offset.value / offset.value.getDistance()
            val targetOffset = normalizedOffset * (maxDimension * 2)

            // Calculate target rotation
            val targetRotation = if (offset.value.x > 0) 45f else -45f

            // Animate both offset and rotation in the same coroutine for synchronization
            scope.launch {
                val offsetAnimation = launch {
                    offset.animateTo(
                        targetValue = offset.value + targetOffset,
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                val rotationAnimation = launch {
                    rotationAngle.animateTo(
                        targetValue = targetRotation,
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                // Wait for both animations to complete
                joinAll(offsetAnimation, rotationAnimation)

                // Trigger drop callbacks AFTER animation completes
                if (offset.value.x > 0) {
                    onDropRight()
                } else {
                    onDropLeft()
                }
            }
        } else {
            // Animate back to original position with a spring animation
            scope.launch {
                val offsetAnimation = launch {
                    offset.animateTo(
                        targetValue = Offset.Zero,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                val rotationAnimation = launch {
                    rotationAngle.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                // Wait for both animations to complete
                joinAll(offsetAnimation, rotationAnimation)
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
            val offsetAnimation = launch {
                offset.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            val rotationAnimation = launch {
                rotationAngle.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            // Wait for both animations to complete
            joinAll(offsetAnimation, rotationAnimation)
        }

        // Notify state changes
        onDragStateChanged(isDragging, dragDirection, dragProgress)
    }
}

