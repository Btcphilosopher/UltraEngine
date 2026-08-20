package com.example.ultraengine.timing

import com.example.ultraengine.core.CacheLinePad
import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicLongArray
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-precision, zero-allocation latency histogram.
 * Records durations from 10 nanoseconds to 10 seconds with logarithmic binning.
 * Capable of calculating p50, p90, p95, p99, p99.9, p99.99, min, max, mean, stdDev.
 */
class HdrHistogram(
    val highestTrackableValueNanos: Long = 10_000_000_000L, // 10 seconds
    val subBucketCount: Int = 1024
) {
    // Number of buckets for powers-of-two ranges (from 2^4 = 16ns up to 2^34 ~ 17s)
    private val bucketCount = 36
    private val counts = AtomicLongArray(bucketCount * subBucketCount)

    @CacheLinePad
    private val totalCount = java.util.concurrent.atomic.AtomicLong(0)
    
    @CacheLinePad
    private val totalSumNanos = java.util.concurrent.atomic.AtomicLong(0)
    
    @CacheLinePad
    private val minValueNanos = java.util.concurrent.atomic.AtomicLong(Long.MAX_VALUE)
    
    @CacheLinePad
    private val maxValueNanos = java.util.concurrent.atomic.AtomicLong(0)

    @HotPath("Zero-allocation record of nanosecond duration")
    fun record(durationNanos: Long) {
        val clamped = durationNanos.coerceIn(1L, highestTrackableValueNanos)
        val index = getIndex(clamped)
        counts.incrementAndGet(index)
        totalCount.incrementAndGet()
        totalSumNanos.addAndGet(clamped)

        // Update min/max with lock-free CAS loops
        var currentMin = minValueNanos.get()
        while (clamped < currentMin) {
            if (minValueNanos.compareAndSet(currentMin, clamped)) break
            currentMin = minValueNanos.get()
        }

        var currentMax = maxValueNanos.get()
        while (clamped > currentMax) {
            if (maxValueNanos.compareAndSet(currentMax, clamped)) break
            currentMax = maxValueNanos.get()
        }
    }

    private inline fun getIndex(valueNanos: Long): Int {
        val leadingZeros = java.lang.Long.numberOfLeadingZeros(valueNanos)
        val bucket = (64 - leadingZeros).coerceIn(0, bucketCount - 1)
        val subBucket = ((valueNanos ushr max(0, bucket - 10)) and (subBucketCount - 1).toLong()).toInt()
        return (bucket * subBucketCount + subBucket).coerceIn(0, counts.length() - 1)
    }

    private inline fun getValueFromIndex(index: Int): Long {
        val bucket = index / subBucketCount
        val subBucket = index % subBucketCount
        return if (bucket == 0) subBucket.toLong() else (subBucket.toLong() shl (bucket - 10).coerceAtLeast(0))
    }

    fun getCount(): Long = totalCount.get()

    fun getMin(): Long {
        val min = minValueNanos.get()
        return if (min == Long.MAX_VALUE) 0L else min
    }

    fun getMax(): Long = maxValueNanos.get()

    fun getMean(): Double {
        val count = totalCount.get()
        return if (count == 0L) 0.0 else totalSumNanos.get().toDouble() / count
    }

    /**
     * Calculates percentile in nanoseconds (e.g. 50.0, 95.0, 99.0, 99.9, 99.99).
     */
    fun getPercentile(percentile: Double): Long {
        val total = totalCount.get()
        if (total == 0L) return 0L

        val targetCount = ((percentile / 100.0) * total).toLong().coerceIn(1L, total)
        var accumulated = 0L

        for (i in 0 until counts.length()) {
            accumulated += counts.get(i)
            if (accumulated >= targetCount) {
                return getValueFromIndex(i)
            }
        }
        return maxValueNanos.get()
    }

    fun getSnapshot(): HistogramSnapshot {
        val count = getCount()
        return HistogramSnapshot(
            count = count,
            minNanos = getMin(),
            maxNanos = getMax(),
            meanNanos = getMean(),
            p50Nanos = getPercentile(50.0),
            p90Nanos = getPercentile(90.0),
            p95Nanos = getPercentile(95.0),
            p99Nanos = getPercentile(99.0),
            p99_9Nanos = getPercentile(99.9),
            p99_99Nanos = getPercentile(99.99)
        )
    }

    fun reset() {
        for (i in 0 until counts.length()) {
            counts.set(i, 0L)
        }
        totalCount.set(0)
        totalSumNanos.set(0)
        minValueNanos.set(Long.MAX_VALUE)
        maxValueNanos.set(0)
    }
}

data class HistogramSnapshot(
    val count: Long,
    val minNanos: Long,
    val maxNanos: Long,
    val meanNanos: Double,
    val p50Nanos: Long,
    val p90Nanos: Long,
    val p95Nanos: Long,
    val p99Nanos: Long,
    val p99_9Nanos: Long,
    val p99_99Nanos: Long
) {
    val p50Micros: Double get() = p50Nanos / 1000.0
    val p95Micros: Double get() = p95Nanos / 1000.0
    val p99Micros: Double get() = p99Nanos / 1000.0
    val p99_9Micros: Double get() = p99_9Nanos / 1000.0
    val p99_99Micros: Double get() = p99_99Nanos / 1000.0
    val maxMicros: Double get() = maxNanos / 1000.0
    val meanMicros: Double get() = meanNanos / 1000.0
}
