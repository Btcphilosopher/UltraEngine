package com.example.ultraengine.events

import com.example.ultraengine.core.HotPath
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Direct event handler contract for low-latency event processing.
 */
fun interface EventHandler<in E : Event> {
    @HotPath
    fun onEvent(event: E)
}

/**
 * High-speed pre-registered EventBus.
 * Eliminates reflection and dynamic lookup overhead by using direct indexed array dispatch.
 */
class EventBus(maxEventTypes: Int = 128) {
    @Suppress("UNCHECKED_CAST")
    private val handlers = Array<CopyOnWriteArrayList<EventHandler<FastEvent>>>(maxEventTypes) {
        CopyOnWriteArrayList()
    }

    fun register(eventType: Int, handler: EventHandler<FastEvent>) {
        if (eventType in handlers.indices) {
            handlers[eventType].add(handler)
        }
    }

    fun unregister(eventType: Int, handler: EventHandler<FastEvent>) {
        if (eventType in handlers.indices) {
            handlers[eventType].remove(handler)
        }
    }

    @HotPath("Direct indexed array dispatch - zero reflection")
    fun publish(event: FastEvent) {
        val type = event.eventType
        if (type in handlers.indices) {
            val list = handlers[type]
            val size = list.size
            for (i in 0 until size) {
                list[i].onEvent(event)
            }
        }
    }
}
