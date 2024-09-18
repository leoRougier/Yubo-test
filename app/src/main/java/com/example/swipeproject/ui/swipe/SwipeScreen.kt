// SwipeScreen.kt
package com.example.swipeproject.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.swipeproject.model.entity.CompleteUserProfile
import com.example.swipeproject.ui.swipe.components.SwipeDirection
import com.example.swipeproject.ui.swipe.components.SwipeStack

// SwipeScreen.kt
@Composable
fun SwipeScreen(
    usersLazyPagingItems: LazyPagingItems<CompleteUserProfile>, onEvent: (SwipeScreenEvent) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            usersLazyPagingItems.itemSnapshotList.items.isNotEmpty() -> {
                SwipeStack(
                    userProfiles = usersLazyPagingItems.itemSnapshotList.items,
                    onSwiped = { uid, direction ->
                        // Remove the swiped profile from the list
                        //profiles.removeAll { it.user?.uid == uid }
                        // Perform the swipe action
                        onEvent(SwipeScreenEvent.OnSwipe(uid))
                        when (direction) {
                            SwipeDirection.LEFT -> {
                                //viewModel.dislikeUser(uid)
                            }

                            SwipeDirection.RIGHT -> {
                                //viewModel.likeUser(uid)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            usersLazyPagingItems.loadState.refresh is LoadState.Loading ||
                usersLazyPagingItems.loadState.append is LoadState.Loading -> {
                CircularProgressIndicator()
            }

            usersLazyPagingItems.loadState.refresh is LoadState.Error -> {
                val e = usersLazyPagingItems.loadState.refresh as LoadState.Error
                Text(text = "Error: ${e.error.localizedMessage}")
            }

            else -> {
                Text(text = "No more users")
            }
        }
    }
}

