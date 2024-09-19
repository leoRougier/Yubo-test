package com.example.swipeproject.ui.swipe.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.model.entity.CompleteUserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DragDropStack(
    userProfiles: List<UserProfile>,
    onDropLeft: (String?) -> Unit,
    onDropRight: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.i("DragDropStack", userProfiles.size.toString())
    Log.i("DragDropStack", userProfiles.toString())

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        userProfiles.reversed().forEach {  userProfile ->
           // Log.i("DragDropCard", userProfile.toString())
            DragDropCard(
                userProfile = userProfile,
                onDropLeft = { uid -> onDropLeft(uid) },
                onDropRight = { uid -> onDropRight(uid) },
            )
        }
    }
}


@Composable
fun DragDropCard(
    userProfile: UserProfile,
    onDropLeft: (String?) -> Unit,
    onDropRight: (String?) -> Unit,
) {
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val rotationAngle = remember { Animatable(0f) }
    val maxTiltAngle = 15f

    val thresholdPercentage = 0.40f
    var threshold by remember { mutableFloatStateOf(0f) }

    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Update threshold when the card size changes
    val onCardSizeChanged: (IntSize) -> Unit = { size ->
        cardSize = size
        threshold = size.width * thresholdPercentage
    }

    // Update rotation angle based on horizontal offset
    suspend fun updateRotationAngle() {
        val angle = ((offset.value.x / (cardSize.width / 2).coerceAtLeast(1)) * maxTiltAngle)
            .coerceIn(-maxTiltAngle, maxTiltAngle)
        rotationAngle.snapTo(angle)
    }

    suspend fun handleDragEnd(scope: CoroutineScope) {
        val horizontalDistance = abs(offset.value.x)
        if (horizontalDistance > threshold) {
            // Calculate target offset to animate card off-screen
            val maxDimension = max(cardSize.width.toFloat(), cardSize.height.toFloat())
            val normalizedOffset = offset.value / offset.value.getDistance()
            val targetOffset = normalizedOffset * (maxDimension * 2)

            // Animate the card off-screen
            scope.launch {
                offset.animateTo(
                    targetValue = offset.value + targetOffset,
                    animationSpec = tween(durationMillis = 300)
                )
            }
            // Continue rotation as card moves off-screen
            scope.launch {
                val targetRotation = (rotationAngle.value / maxTiltAngle) * 45f // Rotate up to 45 degrees
                rotationAngle.animateTo(
                    targetValue = targetRotation,
                    animationSpec = tween(durationMillis = 300)
                )
            }

            // Trigger callback based on drag direction
            if (offset.value.x > 0) {
                onDropRight(userProfile.uid)
            } else {
                onDropLeft(userProfile.uid)
            }
        } else {
            // Animate back to original position with a spring animation
            scope.launch {
                offset.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            // Animate rotation angle back to zero
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
    }


    // Handle drag gesture
    suspend fun handleDrag(scope: CoroutineScope, change: PointerInputChange, dragAmount: Offset) {
        offset.snapTo(offset.value + dragAmount)
        updateRotationAngle()
        change.consume()
    }

    suspend fun handleDragCancel(scope: CoroutineScope) {
        // Animate back to original position with a spring animation
        scope.launch {
            offset.animateTo(
                targetValue = Offset.Zero,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        // Animate rotation angle back to zero
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


    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged(onCardSizeChanged)
            .offset {
                IntOffset(
                    offset.value.x.roundToInt(),
                    offset.value.y.roundToInt()
                )
            }
            .graphicsLayer {
                rotationZ = rotationAngle.value
            }
            .pointerInput(Unit) {
                // Use coroutineScope for gesture-related coroutines
                coroutineScope {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            Log.d("DragDropCard", "Drag started for user: ${userProfile.uid}")
                            launch { handleDrag(this, change, dragAmount) }
                        },
                        onDragEnd = {
                            Log.d("DragDropCard", "Drag ended for user: ${userProfile.uid}")
                            launch { handleDragEnd(this) }
                        },
                        onDragCancel = {
                            Log.d("DragDropCard", "Drag cancelled for user: ${userProfile.uid}")
                            launch { handleDragCancel(this) }
                        }
                    )
                }
            }
    ) {
        // Card content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
            )
        }
    }
}
