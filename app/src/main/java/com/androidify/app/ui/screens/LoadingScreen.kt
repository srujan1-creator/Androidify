package com.androidify.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidify.app.ui.components.AnimatedBotLogo
import com.androidify.app.viewmodel.GenerationUiState
import com.androidify.app.viewmodel.GenerationViewModel

/**
 * Loading screen displaying generated bot state updates.
 * Automatically navigates to result on success, or displays error on failure.
 */
@Composable
fun LoadingScreen(
    viewModel: GenerationViewModel,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    // Handle state transitions
    LaunchedEffect(uiState) {
        when (uiState) {
            is GenerationUiState.Success -> {
                onSuccess()
            }
            is GenerationUiState.Error -> {
                onFailure((uiState as GenerationUiState.Error).message)
            }
            is GenerationUiState.ValidationFailed -> {
                onFailure((uiState as GenerationUiState.ValidationFailed).message)
            }
            else -> {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated loader element
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
            ) {
                // Background glowing circles
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color(0xFF3DDC84).copy(alpha = 0.1f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF3DDC84).copy(alpha = 0.15f), CircleShape)
                )
                
                // Actual bot logo floating
                AnimatedBotLogo(
                    botColor = Color(0xFF3DDC84),
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Text status updates
            Text(
                text = "Generating...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loadingMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF3DDC84),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = Color(0xFF3DDC84),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
