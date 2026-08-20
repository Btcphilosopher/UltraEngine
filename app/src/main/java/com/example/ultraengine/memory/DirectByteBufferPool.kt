package com.example.ultraengine.memory

import com.example.ultraengine.core.HotPath
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * High-performance, off-heap DirectByteBuffer pool to avoid GC heap pressure.
 */
class DirectByteBufferPool(
    val poolCapacity: Int = 256,
    val bufferSizeBytes: Int = 4096
) {
    private val pool = AtomicReferenceArray<ByteBuffer>(poolCapacity)
    private val head = AtomicInteger(0)

    init {
        for (i in 0 until poolCapacity) {
            pool.set(i, ByteBuffer.allocateDirect(bufferSizeBytes))
        }
        head.set(poolCapacity)
    }

    @HotPath("Lease direct ByteBuffer with zero allocation")
    fun acquire(): ByteBuffer {
        while (true) {
            val currentHead = head.get()
            if (currentHead <= 0) {
                // Fallback: direct buffer creation if pool exhausted
                return ByteBuffer.allocateDirect(bufferSizeBytes)
            }
            if (head.compareAndSet(currentHead, currentHead - 1)) {
                val buf = pool.getAndSet(currentHead - 1, null)
                if (buf != null) {
                    buf.clear()
                    return buf
                }
            }
        }
    }

    @HotPath("Release direct ByteBuffer back to pool")
    fun release(buffer: ByteBuffer) {
        if (!buffer.isDirect || buffer.capacity() != bufferSizeBytes) return
        buffer.clear()

        while (true) {
            val currentHead = head.get()
            if (currentHead >= poolCapacity) {
                // Pool full, drop buffer to let GC clean up native memory
                return
            }
            if (head.compareAndSet(currentHead, currentHead + 1)) {
                pool.set(currentHead, buffer)
                return
            }
        }
    }
}
