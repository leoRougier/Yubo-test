package com.example.swipeproject.ui.swipe.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.example.swipeproject.model.DragState
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.utils.DragDropController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


private const val MAX_VISIBLE_PROFILES = 5

@Composable
fun DragDropStack(
    userProfiles: List<UserProfile>,
    onDropLeft: (String?) -> Unit,
    onDropRight: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragState = remember { DragState() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        userProfiles.take(MAX_VISIBLE_PROFILES).reversed().forEachIndexed { index, userProfile ->

            key(userProfile.uid) {
                DragDropCard(
                    isTop = index == userProfiles.takeLast(MAX_VISIBLE_PROFILES).size - 1,
                    onDropLeft = { onDropLeft(userProfile.uid) },
                    onDropRight = { onDropRight(userProfile.uid) },
                    onDragStateChanged = { dragging, direction, progress ->
                        dragState.update(dragging, direction, progress)
                    }
                ) {
                    UserProfile(userProfile)
                }
            }
        }
        AnimatedButton(dragState = dragState)
    }
}

@Composable
fun DragDropCard(
    isTop: Boolean,
    onDropLeft: () -> Unit,
    onDropRight: () -> Unit,
    onDragStateChanged: (dragging: Boolean, direction: Int?, progress: Float) -> Unit,
    content: @Composable () -> Unit
) {
    if (!isTop) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val onDropLeftState by rememberUpdatedState(newValue = onDropLeft)
    val onDropRightState by rememberUpdatedState(newValue = onDropRight)
    val onDragStateChangedState by rememberUpdatedState(newValue = onDragStateChanged)

    val controller = remember {
        DragDropController(
            onDropLeft = { onDropLeftState() },
            onDropRight = { onDropRightState() },
            onDragStateChanged = { dragging, direction, progress ->
                onDragStateChangedState(dragging, direction, progress)
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    controller.scope = coroutineScope

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                controller.onCardSizeChanged(size)
            }
            .offset {
                IntOffset(
                    controller.offset.value.x.roundToInt(),
                    controller.offset.value.y.roundToInt()
                )
            }
            .graphicsLayer {
                rotationZ = controller.rotationAngle.value
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {

                    },
                    onDrag = { change, dragAmount ->
                        coroutineScope.launch {
                            controller.handleDrag(change, dragAmount)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            controller.handleDragEnd()
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            controller.handleDragCancel()
                        }
                    }
                )
            }
    ) {
        content()
    }
}

