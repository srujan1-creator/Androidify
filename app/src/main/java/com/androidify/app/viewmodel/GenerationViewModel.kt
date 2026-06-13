package com.androidify.app.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidify.app.data.model.Vibe
import com.androidify.app.data.repository.AndroidifyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GenerationUiState {
    object Idle : GenerationUiState()
    object Validating : GenerationUiState()
    object Generating : GenerationUiState()
    data class Success(val botBitmap: Bitmap, val styleUsed: String) : GenerationUiState()
    data class Error(val message: String) : GenerationUiState()
    data class ValidationFailed(val message: String) : GenerationUiState()
}

@HiltViewModel
class GenerationViewModel @Inject constructor(
    private val repository: AndroidifyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _selectedVibe = MutableStateFlow(Vibe.CREATIVE)
    val selectedVibe: StateFlow<Vibe> = _selectedVibe.asStateFlow()

    private val _loadingMessage = MutableStateFlow("Preparing...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun selectVibe(vibe: Vibe) {
        _selectedVibe.value = vibe
    }

    fun generateBot(imageBase64: String) {
        viewModelScope.launch {
            try {
                // Phase 1: Validate
                _uiState.value = GenerationUiState.Validating
                _loadingMessage.value = "Analyzing your selfie..."

                val validationResult = repository.validateImage(imageBase64)
                validationResult.onFailure { error ->
                    _uiState.value = GenerationUiState.Error(
                        error.message ?: "Failed to validate image"
                    )
                    return@launch
                }
                validationResult.onSuccess { response ->
                    if (!response.isValid) {
                        _uiState.value = GenerationUiState.ValidationFailed(response.message)
                        return@launch
                    }
                }

                // Phase 2: Generate
                _uiState.value = GenerationUiState.Generating
                _loadingMessage.value = "Building your Android bot..."

                val generationResult = repository.generateBot(
                    imageBase64 = imageBase64,
                    vibe = _selectedVibe.value.apiName
                )
                generationResult.onFailure { error ->
                    _uiState.value = GenerationUiState.Error(
                        error.message ?: "Failed to generate bot"
                    )
                    return@launch
                }
                generationResult.onSuccess { response ->
                    val bitmap = base64ToBitmap(response.generatedImageBase64)
                    if (bitmap != null) {
                        _uiState.value = GenerationUiState.Success(
                            botBitmap = bitmap,
                            styleUsed = response.styleUsed
                        )
                    } else {
                        _uiState.value = GenerationUiState.Error("Failed to decode generated image")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GenerationUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = GenerationUiState.Idle
        _selectedVibe.value = Vibe.CREATIVE
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
