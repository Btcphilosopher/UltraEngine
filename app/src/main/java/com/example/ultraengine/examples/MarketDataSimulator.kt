package com.example.ultraengine.examples

import com.example.ultraengine.core.HotPath
import com.example.ultraengine.events.FastEvent
import com.example.ultraengine.memory.ObjectPool
import com.example.ultraengine.timing.NanoClock
import kotlin.random.Random

data class MarketQuote(
    val symbol: String,
    val bidPrice: Double,
    val askPrice: Double,
    val bidQty: Long,
    val askQty: Long,
    val timestampNanos: Long,
    val latencyMicros: Double
)

/**
 * Real-time High-Frequency Market Data Feed & Quote Simulator.
 * Simulates microsecond-level Level 2 quotes, spread dynamics, and order book top-of-book updates.
 */
class MarketDataSimulator(
    val symbols: List<String> = listOf("AAPL", "GOOGL", "MSFT", "NVDA", "BTC-USD"),
    private val eventPool: ObjectPool<FastEvent> = ObjectPool(1024) { FastEvent() }
) {
    private val basePrices = doubleArrayOf(225.50, 180.25, 430.10, 125.80, 64200.00)
    private val spreads = doubleArrayOf(0.01, 0.02, 0.05, 0.02, 1.50)
    private var sequence = 0L

    @HotPath("Generate simulated microsecond market quote event with zero garbage")
    fun generateTick(): FastEvent {
        val symIndex = (sequence % symbols.size).toInt()
        val randomDelta = (Random.nextDouble() - 0.49) * spreads[symIndex] * 2.0
        val midPrice = (basePrices[symIndex] + randomDelta).coerceAtLeast(1.0)
        basePrices[symIndex] = midPrice

        val spread = spreads[symIndex]
        val bidPrice = midPrice - (spread / 2.0)
        val askPrice = midPrice + (spread / 2.0)
        val bidQty = (100L + Random.nextInt(50) * 10L)
        val askQty = (100L + Random.nextInt(50) * 10L)

        val event = eventPool.acquire()
        // Pack price into 64-bit long (price * 10000 fixed-point)
        val packedBidPrice = (bidPrice * 10000).toLong()
        val packedAskPrice = (askPrice * 10000).toLong()

        event.populate(
            id = ++sequence,
            type = 100, // EventType: MARKET_QUOTE
            p1 = packedBidPrice,
            p2 = packedAskPrice,
            p3 = midPrice,
            sym = symIndex,
            corr = bidQty or (askQty shl 32)
        )
        return event
    }

    @HotPath("Decode market quote from FastEvent")
    fun parseQuote(event: FastEvent): MarketQuote {
        val symName = symbols.getOrElse(event.symbolCode) { "UNKNOWN" }
        val bid = event.payload1 / 10000.0
        val ask = event.payload2 / 10000.0
        val bidQty = event.correlationId and 0xFFFFFFFFL
        val askQty = (event.correlationId ushr 32) and 0xFFFFFFFFL
        val latencyUs = (NanoClock.nowNanos() - event.timestampNanos) / 1000.0

        return MarketQuote(
            symbol = symName,
            bidPrice = bid,
            askPrice = ask,
            bidQty = bidQty,
            askQty = askQty,
            timestampNanos = event.timestampNanos,
            latencyMicros = latencyUs
        )
    }
}
