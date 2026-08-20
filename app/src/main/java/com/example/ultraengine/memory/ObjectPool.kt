package com.example.ultraengine.memory

import com.example.ultraengine.core.HotPath
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

interface Resettable {
    fun reset()
}

/**
 * Lock-free, pre-allocated object pool for zero-allocation recycling in hot paths.
 */
class ObjectPool<T : Any>(
    val capacity: Int = 1024,
    private val factory: () -> T
) {
    private val pool = AtomicReferenceArray<T>(capacity)
    private val head = AtomicInteger(0)

    init {
        for (i in 0 until capacity) {
            pool.set(i, factory())
        }
        head.set(capacity)
    }

    @HotPath("Acquire object from pool with zero allocation")
    fun acquire(): T {
        while (true) {
            val currentHead = head.get()
            if (currentHead <= 0) {
                return factory() // Pool exhausted fallback
            }
            if (head.compareAndSet(currentHead, currentHead - 1)) {
                val item = pool.getAndSet(currentHead - 1, null)
                if (item != null) {
                    if (item is Resettable) item.reset()
                    return item
                }
            }
        }
    }

    @HotPath("Release object back to pool")
    fun release(item: T) {
        if (item is Resettable) item.reset()

        while (true) {
            val currentHead = head.get()
            if (currentHead >= capacity) return
            if (head.compareAndSet(currentHead, currentHead + 1)) {
                pool.set(currentHead, item)
                return
            }
        }
    }
}
