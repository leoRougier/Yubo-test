package com.example.swipeproject.ui.swipe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.swipeproject.model.UserProfile
import com.example.swipeproject.ui.swipe.components.DragDropStack

@Composable
fun DragDropScreen(
    usersLazyPagingItems: LazyPagingItems<UserProfile>,
    onEvent: (SwipeScreenEvent) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            usersLazyPagingItems.itemSnapshotList.items.isNotEmpty() -> {
                DragDropStack(
                    userProfiles = usersLazyPagingItems.itemSnapshotList,
                    onDropLeft = { uid ->
                        onEvent(SwipeScreenEvent.DisLike(uid))
                    },
                    onDropRight = { uid ->
                        onEvent(SwipeScreenEvent.Like(uid))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            usersLazyPagingItems.loadState.append is LoadState.Loading -> {
                Log.i("loadState", "append : Loading")
            }

            usersLazyPagingItems.loadState.append is LoadState.Error -> {
                val e = usersLazyPagingItems.loadState.append as LoadState.Error
                Text(text = "Error: ${e.error.localizedMessage}")
            }

            usersLazyPagingItems.loadState.refresh is LoadState.Loading -> {
                CircularProgressIndicator()
            }

            usersLazyPagingItems.loadState.refresh is LoadState.Error -> {
                val e = usersLazyPagingItems.loadState.refresh as LoadState.Error
                Text(text = "Error: ${e.error.localizedMessage}")
            }

            else -> {
                Log.i("loadState", "loadState : ${usersLazyPagingItems.loadState}")
                Text(text = "No more users")
            }
        }
    }
}
