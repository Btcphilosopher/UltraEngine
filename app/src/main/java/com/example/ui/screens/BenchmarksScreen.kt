package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UltraEngineViewModel
import com.example.ui.components.LatencyHistogramChart
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishPurpleDark
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.benchmarks.BenchmarkResult

@Composable
fun BenchmarksScreen(
    viewModel: UltraEngineViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.benchmarkReport.collectAsState()
    val isBenchmarking by viewModel.isBenchmarking.collectAsState()
    val progress by viewModel.benchmarkProgress.collectAsState()
    val statusText by viewModel.benchmarkStatusText.collectAsState()

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PolishCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header ---
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
                                .background(PolishPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = PolishPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Benchmark Suite",
                                color = PolishTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "LOCK-FREE QUEUES & WIRE FRAMING",
                                color = PolishPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    if (report != null) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("UltraEngine Benchmark Report", report?.toMarkdown()))
                                Toast.makeText(context, "Benchmark Report copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Report", tint = PolishPurple)
                        }
                    }
                }
            }
        }

        // --- Run Suite Action Card ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PolishSurface)
                    .border(1.dp, PolishBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "FULL SUITE EXECUTION",
                        color = PolishTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SPSC, MPSC, Unboxed, EventBus, ZeroCopy, Timers & Matcher",
                        color = PolishTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isBenchmarking) {
                        Column {
                            Text(text = statusText, color = PolishPurple, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = PolishPurple,
                                trackColor = PolishPurpleContainer,
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.runFullBenchmarkSuite() },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPurple, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Run All Benchmarks (JIT Warmup + Measured)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Benchmark Results List ---
        if (report != null && report?.results?.isNotEmpty() == true) {
            item {
                Text(
                    text = "MEASURED RESULTS",
                    color = PolishTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            items(report?.results ?: emptyList()) { result ->
                BenchmarkResultCard(result = result)
            }
        }
    }
}

@Composable
fun BenchmarkResultCard(result: BenchmarkResult) {
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
                    text = result.benchmarkName,
                    color = PolishTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%,d ops/s", result.throughputOpsPerSec),
                    color = PolishPurple,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentiles row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolishSurfaceContainer, RoundedCornerShape(10.dp))
                    .border(1.dp, PolishBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "p50", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.2fμs", result.histogram.p50Micros), color = PolishPurple, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "p95", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.2fμs", result.histogram.p95Micros), color = PolishPurple, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "p99", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.2fμs", result.histogram.p99Micros), color = PolishRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "p99.9", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.2fμs", result.histogram.p99_9Micros), color = PolishRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Max", color = PolishTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = String.format("%.2fμs", result.histogram.maxMicros), color = PolishTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LatencyHistogramChart(snapshot = result.histogram)
        }
    }
}

