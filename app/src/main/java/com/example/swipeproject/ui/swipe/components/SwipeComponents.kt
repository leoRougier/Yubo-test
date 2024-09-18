package com.example.swipeproject.ui.swipe.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.swipeproject.model.entity.CompleteUserProfile
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeStack(
    userProfiles: List<CompleteUserProfile>,
    onSwiped: (String?, SwipeDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.i("SwipeStack", userProfiles.toString())
    val profiles = userProfiles//.take(2) // Show only the top 2 cards

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        profiles.forEach { userProfile ->
            key(userProfile.user?.uid) { // Use key for efficient recomposition
                SwipeCard(
                    onSwipeLeft = {
                        Log.d("SwipeCard", "Swiped Left")
                        onSwiped(userProfile.user?.uid, SwipeDirection.LEFT)
                    },
                    onSwipeRight = {
                        Log.d("SwipeCard", "Swiped Left")
                        onSwiped(userProfile.user?.uid, SwipeDirection.RIGHT)
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.DarkGray),

                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center), // Center the text
                            text = userProfile.user?.name ?: "empty",
                            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
                        )
                    }
                }
            }
        }
    }
}

enum class SwipeDirection {
    LEFT,
    RIGHT
}

// SwipeCard.kt
@Composable
fun SwipeCard(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    swipeThreshold: Float = 200f,
    sensitivityFactor: Float = 1f,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    val animatedOffset by animateFloatAsState(
        targetValue = offset,
        animationSpec = tween(durationMillis = 300),
        label = "AnimatedOffset"
    )
    val rotationZ by remember { derivedStateOf { (animatedOffset / 50f).coerceIn(-30f, 30f) } }
    val alpha by remember { derivedStateOf { 1f - (abs(animatedOffset) / screenWidthPx).coerceIn(0f, 1f) } }

    Box(
        modifier = Modifier
            .offset { IntOffset(animatedOffset.roundToInt(), 0) }
            .graphicsLayer(
                rotationZ = rotationZ,
                alpha = alpha
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        Log.d("SwipeCard", "onDragEnd called")
                        if (offset > swipeThreshold) {
                            Log.d("SwipeCard", "Swiped Right Detected")
                            coroutineScope.launch {
                                offset = screenWidthPx
                                onSwipeRight()
                            }
                        } else if (offset < -swipeThreshold) {
                            Log.d("SwipeCard", "Swiped Left Detected")
                            coroutineScope.launch {
                                offset = -screenWidthPx
                                onSwipeLeft()
                            }
                        } else {
                            // Animate back to center
                            offset = 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        Log.d("SwipeCard", "onHorizontalDrag: dragAmount = $dragAmount")
                        offset += dragAmount * sensitivityFactor
                        change.consumePositionChange()
                    }
                )
            }

    ) {
        content()
    }
}

