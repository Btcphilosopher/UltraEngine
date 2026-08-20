package com.example.ultraengine.timing

import com.example.ultraengine.core.HotPath

/**
 * High-resolution monotonic clock for latency measurements and deadlines.
 * Never uses System.currentTimeMillis() on latency-critical paths.
 */
object NanoClock {
    @HotPath
    inline fun nowNanos(): Long = System.nanoTime()

    @HotPath
    inline fun nowMicros(): Long = System.nanoTime() / 1_000L

    @HotPath
    inline fun nowMillis(): Long = System.nanoTime() / 1_000_000L

    @HotPath
    inline fun elapsedNanos(startNanos: Long): Long = System.nanoTime() - startNanos

    @HotPath
    inline fun elapsedMicros(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000L

    @HotPath
    inline fun isExpired(deadlineNanos: Long): Boolean = System.nanoTime() >= deadlineNanos
}
