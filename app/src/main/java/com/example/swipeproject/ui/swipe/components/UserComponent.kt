package com.example.swipeproject.ui.swipe.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.swipeproject.R
import com.example.swipeproject.model.DragState
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.ui.theme.lilitaoneFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
                modifier = Modifier
                    .fillMaxSize(),
                model = userProfile.profilePhoto[page],
                contentDescription = "Profile Photo ${page + 1}",
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_error),
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
        fontFamily = lilitaoneFontFamily
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
            UserProfileRow(userProfile, textStyle)
        }
    }
}

@Composable
fun UserProfileRow(userProfile: UserProfile, textStyle: androidx.compose.ui.text.TextStyle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_home),
            contentDescription = "Home Icon",
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = userProfile.location,
            style = textStyle,
            modifier = Modifier.weight(1f)
        )
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

@Composable
fun AnimatedButton(dragState: DragState) {
    if (dragState.isDragging && dragState.direction != null) {
        val imageRes = if (dragState.direction == 1) {
            R.drawable.ic_like
        } else {
            R.drawable.ic_dislike
        }

        val animatedButtonSize by animateDpAsState(
            targetValue = 48.dp + (24.dp * dragState.progress).coerceAtMost(24.dp),
            animationSpec = tween(durationMillis = 100), label = "animate button size"
        )

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = if (dragState.direction == 1) "Like" else "Dislike",
            modifier = Modifier
                .size(animatedButtonSize)
                .clip(RoundedCornerShape(24.dp))
        )
    }
}