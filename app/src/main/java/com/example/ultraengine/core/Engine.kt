package com.example.ultraengine.core

import com.example.ultraengine.concurrency.WaitStrategy
import com.example.ultraengine.concurrency.YieldingWaitStrategy
import com.example.ultraengine.scheduling.BackpressureStrategy

/**
 * Core engine configuration specifying concurrency, queue sizing, and wait strategies.
 */
data class EngineConfig(
    val workerThreads: Int = Runtime.getRuntime().availableProcessors().coerceIn(1, 8),
    val ringBufferCapacity: Int = 1 shl 16, // 65536 slots (power of 2)
    val directBufferPoolSize: Int = 256,
    val directBufferCapacityBytes: Int = 4096,
    val waitStrategy: WaitStrategy = YieldingWaitStrategy(),
    val backpressureStrategy: BackpressureStrategy = BackpressureStrategy.BLOCK_CONTROLLED,
    val maxBatchSize: Int = 64,
    val enableEventLogging: Boolean = true,
    val logFilePath: String? = null,
    val enableTelemetry: Boolean = true,
    val cpuAffinityEnabled: Boolean = false,
    val deterministicMode: Boolean = false,
    val randomSeed: Long = 42L
)

enum class EngineState {
    STOPPED,
    STARTING,
    RUNNING,
    PAUSED,
    SHUTTING_DOWN
}

enum class ExecutionPath {
    HOT_PATH_EVENT,
    HOT_PATH_MESSAGE,
    HOT_PATH_TIMER,
    IO_PATH_STORAGE,
    CONTROL_PATH_MANAGEMENT
}

/**
 * Non-allocating task interface for low-latency engine operations.
 */
fun interface Task {
    @HotPath("Execute task without allocating objects")
    fun execute(): Boolean
}

/**
 * Central engine interface.
 */
interface Engine {
    val state: EngineState
    val config: EngineConfig

    fun start()
    fun stop()
    fun pause()
    fun resume()
    
    @HotPath
    fun submit(task: Task): Boolean
}
