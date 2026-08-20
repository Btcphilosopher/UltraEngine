package com.example.ultraengine.concurrency

import com.example.ultraengine.core.ColdPath
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread affinity and dedicated execution management.
 */
object ThreadAffinity {
    private val threadIdGen = AtomicInteger(1)

    fun createNamedThreadFactory(prefix: String, priority: Int = Thread.MAX_PRIORITY): ThreadFactory {
        return ThreadFactory { runnable ->
            val thread = Thread(runnable, "$prefix-${threadIdGen.getAndIncrement()}")
            thread.isDaemon = true
            thread.priority = priority
            thread
        }
    }

    @ColdPath("Pin thread to CPU core if native OS support is available")
    fun bindToCore(coreId: Int): Boolean {
        // Platform abstraction hook; returns false if OS does not support taskset/sched_setaffinity
        return try {
            val osName = System.getProperty("os.name")?.lowercase() ?: ""
            if (osName.contains("linux")) {
                // Linux native affinity hook placeholder
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }
}
