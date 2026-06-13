package com.androidify.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request to validate whether an image contains a valid human face.
 * @param imageBase64 Base64-encoded image string
 */
data class ImageValidationRequest(
    @SerializedName("image_base64") val imageBase64: String
)

/**
 * Response from the image validation endpoint.
 * @param isValid Whether the image contains a valid face
 * @param message Human-readable validation message
 * @param confidence Confidence score (0.0 to 1.0)
 */
data class ImageValidationResponse(
    @SerializedName("is_valid") val isValid: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("confidence") val confidence: Float
)

/**
 * Request to generate an Android bot from a selfie.
 * @param imageBase64 Base64-encoded selfie image
 * @param vibe Style vibe for the bot (e.g., "sporty", "creative")
 * @param customPrompt Optional custom prompt for generation
 */
data class BotGenerationRequest(
    @SerializedName("image_base64") val imageBase64: String,
    @SerializedName("vibe") val vibe: String,
    @SerializedName("custom_prompt") val customPrompt: String? = null
)

/**
 * Response containing the generated Android bot image.
 * @param generatedImageBase64 Base64-encoded generated bot image
 * @param styleUsed The style that was applied
 */
data class BotGenerationResponse(
    @SerializedName("generated_image_base64") val generatedImageBase64: String,
    @SerializedName("style_used") val styleUsed: String
)

/**
 * Request to caption an image (for accessibility/description).
 * @param imageBase64 Base64-encoded image string
 */
data class ImageCaptionRequest(
    @SerializedName("image_base64") val imageBase64: String
)

/**
 * Response with the generated caption for the image.
 * @param caption The generated caption text
 */
data class ImageCaptionResponse(
    @SerializedName("caption") val caption: String
)
