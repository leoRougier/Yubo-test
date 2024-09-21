package com.example.swipeproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swipeproject.ui.swipe.DragDropScreen
import com.example.swipeproject.ui.swipe.SwipeScreenEvent
import com.example.swipeproject.ui.swipe.SwipeViewModel
import com.example.swipeproject.ui.theme.SwipeProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SwipeProjectTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    val viewModel = hiltViewModel<SwipeViewModel>()
                    val state = viewModel.userStack.collectAsState()

                    DragDropScreen(state ) { event ->
                        when(event){
                            is SwipeScreenEvent.DisLike -> viewModel.disLike(event.uid)
                            is SwipeScreenEvent.Like -> viewModel.like(event.uid)
                        }
                    }
                }
            }
        }
    }
}