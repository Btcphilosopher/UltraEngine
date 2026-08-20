package com.example.ultraengine.queues

import com.example.ultraengine.concurrency.PaddedAtomicLong
import com.example.ultraengine.core.CacheLinePad
import com.example.ultraengine.core.HotPath

/**
 * Single-Producer Single-Consumer (SPSC) lock-free Ring Buffer.
 * Uses power-of-2 capacity masking and cache-line padded sequence counters.
 * Zero locks, zero CAS on the hot path (only memory release/acquire barriers).
 */
class SpscRingBuffer<E : Any>(requestedCapacity: Int = 1024) : RingBuffer<E> {
    override val capacity: Int = findNextPositivePowerOfTwo(requestedCapacity)
    private val mask = capacity - 1

    @Suppress("UNCHECKED_CAST")
    private val buffer = arrayOfNulls<Any>(capacity) as Array<E?>

    // Producer sequence (only updated by producer thread)
    private val producerSequence = PaddedAtomicLong(0L)

    // Cached consumer sequence visible to producer to avoid cross-core false sharing
    @CacheLinePad private var cachedConsumerSequence: Long = 0L

    // Consumer sequence (only updated by consumer thread)
    private val consumerSequence = PaddedAtomicLong(0L)

    // Cached producer sequence visible to consumer
    @CacheLinePad private var cachedProducerSequence: Long = 0L

    override val size: Int
        get() = (producerSequence.get() - consumerSequence.get()).toInt().coerceAtLeast(0)

    override val isEmpty: Boolean
        get() = producerSequence.get() == consumerSequence.get()

    override val isFull: Boolean
        get() = size >= capacity

    @HotPath("Zero-lock, single-barrier offer")
    override fun offer(element: E): Boolean {
        val currentHead = producerSequence.get()
        val wrapBoundary = currentHead - capacity

        if (wrapBoundary >= cachedConsumerSequence) {
            cachedConsumerSequence = consumerSequence.get()
            if (wrapBoundary >= cachedConsumerSequence) {
                return false // Buffer full
            }
        }

        val index = (currentHead and mask.toLong()).toInt()
        buffer[index] = element
        // Store-Store barrier / Release write
        producerSequence.lazySet(currentHead + 1)
        return true
    }

    @HotPath("Zero-lock, single-barrier poll")
    override fun poll(): E? {
        val currentTail = consumerSequence.get()
        if (currentTail >= cachedProducerSequence) {
            cachedProducerSequence = producerSequence.get()
            if (currentTail >= cachedProducerSequence) {
                return null // Buffer empty
            }
        }

        val index = (currentTail and mask.toLong()).toInt()
        val element = buffer[index]
        buffer[index] = null // Allow GC recycling if necessary
        // Release read
        consumerSequence.lazySet(currentTail + 1)
        return element
    }

    @HotPath("Batch drain to minimize memory barrier overhead")
    override fun drain(consumer: (E) -> Unit, maxBatchSize: Int): Int {
        val currentTail = consumerSequence.get()
        if (currentTail >= cachedProducerSequence) {
            cachedProducerSequence = producerSequence.get()
            if (currentTail >= cachedProducerSequence) return 0
        }

        val available = (cachedProducerSequence - currentTail).toInt()
        val toDrain = minOf(available, maxBatchSize)

        for (i in 0 until toDrain) {
            val index = ((currentTail + i) and mask.toLong()).toInt()
            val item = buffer[index]
            buffer[index] = null
            if (item != null) {
                consumer(item)
            }
        }

        consumerSequence.lazySet(currentTail + toDrain)
        return toDrain
    }

    override fun clear() {
        while (poll() != null) {}
    }

    companion object {
        private fun findNextPositivePowerOfTwo(value: Int): Int {
            return 1 shl (32 - Integer.numberOfLeadingZeros(value.coerceAtLeast(1) - 1))
        }
    }
}
