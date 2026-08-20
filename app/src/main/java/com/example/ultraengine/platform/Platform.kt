package com.example.ultraengine.platform

import com.example.ultraengine.core.ColdPath

enum class PlatformType {
    LINUX,
    GENERIC_JVM,
    ANDROID
}

interface Platform {
    val platformType: PlatformType
    val availableCores: Int
    val cacheLineSizeBytes: Int
    val supportsThreadPinning: Boolean
    val supportsHighResMonotonicTimer: Boolean

    fun querySystemTopology(): String
}

object PlatformProvider : Platform {
    private val osName = System.getProperty("os.name")?.lowercase() ?: ""
    private val isAndroidRuntime = System.getProperty("java.vendor")?.contains("Android", ignoreCase = true) == true

    override val platformType: PlatformType = when {
        isAndroidRuntime -> PlatformType.ANDROID
        osName.contains("linux") -> PlatformType.LINUX
        else -> PlatformType.GENERIC_JVM
    }

    override val availableCores: Int = Runtime.getRuntime().availableProcessors()

    // Typical modern x86_64 / ARM64 cache-line size is 64 bytes
    override val cacheLineSizeBytes: Int = 64

    override val supportsThreadPinning: Boolean = platformType == PlatformType.LINUX

    override val supportsHighResMonotonicTimer: Boolean = true

    @ColdPath
    override fun querySystemTopology(): String {
        return buildString {
            appendLine("Platform: $platformType")
            appendLine("OS: $osName")
            appendLine("CPU Cores: $availableCores")
            appendLine("Cache-Line: ${cacheLineSizeBytes}B")
            appendLine("JVM: ${System.getProperty("java.version")} (${System.getProperty("java.vm.name")})")
            appendLine("GC: ${try { java.lang.management.ManagementFactory.getGarbageCollectorMXBeans().joinToString { it.name } } catch (_: Throwable) { "ART/Android" }}")
        }
    }
}
