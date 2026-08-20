package com.example.ultraengine.timing

import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicLong

/**
 * High-performance O(1) Timing Wheel for low-latency timer scheduling and deadlines.
 * Avoids O(log N) priority queue overhead and allocations on the hot path.
 */
class HierarchicalTimerWheel(
    val tickDurationNanos: Long = 100_000L, // 100 microseconds per tick
    val wheelSize: Int = 512                // Power of 2 wheel slots
) {
    private val mask = wheelSize - 1
    private val buckets = Array(wheelSize) { TimerBucket() }
    private var currentTick: Long = 0
    private val timerIdGen = AtomicLong(1)

    class TimerTask(
        var id: Long = 0,
        var deadlineNanos: Long = 0,
        var rounds: Long = 0,
        var cancelled: Boolean = false,
        var isPeriodic: Boolean = false,
        var periodNanos: Long = 0,
        var action: (() -> Unit)? = null,
        var next: TimerTask? = null,
        var prev: TimerTask? = null
    )

    private class TimerBucket {
        var head: TimerTask? = null

        @HotPath
        fun add(task: TimerTask) {
            task.next = head
            task.prev = null
            head?.prev = task
            head = task
        }

        @HotPath
        fun remove(task: TimerTask) {
            task.prev?.next = task.next
            task.next?.prev = task.prev
            if (head == task) {
                head = task.next
            }
            task.next = null
            task.prev = null
        }
    }

    // Object pool for timer tasks to avoid GC in hot path
    private val taskPool = Array(1024) { TimerTask() }
    private var poolIndex = 0

    @Synchronized
    private fun leaseTask(): TimerTask {
        return if (poolIndex < taskPool.size) {
            taskPool[poolIndex++]
        } else {
            TimerTask()
        }
    }

    @Synchronized
    private fun releaseTask(task: TimerTask) {
        task.action = null
        task.cancelled = true
        task.next = null
        task.prev = null
        if (poolIndex > 0) {
            taskPool[--poolIndex] = task
        }
    }

    @HotPath("Schedule one-shot timer at deadline")
    fun schedule(delayNanos: Long, action: () -> Unit): Long {
        val now = NanoClock.nowNanos()
        val deadline = now + delayNanos
        val ticks = (delayNanos / tickDurationNanos).coerceAtLeast(1L)
        val targetTick = currentTick + ticks
        val bucketIndex = (targetTick and mask.toLong()).toInt()
        val rounds = ticks / wheelSize

        val task = leaseTask().apply {
            this.id = timerIdGen.getAndIncrement()
            this.deadlineNanos = deadline
            this.rounds = rounds
            this.cancelled = false
            this.isPeriodic = false
            this.periodNanos = 0
            this.action = action
        }

        buckets[bucketIndex].add(task)
        return task.id
    }

    @HotPath("Schedule recurring periodic timer")
    fun schedulePeriodic(initialDelayNanos: Long, periodNanos: Long, action: () -> Unit): Long {
        val now = NanoClock.nowNanos()
        val deadline = now + initialDelayNanos
        val ticks = (initialDelayNanos / tickDurationNanos).coerceAtLeast(1L)
        val targetTick = currentTick + ticks
        val bucketIndex = (targetTick and mask.toLong()).toInt()
        val rounds = ticks / wheelSize

        val task = leaseTask().apply {
            this.id = timerIdGen.getAndIncrement()
            this.deadlineNanos = deadline
            this.rounds = rounds
            this.cancelled = false
            this.isPeriodic = true
            this.periodNanos = periodNanos
            this.action = action
        }

        buckets[bucketIndex].add(task)
        return task.id
    }

    @HotPath("Advance the timer wheel by one tick and fire expired timers")
    fun advance(): Int {
        var firedCount = 0
        val bucketIndex = (currentTick and mask.toLong()).toInt()
        val bucket = buckets[bucketIndex]
        var current = bucket.head

        while (current != null) {
            val next = current.next
            if (current.cancelled) {
                bucket.remove(current)
                releaseTask(current)
            } else if (current.rounds > 0) {
                current.rounds--
            } else {
                // Expired
                bucket.remove(current)
                try {
                    current.action?.invoke()
                    firedCount++
                } catch (_: Throwable) {}

                if (current.isPeriodic && !current.cancelled) {
                    val ticks = (current.periodNanos / tickDurationNanos).coerceAtLeast(1L)
                    val nextTick = currentTick + ticks
                    val nextBucketIndex = (nextTick and mask.toLong()).toInt()
                    current.rounds = ticks / wheelSize
                    buckets[nextBucketIndex].add(current)
                } else {
                    releaseTask(current)
                }
            }
            current = next
        }
        currentTick++
        return firedCount
    }
}
