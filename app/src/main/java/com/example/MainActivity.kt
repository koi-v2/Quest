package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClassSelectionScreen
import com.example.ui.screens.MainGameScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                // Display toast/snackbar feedback when game events occur
                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                // Handle back button behavior
                BackHandler(enabled = currentScreen != GameScreen.AUTH) {
                    when (currentScreen) {
                        GameScreen.CLASS_SELECTION -> {
                            if (viewModel.activeCharacter.value != null) {
                                // Return to main dashboard if already has a character
                                viewModel.switchCharacter(viewModel.activeCharacter.value!!)
                            } else {
                                viewModel.logout()
                            }
                        }
                        GameScreen.MAIN_DASHBOARD -> {
                            // Don't close app on back, stay on dashboard or prompt
                        }
                        else -> {}
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_transition"
                    ) { target ->
                        when (target) {
                            GameScreen.AUTH -> AuthScreen(viewModel = viewModel)
                            GameScreen.CLASS_SELECTION -> ClassSelectionScreen(viewModel = viewModel)
                            GameScreen.MAIN_DASHBOARD -> MainGameScreen(viewModel = viewModel)
                        }
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
