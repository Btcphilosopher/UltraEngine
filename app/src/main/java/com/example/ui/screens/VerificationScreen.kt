package com.example.ui.screens

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishGreenDark
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishPurpleDark
import com.example.ui.theme.PolishRed
import com.example.ui.theme.PolishRedContainer
import com.example.ui.theme.PolishRedDark
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ultraengine.tests.TestSuiteResult

@Composable
fun VerificationScreen(
    viewModel: UltraEngineViewModel,
    modifier: Modifier = Modifier
) {
    val testResults by viewModel.testResults.collectAsState()
    val isRunningTests by viewModel.isRunningTests.collectAsState()
    val replayStats by viewModel.replayStats.collectAsState()

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
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = PolishPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Verification & Chaos",
                                color = PolishTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "CONCURRENCY • LOG CRC • DETERMINISM",
                                color = PolishPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Run Verification Action Card ---
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
                        text = "AUTOMATED TEST SUITE",
                        color = PolishTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Executes MPMC multi-threaded race conditions, load-shedding policies, CRC32 deterministic log validation, and memory leak soak test.",
                        color = PolishTextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isRunningTests) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = PolishPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Running multi-threaded stress tests...", color = PolishPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.runVerificationTests() },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPurple, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Run All Chaos & Concurrency Tests", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Test Results ---
        if (testResults.isNotEmpty()) {
            item {
                Text(
                    text = "VERIFICATION TEST RESULTS",
                    color = PolishTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            items(testResults) { test ->
                TestResultCard(test = test)
            }
        }

        // --- Deterministic Event Log Replayer Card ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVENT JOURNAL REPLAY ENGINE",
                            color = PolishTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = PolishPurple, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reads binary append-only event log from disk, re-calculates CRC32 checksums, and replays state machine for exact deterministic reproduction.",
                        color = PolishTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.triggerEventReplay() },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPurpleContainer, contentColor = PolishPurpleDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Replay Journal from Disk", fontWeight = FontWeight.Bold)
                    }

                    if (replayStats != null) {
                        val stats = replayStats!!
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PolishSurfaceContainer, RoundedCornerShape(10.dp))
                                .border(1.dp, PolishBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "Replayed: %,d records".format(stats.totalRecordsReplayed), color = PolishGreenDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Corrupted/Dropped: ${stats.corruptedRecordsCount}", color = if (stats.corruptedRecordsCount == 0L) PolishGreenDark else PolishRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Speed: %,d events/sec".format(stats.eventsPerSecond), color = PolishPurple, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestResultCard(test: TestSuiteResult) {
    val statusColor = if (test.passed) PolishGreenDark else PolishRed
    val borderColor = if (test.passed) PolishGreen.copy(alpha = 0.5f) else PolishRed.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PolishSurface)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (test.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = test.testName,
                        color = PolishTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = test.message,
                    color = PolishTextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "${test.elapsedMillis}ms",
                color = PolishPurple,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

