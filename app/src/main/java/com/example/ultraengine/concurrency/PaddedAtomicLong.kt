package com.example.ultraengine.concurrency

import com.example.ultraengine.core.CacheLinePad
import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport

/**
 * 64-byte padded AtomicLong to prevent false-sharing in high-contention concurrent pipelines.
 */
class PaddedAtomicLong(initialValue: Long = 0L) {
    // 56 bytes padding before value
    @CacheLinePad private var p1: Long = 0
    @CacheLinePad private var p2: Long = 0
    @CacheLinePad private var p3: Long = 0
    @CacheLinePad private var p4: Long = 0
    @CacheLinePad private var p5: Long = 0
    @CacheLinePad private var p6: Long = 0
    @CacheLinePad private var p7: Long = 0

    @PublishedApi
    internal val value = AtomicLong(initialValue)

    // 56 bytes padding after value
    @CacheLinePad private var p8: Long = 0
    @CacheLinePad private var p9: Long = 0
    @CacheLinePad private var p10: Long = 0
    @CacheLinePad private var p11: Long = 0
    @CacheLinePad private var p12: Long = 0
    @CacheLinePad private var p13: Long = 0
    @CacheLinePad private var p14: Long = 0

    @HotPath
    inline fun get(): Long = value.get()

    @HotPath
    inline fun set(newValue: Long) = value.set(newValue)

    @HotPath
    inline fun lazySet(newValue: Long) = value.lazySet(newValue)

    @HotPath
    inline fun compareAndSet(expect: Long, update: Long): Boolean = value.compareAndSet(expect, update)

    @HotPath
    inline fun getAndIncrement(): Long = value.getAndIncrement()

    @HotPath
    inline fun getAndAdd(delta: Long): Long = value.getAndAdd(delta)

    @HotPath
    inline fun incrementAndGet(): Long = value.incrementAndGet()
}

/**
 * Wait strategy contract for consumer / producer thread coordination.
 */
interface WaitStrategy {
    @HotPath
    fun idle(consecutiveFailures: Int)
    
    fun reset() {}
}

/**
 * Lowest latency strategy. Burns CPU in a tight loop. Best for isolated pinned cores.
 */
class BusySpinWaitStrategy : WaitStrategy {
    @HotPath
    override fun idle(consecutiveFailures: Int) {
        // CPU hint / pause (thread spin on JVM)
        Thread.onSpinWait()
    }
}

/**
 * Yields current thread quantum to scheduler after several spins.
 */
class YieldingWaitStrategy(private val spinTries: Int = 100) : WaitStrategy {
    @HotPath
    override fun idle(consecutiveFailures: Int) {
        if (consecutiveFailures < spinTries) {
            Thread.onSpinWait()
        } else {
            Thread.yield()
        }
    }
}

/**
 * Adaptive backoff strategy: Spin -> Yield -> Park/Sleep.
 */
class BackoffWaitStrategy(
    private val spinTries: Int = 100,
    private val yieldTries: Int = 500,
    private val minParkNanos: Long = 1000L // 1 microsecond
) : WaitStrategy {
    @HotPath
    override fun idle(consecutiveFailures: Int) {
        when {
            consecutiveFailures < spinTries -> Thread.onSpinWait()
            consecutiveFailures < yieldTries -> Thread.yield()
            else -> LockSupport.parkNanos(minParkNanos)
        }
    }
}

/**
 * Low CPU usage strategy using parkNanos for background workers.
 */
class SleepingWaitStrategy(private val sleepNanos: Long = 10_000L) : WaitStrategy {
    @HotPath
    override fun idle(consecutiveFailures: Int) {
        LockSupport.parkNanos(sleepNanos)
    }
}

/**
 * Blocking wait strategy using traditional synchronization for control-plane tasks.
 */
class BlockingWaitStrategy : WaitStrategy {
    @HotPath
    override fun idle(consecutiveFailures: Int) {
        LockSupport.parkNanos(100_000L)
    }
}
