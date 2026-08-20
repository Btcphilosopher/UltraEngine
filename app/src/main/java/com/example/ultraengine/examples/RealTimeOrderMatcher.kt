package com.example.ultraengine.examples

import com.example.ultraengine.core.HotPath
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicLong

enum class Side { BUY, SELL }

data class Order(
    val orderId: Long,
    val side: Side,
    val price: Double,
    val quantity: Long,
    val timestampNanos: Long
)

data class TradeMatch(
    val matchId: Long,
    val buyOrderId: Long,
    val sellOrderId: Long,
    val price: Double,
    val quantity: Long,
    val latencyNanos: Long
)

/**
 * Microsecond-latency In-Memory Limit Order Book Matching Engine.
 * Implements Price-Time Priority (FIFO) matching.
 */
class RealTimeOrderMatcher(val symbol: String = "BTC-USD") {
    // Bids sorted descending by price
    private val bids = TreeMap<Double, MutableList<Order>>(Comparator.reverseOrder())
    // Asks sorted ascending by price
    private val asks = TreeMap<Double, MutableList<Order>>()

    private val tradeIdGen = AtomicLong(0L)
    @Volatile var totalTradesMatched: Long = 0L
    @Volatile var totalVolumeMatched: Long = 0L

    @HotPath("Submit limit order and match immediately against opposing book")
    @Synchronized
    fun submitOrder(order: Order): List<TradeMatch> {
        val matches = mutableListOf<TradeMatch>()
        var remainingQty = order.quantity

        if (order.side == Side.BUY) {
            // Match against asks
            while (remainingQty > 0 && asks.isNotEmpty()) {
                val bestAskEntry = asks.firstEntry()
                if (bestAskEntry.key > order.price) break // Price does not cross

                val askList = bestAskEntry.value
                val iterator = askList.iterator()

                while (iterator.hasNext() && remainingQty > 0) {
                    val restingAsk = iterator.next()
                    val matchQty = minOf(remainingQty, restingAsk.quantity)
                    val matchPrice = restingAsk.price
                    val matchId = tradeIdGen.incrementAndGet()

                    matches.add(
                        TradeMatch(
                            matchId = matchId,
                            buyOrderId = order.orderId,
                            sellOrderId = restingAsk.orderId,
                            price = matchPrice,
                            quantity = matchQty,
                            latencyNanos = System.nanoTime() - order.timestampNanos
                        )
                    )

                    totalTradesMatched++
                    totalVolumeMatched += matchQty
                    remainingQty -= matchQty

                    val updatedRestingQty = restingAsk.quantity - matchQty
                    if (updatedRestingQty <= 0) {
                        iterator.remove()
                    }
                }

                if (askList.isEmpty()) {
                    asks.remove(bestAskEntry.key)
                }
            }

            // Rest remaining buy quantity in bid book
            if (remainingQty > 0) {
                val restingOrder = order.copy(quantity = remainingQty)
                bids.computeIfAbsent(order.price) { mutableListOf() }.add(restingOrder)
            }
        } else {
            // SELL order: Match against bids
            while (remainingQty > 0 && bids.isNotEmpty()) {
                val bestBidEntry = bids.firstEntry()
                if (bestBidEntry.key < order.price) break

                val bidList = bestBidEntry.value
                val iterator = bidList.iterator()

                while (iterator.hasNext() && remainingQty > 0) {
                    val restingBid = iterator.next()
                    val matchQty = minOf(remainingQty, restingBid.quantity)
                    val matchPrice = restingBid.price
                    val matchId = tradeIdGen.incrementAndGet()

                    matches.add(
                        TradeMatch(
                            matchId = matchId,
                            buyOrderId = restingBid.orderId,
                            sellOrderId = order.orderId,
                            price = matchPrice,
                            quantity = matchQty,
                            latencyNanos = System.nanoTime() - order.timestampNanos
                        )
                    )

                    totalTradesMatched++
                    totalVolumeMatched += matchQty
                    remainingQty -= matchQty

                    val updatedRestingQty = restingBid.quantity - matchQty
                    if (updatedRestingQty <= 0) {
                        iterator.remove()
                    }
                }

                if (bidList.isEmpty()) {
                    bids.remove(bestBidEntry.key)
                }
            }

            if (remainingQty > 0) {
                val restingOrder = order.copy(quantity = remainingQty)
                asks.computeIfAbsent(order.price) { mutableListOf() }.add(restingOrder)
            }
        }

        return matches
    }

    fun getBestBid(): Double? = bids.firstKey()
    fun getBestAsk(): Double? = asks.firstKey()
}
