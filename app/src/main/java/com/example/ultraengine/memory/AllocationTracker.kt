package com.example.ultraengine.memory

import com.example.ultraengine.core.ControlPath
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicLong

data class MemoryTelemetrySnapshot(
    val heapUsedBytes: Long,
    val heapMaxBytes: Long,
    val heapCommittedBytes: Long,
    val gcCount: Long,
    val gcTimeMillis: Long,
    val allocationRateBytesPerSec: Long,
    val allocationsPerSec: Long
)

/**
 * Allocation and GC pause monitor. Tracks allocation regressions in real time.
 */
object AllocationTracker {
    private val allocationCounter = AtomicLong(0)
    private val bytesCounter = AtomicLong(0)
    private var lastSnapshotTimeNs = System.nanoTime()
    private var lastAllocCount = 0L
    private var lastBytesCount = 0L

    fun recordAllocation(bytes: Long = 64L) {
        allocationCounter.incrementAndGet()
        bytesCounter.addAndGet(bytes)
    }

    @ControlPath("Sample JVM memory and GC statistics")
    fun sample(): MemoryTelemetrySnapshot {
        val runtime = Runtime.getRuntime()
        val totalHeap = runtime.totalMemory()
        val freeHeap = runtime.freeMemory()
        val usedHeap = totalHeap - freeHeap
        val maxHeap = runtime.maxMemory()

        var gcTotalCount = 0L
        var gcTotalTime = 0L

        try {
            val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
            for (gc in gcBeans) {
                val count = gc.collectionCount
                val time = gc.collectionTime
                if (count > 0) gcTotalCount += count
                if (time > 0) gcTotalTime += time
            }
        } catch (_: Throwable) {
            // Android Dalvik/ART fallback
        }

        val nowNs = System.nanoTime()
        val elapsedSec = (nowNs - lastSnapshotTimeNs) / 1_000_000_000.0
        val currentAllocs = allocationCounter.get()
        val currentBytes = bytesCounter.get()

        val allocRate = if (elapsedSec > 0.05) ((currentAllocs - lastAllocCount) / elapsedSec).toLong() else 0L
        val bytesRate = if (elapsedSec > 0.05) ((currentBytes - lastBytesCount) / elapsedSec).toLong() else 0L

        if (elapsedSec > 0.2) {
            lastSnapshotTimeNs = nowNs
            lastAllocCount = currentAllocs
            lastBytesCount = currentBytes
        }

        return MemoryTelemetrySnapshot(
            heapUsedBytes = usedHeap,
            heapMaxBytes = maxHeap,
            heapCommittedBytes = totalHeap,
            gcCount = gcTotalCount,
            gcTimeMillis = gcTotalTime,
            allocationRateBytesPerSec = bytesRate,
            allocationsPerSec = allocRate
        )
    }
}
