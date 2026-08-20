package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UltraEngineViewModel
import com.example.ui.components.LatencyHistogramChart
import com.example.ui.components.LatencyPercentileRow
import com.example.ui.components.StatusPill
import com.example.ui.components.TelemetryMetricCard
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishAmberContainer
import com.example.ui.theme.PolishBlue
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishDivider
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishPurpleDark
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishRedContainer
import com.example.ui.theme.PolishRedDark
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.core.EngineState

@Composable
fun TelemetryScreen(
    viewModel: UltraEngineViewModel,
    modifier: Modifier = Modifier
) {
    val engineState by viewModel.engineState.collectAsState()
    val telemetry by viewModel.telemetrySnapshot.collectAsState()
    val waitStrategyName by viewModel.waitStrategyName.collectAsState()
    val backpressureStrategy by viewModel.backpressureStrategy.collectAsState()
    val targetIngestRate by viewModel.targetIngestRate.collectAsState()

    val snapshot = telemetry

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PolishCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Professional Top Header Bar ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PolishSurfaceHeader)
                    .border(1.dp, PolishBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "UltraEngine",
                                color = PolishTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "CORE V1.4.2",
                                color = PolishPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(state = engineState)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.resetTelemetry() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Metrics",
                                tint = PolishTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // --- Engine Control Action Buttons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startEngine() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engineState == EngineState.RUNNING) PolishPurpleContainer else PolishPurple,
                        contentColor = if (engineState == EngineState.RUNNING) PolishPurpleDark else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Start", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.pauseEngine() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engineState == EngineState.PAUSED) PolishAmberContainer else PolishSurface,
                        contentColor = if (engineState == EngineState.PAUSED) PolishAmber else PolishTextSecondary
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PolishBorder))
                ) {
                    Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pause", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.stopEngine() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (engineState == EngineState.STOPPED) PolishRedContainer else PolishSurface,
                        contentColor = if (engineState == EngineState.STOPPED) PolishRed else PolishTextSecondary
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PolishBorder))
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Stop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Metric Gauges Grid ---
        item {
            val ops = snapshot?.throughputEventsPerSec ?: 0L
            val qDepth = snapshot?.queueDepth ?: 0
            val p50 = snapshot?.inProcessLatency?.p50Micros ?: 0.0
            val p99 = snapshot?.inProcessLatency?.p99Micros ?: 0.0
            val memAlloc = snapshot?.memory?.allocationsPerSec ?: 0L
            val allocMb = (memAlloc * 64.0) / (1024.0 * 1024.0)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMetricCard(
                        title = "Throughput",
                        value = if (ops >= 1_000_000) String.format("%.2f M/s", ops / 1_000_000.0) else String.format("%,d/s", ops),
                        subtitle = "Target: %,d/s".format(targetIngestRate),
                        icon = Icons.Default.Speed,
                        accentColor = PolishPurple,
                        progressFraction = (ops.toFloat() / targetIngestRate.coerceAtLeast(1)).coerceIn(0.1f, 1f),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        title = "Allocation",
                        value = String.format("%.2f MB/s", allocMb),
                        subtitle = "${String.format("%,d", memAlloc)} obj/s",
                        icon = Icons.Default.Memory,
                        accentColor = PolishRedDark,
                        progressFraction = (allocMb.toFloat() / 10f).coerceIn(0.08f, 0.9f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMetricCard(
                        title = "p50 Latency",
                        value = String.format("%.1f μs", p50),
                        subtitle = "Target < 100 μs",
                        icon = Icons.Default.Timer,
                        accentColor = PolishPurple,
                        progressFraction = (p50.toFloat() / 100f).coerceIn(0.05f, 1f),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricCard(
                        title = "Ring Depth",
                        value = String.format("%,d", qDepth),
                        subtitle = "${snapshot?.droppedEventsCount ?: 0} dropped",
                        icon = Icons.Default.Storage,
                        accentColor = if (qDepth > 1000) PolishRed else PolishBlue,
                        progressFraction = (qDepth.toFloat() / 4096f).coerceIn(0.05f, 1f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Latency Profile Card (Container in PolishSurfaceContainer #F7F2FA) ---
        item {
            val inProc = snapshot?.inProcessLatency
            val p50Val = inProc?.p50Micros ?: 42.4
            val p95Val = inProc?.p95Micros ?: 112.8
            val p99Val = inProc?.p99Micros ?: 181.2
            val p999Val = inProc?.p99_9Micros ?: 410.5

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PolishSurfaceContainer)
                    .border(1.dp, PolishBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATENCY PROFILE (μs)",
                            color = PolishTextPrimary,
                            fontSize = 13.sp,
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
                                text = "REAL-TIME",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LatencyPercentileRow(
                        label = "p50 (Median)",
                        micros = p50Val,
                        targetMicros = 100.0,
                        isHighlighted = true,
                        progressPercent = (p50Val / 250.0).toFloat().coerceIn(0.1f, 0.95f),
                        isLimitOrTail = false
                    )

                    LatencyPercentileRow(
                        label = "p95 (Target)",
                        micros = p95Val,
                        targetMicros = 250.0,
                        progressPercent = (p95Val / 300.0).toFloat().coerceIn(0.2f, 0.95f),
                        isLimitOrTail = false
                    )

                    LatencyPercentileRow(
                        label = "p99 (Limit)",
                        micros = p99Val,
                        targetMicros = 500.0,
                        isHighlighted = true,
                        progressPercent = (p99Val / 600.0).toFloat().coerceIn(0.3f, 0.95f),
                        isLimitOrTail = true
                    )

                    LatencyPercentileRow(
                        label = "p99.9 (Tail)",
                        micros = p999Val,
                        targetMicros = 1000.0,
                        progressPercent = (p999Val / 1200.0).toFloat().coerceIn(0.4f, 0.95f),
                        isLimitOrTail = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Spec Row in Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(PolishDivider)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "JIT WARMUP", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "COMPLETE", color = PolishTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "GC POLICY", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "ZGC-PAUSE", color = PolishTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "BACKPRESSURE", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = backpressureStrategy.name, color = PolishTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // --- Thread Pinning & CPU Affinity Card ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurface)
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PolishSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = PolishPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "THREAD PINNING",
                                color = PolishTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "CPU Affinity: Core 4, 5, 6, 7 Active",
                                color = PolishTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Overlapping core avatars
                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PolishPurple)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PolishPurple)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PolishPurpleContainer)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+4", color = PolishPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- HDR Histogram Visualizer ---
        item {
            if (snapshot != null && snapshot.inProcessLatency.count > 0) {
                LatencyHistogramChart(snapshot = snapshot.inProcessLatency)
            }
        }

        // --- JVM Memory & GC Telemetry Card ---
        item {
            val mem = snapshot?.memory
            val usedMb = (mem?.heapUsedBytes ?: 0L) / (1024 * 1024)
            val maxMb = (mem?.heapMaxBytes ?: 0L) / (1024 * 1024)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                            text = "JVM MEMORY & GC TELEMETRY",
                            color = PolishTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = PolishPurple, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Heap Used", color = PolishTextMuted, fontSize = 11.sp)
                            Text(text = "$usedMb / $maxMb MB", color = PolishTextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Allocations", color = PolishTextMuted, fontSize = 11.sp)
                            Text(text = String.format("%,d/s", mem?.allocationsPerSec ?: 0L), color = PolishPurple, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "GC Pause Time", color = PolishTextMuted, fontSize = 11.sp)
                            Text(text = "${mem?.gcTimeMillis ?: 0}ms (${mem?.gcCount ?: 0} cols)", color = PolishAmber, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Concurrency & Queue Configuration Tuning ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurface)
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "CONCURRENCY & WAIT STRATEGY",
                        color = PolishTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Strategy Selector Chips
                    val strategies = listOf("BusySpin", "Yielding", "Backoff", "Sleeping")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        strategies.forEach { strat ->
                            val isSelected = waitStrategyName == strat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PolishPurpleContainer else PolishSurfaceVariant)
                                    .border(1.dp, if (isSelected) PolishPurple else PolishBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setWaitStrategy(strat) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strat,
                                    color = if (isSelected) PolishPurpleDark else PolishTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Ingest Rate Throttle: %,d events/sec".format(targetIngestRate),
                        color = PolishTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Slider(
                        value = targetIngestRate.toFloat(),
                        onValueChange = { viewModel.setTargetIngestRate(it.toInt()) },
                        valueRange = 5_000f..200_000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = PolishPurple,
                            activeTrackColor = PolishPurple,
                            inactiveTrackColor = PolishPurpleContainer
                        )
                    )
                }
            }
        }
    }
}

