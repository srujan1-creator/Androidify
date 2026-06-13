package com.androidify.app.data.model

/**
 * Represents the style/vibe options for the generated Android bot.
 */
enum class Vibe(
    val apiName: String,
    val displayName: String,
    val description: String
) {
    CREATIVE("creative", "Creative", "Artsy & Imaginative"),
    SPORTY("sporty", "Sporty", "Active & Energetic"),
    ADVENTUROUS("adventurous", "Adventurous", "Explorer & Bold"),
    PROFESSIONAL("professional", "Professional", "Polished & Confident"),
    CHILL("chill", "Chill", "Relaxed & Cozy")
}
