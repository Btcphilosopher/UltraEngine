package com.example.ultraengine.events

import com.example.ultraengine.core.HotPath
import com.example.ultraengine.memory.Resettable
import com.example.ultraengine.timing.NanoClock

/**
 * Base event marker.
 */
interface Event {
    val eventId: Long
    val eventType: Int
    val timestampNanos: Long
}

/**
 * Zero-allocation, reusable unboxed event model.
 * Carries integer types, 64-bit timestamps, sequence IDs, and numeric/unboxed payloads.
 */
class FastEvent(
    override var eventId: Long = 0L,
    override var eventType: Int = 0,
    override var timestampNanos: Long = 0L,
    var sequence: Long = 0L,
    var payload1: Long = 0L,
    var payload2: Long = 0L,
    var payload3: Double = 0.0,
    var symbolCode: Int = 0, // Packed integer symbol (e.g. 4-char string packed as Int)
    var correlationId: Long = 0L
) : Event, Resettable {

    @HotPath
    override fun reset() {
        eventId = 0L
        eventType = 0
        timestampNanos = 0L
        sequence = 0L
        payload1 = 0L
        payload2 = 0L
        payload3 = 0.0
        symbolCode = 0
        correlationId = 0L
    }

    @HotPath
    fun populate(
        id: Long,
        type: Int,
        p1: Long = 0L,
        p2: Long = 0L,
        p3: Double = 0.0,
        sym: Int = 0,
        corr: Long = 0L
    ) {
        this.eventId = id
        this.eventType = type
        this.timestampNanos = NanoClock.nowNanos()
        this.payload1 = p1
        this.payload2 = p2
        this.payload3 = p3
        this.symbolCode = sym
        this.correlationId = corr
    }
}
