package com.androidify.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A beautiful, animated 3D-like Android bot logo drawn entirely in Compose canvas.
 * Implements a floating "breathing" animation and subtle rotation.
 */
@Composable
fun AnimatedBotLogo(
    modifier: Modifier = Modifier,
    botColor: Color = Color(0xFF3DDC84) // Android Green
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bot_animation")
    
    // Floating offset (breathing effect)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Arm swing animation
    val armSwingAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arm_swing"
    )

    // Antenna wiggle
    val antennaWiggle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antenna_wiggle"
    )

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = (canvasHeight / 2f) + floatAnim

            // Core scale sizes based on canvas width
            val botWidth = canvasWidth * 0.45f
            val bodyHeight = botWidth * 0.85f
            val headHeight = botWidth * 0.45f
            val gap = botWidth * 0.08f
            val limbWidth = botWidth * 0.2f
            val armHeight = botWidth * 0.7f
            val legHeight = botWidth * 0.3f
            
            // Draw Legs
            val legY = centerY + (bodyHeight / 2f) + gap
            // Left Leg
            drawRoundRect(
                color = botColor,
                topLeft = Offset(centerX - (botWidth * 0.3f) - (limbWidth / 2f), legY),
                size = Size(limbWidth, legHeight),
                cornerRadius = CornerRadius(limbWidth / 2f, limbWidth / 2f)
            )
            // Right Leg
            drawRoundRect(
                color = botColor,
                topLeft = Offset(centerX + (botWidth * 0.3f) - (limbWidth / 2f), legY),
                size = Size(limbWidth, legHeight),
                cornerRadius = CornerRadius(limbWidth / 2f, limbWidth / 2f)
            )

            // Draw Body
            val bodyY = centerY - (bodyHeight / 2f)
            drawRoundRect(
                color = botColor,
                topLeft = Offset(centerX - (botWidth / 2f), bodyY),
                size = Size(botWidth, bodyHeight),
                cornerRadius = CornerRadius(botWidth * 0.15f, botWidth * 0.15f)
            )

            // Draw Head (half circle / arc)
            val headY = bodyY - headHeight - gap
            drawArc(
                color = botColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(centerX - (botWidth / 2f), headY),
                size = Size(botWidth, headHeight * 2f)
            )

            // Draw Eyes
            val eyeRadius = botWidth * 0.05f
            val eyeOffsetOffset = botWidth * 0.2f
            val eyeY = headY + (headHeight * 0.65f)
            // Left Eye
            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(centerX - eyeOffsetOffset, eyeY)
            )
            // Right Eye
            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(centerX + eyeOffsetOffset, eyeY)
            )

            // Draw Antennas
            val antennaLength = botWidth * 0.35f
            val antennaThickness = botWidth * 0.04f
            
            // Left Antenna
            val leftAntennaAngle = -120f + antennaWiggle
            val leftRad = leftAntennaAngle * PI.toFloat() / 180f
            val leftAntennaStart = Offset(
                centerX - (botWidth * 0.25f),
                headY + (headHeight * 0.15f)
            )
            val leftAntennaEnd = Offset(
                leftAntennaStart.x + antennaLength * kotlin.math.cos(leftRad),
                leftAntennaStart.y + antennaLength * sin(leftRad)
            )
            drawLine(
                color = botColor,
                start = leftAntennaStart,
                end = leftAntennaEnd,
                strokeWidth = antennaThickness,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Right Antenna
            val rightAntennaAngle = -60f - antennaWiggle
            val rightRad = rightAntennaAngle * PI.toFloat() / 180f
            val rightAntennaStart = Offset(
                centerX + (botWidth * 0.25f),
                headY + (headHeight * 0.15f)
            )
            val rightAntennaEnd = Offset(
                rightAntennaStart.x + antennaLength * kotlin.math.cos(rightRad),
                rightAntennaStart.y + antennaLength * sin(rightRad)
            )
            drawLine(
                color = botColor,
                start = rightAntennaStart,
                end = rightAntennaEnd,
                strokeWidth = antennaThickness,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Draw Arms (with rotation animation)
            // Left Arm
            val leftArmPivot = Offset(centerX - (botWidth / 2f) - gap - (limbWidth / 2f), bodyY + (limbWidth / 2f))
            drawRoundRect(
                color = botColor,
                topLeft = Offset(leftArmPivot.x - (limbWidth / 2f), leftArmPivot.y - (limbWidth / 2f)),
                size = Size(limbWidth, armHeight),
                cornerRadius = CornerRadius(limbWidth / 2f, limbWidth / 2f)
            )

            // Right Arm
            val rightArmPivot = Offset(centerX + (botWidth / 2f) + gap + (limbWidth / 2f), bodyY + (limbWidth / 2f))
            drawRoundRect(
                color = botColor,
                topLeft = Offset(rightArmPivot.x - (limbWidth / 2f), rightArmPivot.y - (limbWidth / 2f) + armSwingAnim),
                size = Size(limbWidth, armHeight),
                cornerRadius = CornerRadius(limbWidth / 2f, limbWidth / 2f)
            )
        }
    }
}
