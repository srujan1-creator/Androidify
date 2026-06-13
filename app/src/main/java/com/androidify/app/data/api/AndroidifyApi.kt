package com.androidify.app.data.api

import com.androidify.app.data.model.BotGenerationRequest
import com.androidify.app.data.model.BotGenerationResponse
import com.androidify.app.data.model.ImageCaptionRequest
import com.androidify.app.data.model.ImageCaptionResponse
import com.androidify.app.data.model.ImageValidationRequest
import com.androidify.app.data.model.ImageValidationResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for the Androidify backend.
 * All endpoints accept JSON bodies with base64-encoded images.
 */
interface AndroidifyApi {

    /**
     * Validates whether the provided image contains a valid human face.
     */
    @POST("api/validate-image")
    suspend fun validateImage(@Body request: ImageValidationRequest): ImageValidationResponse

    /**
     * Generates an Android bot image from a selfie with the specified vibe.
     * This can take up to 60 seconds due to AI processing.
     */
    @POST("api/generate-bot")
    suspend fun generateBot(@Body request: BotGenerationRequest): BotGenerationResponse

    /**
     * Generates a caption/description for the provided image.
     */
    @POST("api/caption-image")
    suspend fun captionImage(@Body request: ImageCaptionRequest): ImageCaptionResponse
}
