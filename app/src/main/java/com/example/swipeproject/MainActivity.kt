package com.example.swipeproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.swipeproject.ui.swipe.SwipeScreen
import com.example.swipeproject.ui.swipe.SwipeScreenEvent
import com.example.swipeproject.ui.swipe.SwipeViewModel
import com.example.swipeproject.ui.theme.SwipeProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwipeProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel = hiltViewModel<SwipeViewModel>()
                    val usersLazyPagingItems = viewModel.usersStateFlow.collectAsLazyPagingItems()

                    SwipeScreen(usersLazyPagingItems){ event ->
                        when(event){
                            is SwipeScreenEvent.OnSwipe -> viewModel.removeUser(event.uid)
                        }

                    }
                }
            }
        }
    }
}