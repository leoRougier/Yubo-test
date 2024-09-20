package com.example.swipeproject.ui.swipe.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.swipeproject.R
import com.example.swipeproject.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DragDropStack(
    userProfiles: List<UserProfile>,
    onDropLeft: (String?) -> Unit,
    onDropRight: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {

    val isDragging = remember { mutableStateOf(false) }
    val dragDirection = remember { mutableStateOf<Int?>(null) } // -1 for left, 1 for right
    val dragProgress = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        userProfiles.reversed().forEach { userProfile ->
            DragDropCard(
                userProfile = userProfile,
                onDropLeft = { uid -> onDropLeft(uid) },
                onDropRight = { uid -> onDropRight(uid) },
                onDragStateChanged = { dragging, direction, progress ->
                    isDragging.value = dragging
                    dragDirection.value = direction
                    dragProgress.floatValue = progress
                }
            ) {
                UserProfile(userProfile)
            }
        }

        if (isDragging.value && dragDirection.value != null) {
            val imageRes = if (dragDirection.value == 1) {
                R.drawable.ic_like
            } else {
                R.drawable.ic_dislike
            }

            val animatedButtonSize by animateDpAsState(
                targetValue = 48.dp + (24.dp * dragProgress.floatValue).coerceAtMost(24.dp),
                animationSpec = tween(durationMillis = 100)
            )

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = if (dragDirection.value == 1) "Like" else "Dislike",
                modifier = Modifier
                    .size(animatedButtonSize)
                    .clip(RoundedCornerShape(24.dp))
            )
        }
    }
}


@Composable
fun DragDropCard(
    userProfile: UserProfile,
    onDropLeft: (String?) -> Unit,
    onDropRight: (String?) -> Unit,
    onDragStateChanged: (dragging: Boolean, direction: Int?, progress: Float) -> Unit,
    content: @Composable () -> Unit
) {
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val rotationAngle = remember { Animatable(0f) }
    val maxTiltAngle = 15f

    val thresholdPercentage = 0.40f
    var threshold by remember { mutableFloatStateOf(0f) }

    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Internal drag state
    val isDraggingInternal = remember { mutableStateOf(false) }
    val dragDirectionInternal = remember { mutableStateOf<Int?>(null) }
    val dragProgressInternal = remember { mutableStateOf(0f) }

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
        isDraggingInternal.value = false
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

        // Notify DragDropStack about the drag end
        onDragStateChanged(isDraggingInternal.value, dragDirectionInternal.value, dragProgressInternal.value)
    }

    // Handle drag gesture
    suspend fun handleDrag(scope: CoroutineScope, change: PointerInputChange, dragAmount: Offset) {
        isDraggingInternal.value = true
        offset.snapTo(offset.value + dragAmount)
        updateRotationAngle()

        // Update drag direction
        dragDirectionInternal.value = when {
            offset.value.x > 0 -> 1
            offset.value.x < 0 -> -1
            else -> null
        }

        // Update drag progress
        dragProgressInternal.value = (abs(offset.value.x) / threshold).coerceIn(0f, 1f)

        // Notify DragDropStack about the drag progress
        onDragStateChanged(isDraggingInternal.value, dragDirectionInternal.value, dragProgressInternal.value)

        change.consumeAllChanges()
    }

    suspend fun handleDragCancel(scope: CoroutineScope) {
        isDraggingInternal.value = false
        dragDirectionInternal.value = null
        dragProgressInternal.value = 0f
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

        // Notify DragDropStack about the drag cancel
        onDragStateChanged(isDraggingInternal.value, dragDirectionInternal.value, dragProgressInternal.value)
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
                coroutineScope {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            launch { handleDrag(this, change, dragAmount) }
                        },
                        onDragEnd = {
                            launch { handleDragEnd(this) }
                        },
                        onDragCancel = {
                            launch { handleDragCancel(this) }
                        }
                    )
                }
            }
    ) {
        content()
    }
}


val customFontFamily = FontFamily(
    Font(R.font.lilitaone_regular, FontWeight.Normal, FontStyle.Normal)
)

@Composable
fun UserProfile(userProfile: UserProfile) {
    val pagerState = rememberPagerState(pageCount = { userProfile.profilePhoto.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage by remember {
        derivedStateOf { pagerState.currentPage == userProfile.profilePhoto.lastIndex }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) { page ->
            AsyncImage(
                model = userProfile.profilePhoto[page],
                contentDescription = "Profile Photo ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8))
            )
        }

        if (isLastPage) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onPageClick = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )
            Spacer(modifier = Modifier.padding(16.dp))

            Box {
                HorizontalPagerClickZones(pagerState = pagerState, scope = coroutineScope, userProfile.profilePhoto.lastIndex)
                UserProfileOverlay(userProfile, isLastPage)
            }
        }
    }
}

@Composable
fun HorizontalPagerClickZones(pagerState: PagerState, scope: CoroutineScope, lastIndex: Int) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        // Left Click Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    scope.launch {
                        val previousPage = if (pagerState.currentPage > 0) {
                            pagerState.currentPage - 1
                        } else {
                            lastIndex
                        }
                        pagerState.animateScrollToPage(previousPage)
                    }
                }
        )

        // Right Click Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    scope.launch {
                        val nextPage = if (pagerState.currentPage < lastIndex) {
                            pagerState.currentPage + 1
                        } else {
                            0
                        }
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
        )
    }
}


@Composable
fun UserProfileOverlay(userProfile: UserProfile, showFullData: Boolean) {
    val textStyle = MaterialTheme.typography.headlineLarge.copy(
        color = Color.White,
        fontFamily = customFontFamily
    )

    val displayText = buildString {
        append(userProfile.name)
        if (showFullData) {
            if (userProfile.emojis.isNotEmpty()) {
                append(" ")
                append(userProfile.emojis.joinToString(" "))
            }
            append(", ${userProfile.age}")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = displayText,
            style = textStyle,
            modifier = Modifier.fillMaxWidth()
        )

        if (showFullData && userProfile.location.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = userProfile.location,
                style = textStyle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
private fun HorizontalPagerIndicator(
    pagerState: PagerState,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.White,
    unselectedIndicatorSize: Dp = 8.dp,
    selectedIndicatorSize: Dp = 10.dp,
    indicatorCornerRadius: Dp = 2.dp,
    indicatorPadding: Dp = 2.dp,
    minIndicatorClickableSize: Dp = 48.dp
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(selectedIndicatorSize + indicatorPadding * 2)
    ) {
        val maxAvailableWidth = maxWidth
        val totalPadding = indicatorPadding * 2 * pagerState.pageCount
        val indicatorWidth = (maxAvailableWidth - totalPadding) / pagerState.pageCount

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(pagerState.pageCount) { page ->
                // Calculate color and size of the indicator
                val (color, size) = if (pagerState.currentPage == page || pagerState.targetPage == page) {
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val offsetPercentage = 1f - pageOffset.coerceIn(0f, 1f)

                    val calculatedSize =
                        unselectedIndicatorSize + ((selectedIndicatorSize - unselectedIndicatorSize) * offsetPercentage)

                    indicatorColor.copy(alpha = offsetPercentage) to calculatedSize
                } else {
                    indicatorColor.copy(alpha = 0.1f) to unselectedIndicatorSize
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = indicatorPadding)
                        .size(minIndicatorClickableSize)
                        .clickable(onClick = { onPageClick(page) }),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(indicatorCornerRadius))
                            .background(color)
                            .width(indicatorWidth)
                            .height(size / 2)
                    )
                }
            }
        }
    }
}