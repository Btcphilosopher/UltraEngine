package com.example.ultraengine.events

import com.example.ultraengine.concurrency.ThreadAffinity
import com.example.ultraengine.concurrency.WaitStrategy
import com.example.ultraengine.concurrency.YieldingWaitStrategy
import com.example.ultraengine.core.EngineState
import com.example.ultraengine.core.HotPath
import com.example.ultraengine.queues.MpscRingBuffer
import com.example.ultraengine.queues.RingBuffer
import com.example.ultraengine.timing.HierarchicalTimerWheel
import com.example.ultraengine.timing.NanoClock
import java.util.concurrent.atomic.AtomicReference

/**
 * Dedicated ultra-low latency event loop.
 * Runs on a dedicated CPU thread, continuously draining ring buffer events, firing timer wheels,
 * and dispatching via pre-registered handlers.
 */
class EventLoop(
    val name: String = "UltraEventLoop",
    val queueCapacity: Int = 1 shl 16,
    val waitStrategy: WaitStrategy = YieldingWaitStrategy(),
    val eventBus: EventBus = EventBus(),
    val timerWheel: HierarchicalTimerWheel = HierarchicalTimerWheel()
) {
    val ringBuffer: RingBuffer<FastEvent> = MpscRingBuffer(queueCapacity)
    private val state = AtomicReference(EngineState.STOPPED)
    private var workerThread: Thread? = null

    // Execution performance metrics
    @Volatile var totalEventsProcessed: Long = 0L
    @Volatile var totalBatchesExecuted: Long = 0L
    @Volatile var lastLoopTickNs: Long = 0L

    fun start() {
        if (!state.compareAndSet(EngineState.STOPPED, EngineState.RUNNING)) return

        workerThread = ThreadAffinity.createNamedThreadFactory(name).newThread {
            runLoop()
        }
        workerThread?.start()
    }

    fun stop() {
        if (state.compareAndSet(EngineState.RUNNING, EngineState.SHUTTING_DOWN)) {
            workerThread?.interrupt()
            workerThread?.join(500)
            state.set(EngineState.STOPPED)
        }
    }

    @HotPath("Dedicated event loop execution step")
    private fun runLoop() {
        var idleCount = 0
        val batchConsumer: (FastEvent) -> Unit = { event ->
            eventBus.publish(event)
            totalEventsProcessed++
        }

        while (state.get() == EngineState.RUNNING) {
            val nowNs = NanoClock.nowNanos()
            lastLoopTickNs = nowNs

            // 1. Drain pending events in batches
            val drained = ringBuffer.drain(batchConsumer, maxBatchSize = 64)

            // 2. Advance timer wheel ticks
            val expiredTimers = timerWheel.advance()

            if (drained > 0 || expiredTimers > 0) {
                idleCount = 0
                totalBatchesExecuted++
            } else {
                idleCount++
                waitStrategy.idle(idleCount)
            }
        }
    }

    @HotPath("Submit fast event into the event loop ring buffer")
    fun submit(event: FastEvent): Boolean {
        return ringBuffer.offer(event)
    }
}
