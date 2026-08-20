package com.example.ultraengine.scheduling

import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicLong

enum class BackpressureStrategy {
    DROP_OLDEST,
    DROP_NEWEST,
    REJECT,
    PRIORITY,
    BLOCK_CONTROLLED
}

enum class EventPriority(val level: Int) {
    CRITICAL(0),
    HIGH(1),
    NORMAL(2),
    LOW(3),
    BACKGROUND(4)
}

/**
 * Backpressure and load-shedding coordinator.
 * Prevents memory exhaustion and unconstrained tail latency during peak bursts.
 */
class BackpressureController(
    val strategy: BackpressureStrategy = BackpressureStrategy.BLOCK_CONTROLLED,
    val maxQueueDepth: Int = 100_000
) {
    private val droppedEventsCount = AtomicLong(0L)
    private val rejectedEventsCount = AtomicLong(0L)
    private val processedEventsCount = AtomicLong(0L)

    val droppedCount: Long get() = droppedEventsCount.get()
    val rejectedCount: Long get() = rejectedEventsCount.get()
    val processedCount: Long get() = processedEventsCount.get()

    @HotPath("Decide whether to accept or shed event based on current queue depth")
    fun shouldAccept(currentDepth: Int, priority: EventPriority = EventPriority.NORMAL): Boolean {
        if (currentDepth < maxQueueDepth) {
            processedEventsCount.incrementAndGet()
            return true
        }

        // Under overload:
        when (strategy) {
            BackpressureStrategy.DROP_NEWEST -> {
                droppedEventsCount.incrementAndGet()
                return false
            }
            BackpressureStrategy.REJECT -> {
                rejectedEventsCount.incrementAndGet()
                return false
            }
            BackpressureStrategy.PRIORITY -> {
                if (priority == EventPriority.CRITICAL || priority == EventPriority.HIGH) {
                    processedEventsCount.incrementAndGet()
                    return true
                } else {
                    droppedEventsCount.incrementAndGet()
                    return false
                }
            }
            BackpressureStrategy.DROP_OLDEST,
            BackpressureStrategy.BLOCK_CONTROLLED -> {
                processedEventsCount.incrementAndGet()
                return true
            }
        }
    }

    fun reset() {
        droppedEventsCount.set(0)
        rejectedEventsCount.set(0)
        processedEventsCount.set(0)
    }
}
