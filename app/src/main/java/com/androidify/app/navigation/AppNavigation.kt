package com.androidify.app.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.androidify.app.ui.screens.*
import com.androidify.app.viewmodel.CameraViewModel
import com.androidify.app.viewmodel.GenerationViewModel

/**
 * Navigation graph defining all screen destinations and transitions.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Share ViewModels by instantiating them at the NavHost scope
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val generationViewModel: GenerationViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = {
                    cameraViewModel.clearCapture()
                    navController.navigate("camera")
                }
            )
        }

        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                onImageCaptured = {
                    navController.navigate("preview")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("preview") {
            PreviewScreen(
                viewModel = cameraViewModel,
                onConfirm = {
                    navController.navigate("customize")
                },
                onRetake = {
                    cameraViewModel.clearCapture()
                    navController.navigate("camera") {
                        popUpTo("camera") { inclusive = true }
                    }
                }
            )
        }

        composable("customize") {
            CustomizeScreen(
                cameraViewModel = cameraViewModel,
                generationViewModel = generationViewModel,
                onGenerate = {
                    navController.navigate("loading")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("loading") {
            LoadingScreen(
                viewModel = generationViewModel,
                onSuccess = {
                    navController.navigate("result") {
                        popUpTo("customize") { inclusive = true }
                    }
                },
                onFailure = { error ->
                    Toast.makeText(context, "Generation error: $error", Toast.LENGTH_LONG).show()
                    navController.navigate("customize") {
                        popUpTo("customize") { inclusive = true }
                    }
                }
            )
        }

        composable("result") {
            ResultScreen(
                viewModel = generationViewModel,
                onStartOver = {
                    cameraViewModel.clearCapture()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
    }
}
