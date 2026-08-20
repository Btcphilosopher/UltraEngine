package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishDivider
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishRedDark
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.timing.HistogramSnapshot

@Composable
fun LatencyHistogramChart(
    snapshot: HistogramSnapshot,
    modifier: Modifier = Modifier
) {
    val percentiles = listOf(
        "p50" to snapshot.p50Micros,
        "p90" to (snapshot.p90Nanos / 1000.0),
        "p95" to snapshot.p95Micros,
        "p99" to snapshot.p99Micros,
        "p99.9" to snapshot.p99_9Micros,
        "max" to snapshot.maxMicros
    )

    val maxVal = percentiles.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 100.0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PolishSurfaceContainer)
            .border(1.dp, PolishBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HDR LATENCY HISTOGRAM",
                    color = PolishTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PolishPurple)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${snapshot.count} samples",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Bar Histogram
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = (canvasWidth / percentiles.size) * 0.58f
                val spacing = canvasWidth / percentiles.size

                // Draw baseline
                drawLine(
                    color = PolishDivider,
                    start = Offset(0f, canvasHeight),
                    end = Offset(canvasWidth, canvasHeight),
                    strokeWidth = 2f
                )

                percentiles.forEachIndexed { index, pair ->
                    val value = pair.second
                    val barHeight = ((value / maxVal) * (canvasHeight - 12f)).toFloat().coerceAtLeast(5f)
                    val xPos = (index * spacing) + (spacing - barWidth) / 2f
                    val yPos = canvasHeight - barHeight

                    val barColor = when (pair.first) {
                        "p50", "p90" -> PolishPurple
                        "p95" -> PolishPurple.copy(alpha = 0.85f)
                        "p99" -> PolishRed
                        "p99.9" -> PolishRed.copy(alpha = 0.7f)
                        else -> PolishRedDark
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(xPos, yPos),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                percentiles.forEach { pair ->
                    val isTail = pair.first == "p99" || pair.first == "p99.9" || pair.first == "max"
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pair.first,
                            color = if (isTail) PolishRed else PolishTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.1fμs", pair.second),
                            color = if (isTail) PolishRed else PolishTextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

