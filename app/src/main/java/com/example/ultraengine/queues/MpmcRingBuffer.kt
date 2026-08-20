package com.example.ultraengine.queues

import com.example.ultraengine.concurrency.PaddedAtomicLong
import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicLongArray
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * Multi-Producer Multi-Consumer (MPMC) lock-free Ring Buffer.
 * Uses sequence flags per slot (Vyukov queue algorithm) to avoid locks and deadlocks.
 */
class MpmcRingBuffer<E : Any>(requestedCapacity: Int = 1024) : RingBuffer<E> {
    override val capacity: Int = findNextPositivePowerOfTwo(requestedCapacity)
    private val mask = capacity - 1

    private val buffer = AtomicReferenceArray<E?>(capacity)
    private val sequenceArray = AtomicLongArray(capacity)

    private val producerHead = PaddedAtomicLong(0L)
    private val consumerTail = PaddedAtomicLong(0L)

    init {
        for (i in 0 until capacity) {
            sequenceArray.set(i, i.toLong())
        }
    }

    override val size: Int
        get() = (producerHead.get() - consumerTail.get()).toInt().coerceAtLeast(0)

    override val isEmpty: Boolean
        get() = producerHead.get() == consumerTail.get()

    override val isFull: Boolean
        get() = size >= capacity

    @HotPath("Lock-free concurrent offer with sequence gating")
    override fun offer(element: E): Boolean {
        while (true) {
            val currentHead = producerHead.get()
            val index = (currentHead and mask.toLong()).toInt()
            val seq = sequenceArray.get(index)
            val diff = seq - currentHead

            if (diff == 0L) {
                if (producerHead.compareAndSet(currentHead, currentHead + 1)) {
                    buffer.set(index, element)
                    sequenceArray.set(index, currentHead + 1)
                    return true
                }
            } else if (diff < 0) {
                return false // Full
            }
        }
    }

    @HotPath("Lock-free concurrent poll with sequence gating")
    override fun poll(): E? {
        while (true) {
            val currentTail = consumerTail.get()
            val index = (currentTail and mask.toLong()).toInt()
            val seq = sequenceArray.get(index)
            val diff = seq - (currentTail + 1)

            if (diff == 0L) {
                if (consumerTail.compareAndSet(currentTail, currentTail + 1)) {
                    val element = buffer.get(index)
                    buffer.set(index, null)
                    sequenceArray.set(index, currentTail + mask.toLong() + 1)
                    return element
                }
            } else if (diff < 0) {
                return null // Empty
            }
        }
    }

    @HotPath
    override fun drain(consumer: (E) -> Unit, maxBatchSize: Int): Int {
        var count = 0
        while (count < maxBatchSize) {
            val item = poll() ?: break
            consumer(item)
            count++
        }
        return count
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
