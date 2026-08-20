package com.example.ultraengine.networking

import com.example.ultraengine.core.HotPath
import com.example.ultraengine.core.IoPath
import com.example.ultraengine.memory.DirectByteBufferPool
import com.example.ultraengine.serialization.MessageHeader
import com.example.ultraengine.serialization.ZeroCopyProtocol
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * High-performance non-blocking Java NIO TCP Channel Handler.
 * Operates on pooled direct ByteBuffers to eliminate heap allocations during network ingress/egress.
 */
class NioFastChannel(
    private val bufferPool: DirectByteBufferPool = DirectByteBufferPool(64, 4096)
) {
    private var serverChannel: ServerSocketChannel? = null
    private val clientChannels = mutableListOf<SocketChannel>()

    @IoPath("Start non-blocking server on specified port")
    fun bind(port: Int) {
        serverChannel = ServerSocketChannel.open().apply {
            configureBlocking(false)
            bind(InetSocketAddress(port))
        }
    }

    @HotPath("Poll network for incoming frames without blocking")
    fun pollIncoming(onMessage: (MessageHeader, ByteBuffer) -> Unit): Int {
        var messagesReceived = 0
        val sChannel = serverChannel ?: return 0

        // Accept new connections
        val newClient = sChannel.accept()
        if (newClient != null) {
            newClient.configureBlocking(false)
            clientChannels.add(newClient)
        }

        // Poll existing client connections
        val iterator = clientChannels.iterator()
        while (iterator.hasNext()) {
            val client = iterator.next()
            if (!client.isOpen) {
                iterator.remove()
                continue
            }

            val readBuf = bufferPool.acquire()
            try {
                val bytesRead = client.read(readBuf)
                if (bytesRead > 0) {
                    readBuf.flip()
                    val header = ZeroCopyProtocol.decodeHeader(readBuf)
                    if (header != null) {
                        onMessage(header, readBuf)
                        messagesReceived++
                    }
                } else if (bytesRead == -1) {
                    client.close()
                    iterator.remove()
                }
            } catch (_: Throwable) {
                client.close()
                iterator.remove()
            } finally {
                bufferPool.release(readBuf)
            }
        }
        return messagesReceived
    }

    @IoPath("Close all network channels")
    fun close() {
        serverChannel?.close()
        for (client in clientChannels) {
            try { client.close() } catch (_: Throwable) {}
        }
        clientChannels.clear()
    }
}
