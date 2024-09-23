package com.example.swipeproject.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.swipeproject.ui.swipe.components.DragDropStack

@Composable
fun SwipeUserScreen(
    state: State<SwipeUserScreenState>,
    onEvent: (SwipeScreenEvent) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.value.isLoading -> {
                CircularProgressIndicator()
            }

            state.value.userProfiles.isNotEmpty() -> {
                DragDropStack(
                    userProfiles = state.value.userProfiles,
                    onDropLeft = { uid ->
                        onEvent(SwipeScreenEvent.DisLike(uid))
                    },
                    onDropRight = { uid ->
                        onEvent(SwipeScreenEvent.Like(uid))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {

                Text(text = "No more users")
            }
        }
    }
}
