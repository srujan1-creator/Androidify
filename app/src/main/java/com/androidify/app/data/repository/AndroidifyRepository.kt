package com.androidify.app.data.repository

import com.androidify.app.data.api.AndroidifyApi
import com.androidify.app.data.model.BotGenerationRequest
import com.androidify.app.data.model.BotGenerationResponse
import com.androidify.app.data.model.ImageValidationRequest
import com.androidify.app.data.model.ImageValidationResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that mediates between the UI layer and the Androidify backend API.
 * Wraps API calls in Kotlin Result for clean error handling.
 */
@Singleton
class AndroidifyRepository @Inject constructor(
    private val api: AndroidifyApi
) {

    /**
     * Validates an image to ensure it contains a valid human face.
     * @param imageBase64 The base64-encoded image to validate
     * @return Result wrapping the validation response or an error
     */
    suspend fun validateImage(imageBase64: String): Result<ImageValidationResponse> {
        return try {
            val response = api.validateImage(ImageValidationRequest(imageBase64))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates an Android bot from a selfie image with the given style vibe.
     * Note: This call may take up to 60+ seconds due to AI model processing.
     *
     * @param imageBase64 The base64-encoded selfie image
     * @param vibe The style vibe to apply (e.g., "sporty", "creative", "professional")
     * @param customPrompt Optional custom prompt to guide generation
     * @return Result wrapping the generation response (with generated bot image) or an error
     */
    suspend fun generateBot(
        imageBase64: String,
        vibe: String,
        customPrompt: String? = null
    ): Result<BotGenerationResponse> {
        return try {
            val response = api.generateBot(
                BotGenerationRequest(
                    imageBase64 = imageBase64,
                    vibe = vibe,
                    customPrompt = customPrompt
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
