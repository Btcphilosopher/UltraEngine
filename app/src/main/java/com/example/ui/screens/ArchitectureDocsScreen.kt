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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishGreen
import com.example.ui.theme.PolishGreenDark
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishSurfaceContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary

@Composable
fun ArchitectureDocsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PolishCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = PolishPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Architecture Blueprint",
                                color = PolishTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "ULTRA-LOW-LATENCY SPECIFICATION",
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

        item {
            DocSectionCard(
                title = "1. LATENCY TARGET ARCHITECTURE",
                icon = Icons.Default.Speed,
                accentColor = PolishPurple,
                content = """
                    • p50  < 100 μs (Median in-memory transaction)
                    • p95  < 250 μs (95th percentile under continuous load)
                    • p99  < 500 μs (Tail latency budget)
                    • p99.9 < 1.0 ms (Worst-case extreme tail latency)
                    
                    Distinguishes in-process, thread-to-thread, serialization, and queueing latency stages.
                """.trimIndent()
            )
        }

        item {
            DocSectionCard(
                title = "2. LOCK-FREE RING BUFFERS & CACHE PADDING",
                icon = Icons.Default.Memory,
                accentColor = PolishPurple,
                content = """
                    • SPSC: Single Producer Single Consumer with Store-Store (lazySet) release barriers and power-of-2 bitwise masking.
                    • MPSC: Multi-Producer Single-Consumer lock-free CAS sequence reservations with zero contention on consumer drain.
                    • MPMC: Multi-Producer Multi-Consumer using slot sequence flags (Vyukov queue algorithm).
                    • 64-Byte Cache Line Padding: PaddedAtomicLong inserts 56 bytes before and after sequence fields to prevent L1/L2 false sharing across CPU cores.
                """.trimIndent()
            )
        }

        item {
            DocSectionCard(
                title = "3. ZERO-COPY BINARY WIRE PROTOCOL",
                icon = Icons.Default.Code,
                accentColor = PolishPurple,
                content = """
                    • Magic Bytes: 0x55 0x4C ("UL")
                    • Framing: [Magic(2B) | Version(1B) | Type(1B) | Sequence(8B) | TimestampNs(8B) | Correlation(8B) | PayloadLen(4B) | CRC32(4B) | Payload(NB)]
                    • Zero Heap Allocations: DirectByteBuffer pool with slab leasing.
                    • Streaming CRC32 checksum validation for corruption detection.
                """.trimIndent()
            )
        }

        item {
            DocSectionCard(
                title = "4. O(1) HIERARCHICAL TIMING WHEEL",
                icon = Icons.Default.Tune,
                accentColor = PolishPurple,
                content = """
                    • Replaces O(log N) PriorityQueues with an O(1) circular timing wheel.
                    • Preallocated object-pooled TimerTasks eliminate garbage creation on schedule and advance.
                    • Sub-millisecond tick resolution with support for one-shot, periodic deadlines, and fast cancellation.
                """.trimIndent()
            )
        }

        item {
            DocSectionCard(
                title = "5. JVM TUNING & LINUX DEPLOYMENT",
                icon = Icons.Default.Info,
                accentColor = PolishPurple,
                content = """
                    Recommended Production JVM Flags:
                    -XX:+UseZGC -XX:+ZGenerational
                    -XX:+AlwaysPreTouch -XX:+UseNUMA
                    -XX:+UseLargePages -XX:-UseBiasedLocking
                    -XX:CompileThreshold=1000 -XX:+TieredCompilation
                    
                    Linux Kernel Optimizations:
                    • isolcpus & taskset CPU pinning for engine threads
                    • chrt -f 99 real-time FIFO scheduling
                    • nohz_full kernel tickless isolation
                """.trimIndent()
            )
        }
    }
}

@Composable
fun DocSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: String
) {
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
                    text = title,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                color = PolishTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

