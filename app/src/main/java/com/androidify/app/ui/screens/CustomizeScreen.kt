package com.androidify.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidify.app.data.model.Vibe
import com.androidify.app.ui.components.VibeCard
import com.androidify.app.viewmodel.CameraViewModel
import com.androidify.app.viewmodel.GenerationViewModel

/**
 * Screen where the user selects their avatar's vibe and optional custom prompts.
 */
@Composable
fun CustomizeScreen(
    cameraViewModel: CameraViewModel,
    generationViewModel: GenerationViewModel,
    onGenerate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capturedImageBase64 by cameraViewModel.capturedImageBase64.collectAsState()
    val selectedVibe by generationViewModel.selectedVibe.collectAsState()
    
    // For custom prompt addition
    var customPrompt by remember { mutableStateOf("") }

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
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("Back", color = Color.White)
                }
                Text(
                    text = "Pick Your Vibe",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(60.dp)) // Spacer to center title
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Options List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(Vibe.values()) { vibe ->
                    VibeCard(
                        vibe = vibe,
                        isSelected = selectedVibe == vibe,
                        onClick = { generationViewModel.selectVibe(vibe) }
                    )
                }

                // Custom prompt field
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Add custom details (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        placeholder = { Text("e.g. holding a guitar, wearing a cap", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3DDC84),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        singleLine = true
                    )
                }
            }

            // Generate Button
            Button(
                onClick = {
                    val base64 = capturedImageBase64
                    if (base64 != null) {
                        generationViewModel.generateBot(base64)
                        onGenerate()
                    }
                },
                enabled = capturedImageBase64 != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3DDC84),
                    contentColor = Color(0xFF020617)
                ),
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Generate Android Bot",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
