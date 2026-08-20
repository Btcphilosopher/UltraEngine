package com.example.ultraengine.queues

import com.example.ultraengine.core.HotPath

/**
 * High-performance Ring Buffer interface.
 */
interface RingBuffer<E> {
    val capacity: Int
    val size: Int
    val isEmpty: Boolean
    val isFull: Boolean

    @HotPath
    fun offer(element: E): Boolean

    @HotPath
    fun poll(): E?

    @HotPath
    fun drain(consumer: (E) -> Unit, maxBatchSize: Int = 64): Int

    fun clear()
}
