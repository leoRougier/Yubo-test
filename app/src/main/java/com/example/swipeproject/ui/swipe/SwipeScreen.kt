// SwipeScreen.kt
package com.example.swipeproject.ui.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel = hiltViewModel()
) {
    val users = viewModel.usersStateFlow.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when (users.loadState.refresh) {
            is LoadState.Loading -> {
                // Initial loading state
                CircularProgressIndicator()
            }

            is LoadState.Error -> {
                // Error state during initial load
                val e = users.loadState.refresh as LoadState.Error
                Text(text = "Error: ${e.error.localizedMessage}")
            }

            else -> {
                val currentUser = users.itemSnapshotList.items.firstOrNull()
                if (currentUser != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Display User Name
                        Text(
                            text = currentUser.user?.name ?: "empty",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // "Next" Button
                        Button(
                            onClick = { viewModel.removeUser(currentUser.user?.uid) },
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp)
                        ) {
                            Text(text = "Next")
                        }
                    }
                } else {
                    // No more users or empty state
                    if (users.loadState.append is LoadState.Loading) {
                        CircularProgressIndicator()
                    } else if (users.itemSnapshotList.items.isEmpty()) {
                        Text(text = "No more users")
                    }
                }
            }
        }
    }
}
