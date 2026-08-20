package com.example.ultraengine.telemetry

import com.example.ultraengine.core.ControlPath
import com.example.ultraengine.core.HotPath
import com.example.ultraengine.memory.AllocationTracker
import com.example.ultraengine.memory.MemoryTelemetrySnapshot
import com.example.ultraengine.timing.HdrHistogram
import com.example.ultraengine.timing.HistogramSnapshot
import java.util.concurrent.atomic.AtomicLong

data class EngineTelemetrySnapshot(
    val uptimeMillis: Long,
    val throughputEventsPerSec: Long,
    val totalEventsProcessed: Long,
    val inProcessLatency: HistogramSnapshot,
    val queueLatency: HistogramSnapshot,
    val serializationLatency: HistogramSnapshot,
    val queueDepth: Int,
    val droppedEventsCount: Long,
    val memory: MemoryTelemetrySnapshot
)

/**
 * Low-latency telemetry aggregator.
 * Latency histograms are updated asynchronously without blocking event loops.
 */
class EngineTelemetry {
    val inProcessLatencyHistogram = HdrHistogram()
    val queueLatencyHistogram = HdrHistogram()
    val serializationLatencyHistogram = HdrHistogram()

    private val totalEventsCounter = AtomicLong(0L)
    private val droppedEventsCounter = AtomicLong(0L)
    private var lastSampleTimeNs = System.nanoTime()
    private var lastEventCount = 0L
    private val startTimeMillis = System.currentTimeMillis()

    @HotPath("Record in-process execution latency")
    fun recordInProcessLatency(nanos: Long) {
        inProcessLatencyHistogram.record(nanos)
        totalEventsCounter.incrementAndGet()
    }

    @HotPath("Record queue latency")
    fun recordQueueLatency(nanos: Long) {
        queueLatencyHistogram.record(nanos)
    }

    @HotPath("Record binary serialization latency")
    fun recordSerializationLatency(nanos: Long) {
        serializationLatencyHistogram.record(nanos)
    }

    fun recordDroppedEvent() {
        droppedEventsCounter.incrementAndGet()
    }

    @ControlPath("Sample aggregate engine telemetry")
    fun sample(currentQueueDepth: Int = 0): EngineTelemetrySnapshot {
        val nowNs = System.nanoTime()
        val elapsedSec = (nowNs - lastSampleTimeNs) / 1_000_000_000.0
        val currentEvents = totalEventsCounter.get()
        val throughput = if (elapsedSec > 0.1) ((currentEvents - lastEventCount) / elapsedSec).toLong() else 0L

        if (elapsedSec > 0.5) {
            lastSampleTimeNs = nowNs
            lastEventCount = currentEvents
        }

        return EngineTelemetrySnapshot(
            uptimeMillis = System.currentTimeMillis() - startTimeMillis,
            throughputEventsPerSec = throughput,
            totalEventsProcessed = currentEvents,
            inProcessLatency = inProcessLatencyHistogram.getSnapshot(),
            queueLatency = queueLatencyHistogram.getSnapshot(),
            serializationLatency = serializationLatencyHistogram.getSnapshot(),
            queueDepth = currentQueueDepth,
            droppedEventsCount = droppedEventsCounter.get(),
            memory = AllocationTracker.sample()
        )
    }

    fun reset() {
        inProcessLatencyHistogram.reset()
        queueLatencyHistogram.reset()
        serializationLatencyHistogram.reset()
        totalEventsCounter.set(0)
        droppedEventsCounter.set(0)
    }
}
