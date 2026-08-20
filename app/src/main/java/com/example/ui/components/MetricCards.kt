package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishAmberContainer
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishGreenContainer
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishRedContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.core.EngineState

@Composable
fun StatusPill(state: EngineState, modifier: Modifier = Modifier) {
    val (bgColor, fgColor, text) = when (state) {
        EngineState.RUNNING -> Triple(PolishBlueContainer, PolishBlue, "OPTIMIZED")
        EngineState.PAUSED -> Triple(PolishAmberContainer, PolishAmber, "PAUSED")
        EngineState.STOPPED -> Triple(PolishRedContainer, PolishRed, "STOPPED")
        EngineState.STARTING -> Triple(PolishPurpleContainer, PolishPurple, "INITIALIZING")
        EngineState.SHUTTING_DOWN -> Triple(PolishBorder, PolishTextSecondary, "OFFLINE")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, fgColor.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (state == EngineState.RUNNING) fgColor.copy(alpha = alphaPulse) else fgColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = fgColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    progressFraction: Float = 0.75f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PolishSurface)
            .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = PolishTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = PolishTextPrimary,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = PolishTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Theme accent progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PolishPurpleContainer.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
fun LatencyPercentileRow(
    label: String,
    micros: Double,
    targetMicros: Double,
    isHighlighted: Boolean = false,
    progressPercent: Float = 0.2f,
    isLimitOrTail: Boolean = false
) {
    val isExceeded = micros > targetMicros
    val rowTextColor = if (isLimitOrTail || isExceeded) PolishRed else PolishTextSecondary
    val barColor = if (isLimitOrTail || isExceeded) PolishRed else PolishPurple

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = if (isLimitOrTail) PolishRed else PolishTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isHighlighted || isLimitOrTail) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(target < ${targetMicros.toInt()}μs)",
                    color = PolishTextMuted,
                    fontSize = 10.sp
                )
            }

            Text(
                text = String.format("%.1f μs", micros),
                color = if (isLimitOrTail) PolishRed else PolishTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Clean Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(PolishBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressPercent.coerceIn(0.02f, 1.0f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

