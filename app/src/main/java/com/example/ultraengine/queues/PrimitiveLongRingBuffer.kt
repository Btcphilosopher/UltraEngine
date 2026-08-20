package com.example.ultraengine.queues

import com.example.ultraengine.concurrency.PaddedAtomicLong
import com.example.ultraengine.core.CacheLinePad
import com.example.ultraengine.core.HotPath

/**
 * High-speed unboxed 64-bit primitive ring buffer.
 * Provides zero object boxing and zero heap allocations for numeric pipelines (ticks, sequences, timestamps).
 */
class PrimitiveLongRingBuffer(requestedCapacity: Int = 1024) {
    val capacity: Int = findNextPositivePowerOfTwo(requestedCapacity)
    private val mask = capacity - 1
    private val buffer = LongArray(capacity)

    private val producerHead = PaddedAtomicLong(0L)
    @CacheLinePad private var cachedConsumerTail: Long = 0L

    private val consumerTail = PaddedAtomicLong(0L)
    @CacheLinePad private var cachedProducerHead: Long = 0L

    val size: Int get() = (producerHead.get() - consumerTail.get()).toInt().coerceAtLeast(0)
    val isEmpty: Boolean get() = producerHead.get() == consumerTail.get()
    val isFull: Boolean get() = size >= capacity

    @HotPath("Offer unboxed 64-bit long")
    fun offer(value: Long): Boolean {
        val currentHead = producerHead.get()
        val wrapBoundary = currentHead - capacity

        if (wrapBoundary >= cachedConsumerTail) {
            cachedConsumerTail = consumerTail.get()
            if (wrapBoundary >= cachedConsumerTail) return false
        }

        val index = (currentHead and mask.toLong()).toInt()
        buffer[index] = value
        producerHead.lazySet(currentHead + 1)
        return true
    }

    @HotPath("Poll unboxed long (returns -1L if empty or use tryPoll)")
    fun poll(): Long {
        val currentTail = consumerTail.get()
        if (currentTail >= cachedProducerHead) {
            cachedProducerHead = producerHead.get()
            if (currentTail >= cachedProducerHead) return Long.MIN_VALUE
        }

        val index = (currentTail and mask.toLong()).toInt()
        val value = buffer[index]
        consumerTail.lazySet(currentTail + 1)
        return value
    }

    companion object {
        private fun findNextPositivePowerOfTwo(value: Int): Int {
            return 1 shl (32 - Integer.numberOfLeadingZeros(value.coerceAtLeast(1) - 1))
        }
    }
}
