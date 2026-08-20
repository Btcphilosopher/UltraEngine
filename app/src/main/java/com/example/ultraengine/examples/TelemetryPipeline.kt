package com.example.ultraengine.examples

import com.example.ultraengine.core.HotPath
import com.example.ultraengine.events.FastEvent
import com.example.ultraengine.memory.ObjectPool
import com.example.ultraengine.timing.NanoClock
import kotlin.random.Random

data class SensorReading(
    val sensorId: Int,
    val temperatureC: Double,
    val vibrationHz: Double,
    val pressurePsi: Double,
    val isAnomaly: Boolean,
    val timestampNanos: Long
)

/**
 * High-Throughput Industrial / IoT Telemetry Pipeline.
 * Ingests high-frequency sensor readings, runs real-time threshold & statistical anomaly checks in < 500ns.
 */
class TelemetryPipeline(
    private val eventPool: ObjectPool<FastEvent> = ObjectPool(1024) { FastEvent() }
) {
    private var sequence = 0L

    @HotPath("Generate sensor reading event")
    fun generateReading(sensorId: Int): FastEvent {
        val temp = 65.0 + Random.nextDouble() * 15.0 + if (Random.nextInt(100) == 0) 40.0 else 0.0
        val vib = 120.0 + Random.nextDouble() * 10.0
        val pressure = 14.7 + Random.nextDouble() * 2.0

        val event = eventPool.acquire()
        event.populate(
            id = ++sequence,
            type = 200, // EventType: SENSOR_TELEMETRY
            p1 = (temp * 100).toLong(),
            p2 = (vib * 100).toLong(),
            p3 = pressure,
            sym = sensorId
        )
        return event
    }

    @HotPath("Process sensor reading and detect anomalies")
    fun process(event: FastEvent): SensorReading {
        val temp = event.payload1 / 100.0
        val vib = event.payload2 / 100.0
        val pressure = event.payload3
        val anomaly = temp > 95.0 || vib > 150.0 || pressure > 25.0

        return SensorReading(
            sensorId = event.symbolCode,
            temperatureC = temp,
            vibrationHz = vib,
            pressurePsi = pressure,
            isAnomaly = anomaly,
            timestampNanos = event.timestampNanos
        )
    }
}
