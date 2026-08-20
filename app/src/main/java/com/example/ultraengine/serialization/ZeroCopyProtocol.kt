package com.example.ultraengine.serialization

import com.example.ultraengine.core.HotPath
import java.nio.ByteBuffer
import java.util.zip.CRC32

object ProtocolVersion {
    const val MAGIC_BYTE_1: Byte = 0x55 // 'U'
    const val MAGIC_BYTE_2: Byte = 0x4C // 'L'
    const val CURRENT_VERSION: Byte = 1
    const val HEADER_SIZE_BYTES: Int = 36 // 2 + 1 + 1 + 8 + 8 + 8 + 4 + 4
}

data class MessageHeader(
    val magic1: Byte = ProtocolVersion.MAGIC_BYTE_1,
    val magic2: Byte = ProtocolVersion.MAGIC_BYTE_2,
    val version: Byte = ProtocolVersion.CURRENT_VERSION,
    val messageType: Byte,
    val sequence: Long,
    val timestampNanos: Long,
    val correlationId: Long,
    val payloadLength: Int,
    val checksum: Long
)

class MessageFrame(
    var header: MessageHeader? = null,
    val payloadBuffer: ByteBuffer = ByteBuffer.allocateDirect(1024)
)

/**
 * High-performance Zero-Copy Binary Encoder and Decoder.
 * Operates directly on ByteBuffers with zero object allocation.
 */
object ZeroCopyProtocol {
    private val crcThreadLocal = ThreadLocal.withInitial { CRC32() }

    @HotPath("Encode binary message frame directly into ByteBuffer")
    fun encode(
        target: ByteBuffer,
        messageType: Byte,
        sequence: Long,
        timestampNanos: Long,
        correlationId: Long,
        payloadSource: ByteBuffer
    ): Int {
        val startPos = target.position()
        val payloadLen = payloadSource.remaining()

        // 1. Write Header Prefix
        target.put(ProtocolVersion.MAGIC_BYTE_1)
        target.put(ProtocolVersion.MAGIC_BYTE_2)
        target.put(ProtocolVersion.CURRENT_VERSION)
        target.put(messageType)
        target.putLong(sequence)
        target.putLong(timestampNanos)
        target.putLong(correlationId)
        target.putInt(payloadLen)

        // 2. Compute CRC32
        val crc = crcThreadLocal.get()
        crc.reset()
        val payloadStart = payloadSource.position()
        for (i in 0 until payloadLen) {
            crc.update(payloadSource.get(payloadStart + i).toInt())
        }
        val checksum = crc.value and 0xFFFFFFFFL
        target.putInt(checksum.toInt())

        // 3. Write Payload
        target.put(payloadSource)

        return target.position() - startPos
    }

    @HotPath("Decode binary header and payload directly from ByteBuffer")
    fun decodeHeader(source: ByteBuffer): MessageHeader? {
        if (source.remaining() < ProtocolVersion.HEADER_SIZE_BYTES) return null

        val magic1 = source.get()
        val magic2 = source.get()
        if (magic1 != ProtocolVersion.MAGIC_BYTE_1 || magic2 != ProtocolVersion.MAGIC_BYTE_2) {
            return null // Invalid magic bytes
        }

        val version = source.get()
        val msgType = source.get()
        val seq = source.getLong()
        val ts = source.getLong()
        val corr = source.getLong()
        val payloadLen = source.getInt()
        val checksum = source.getInt().toLong() and 0xFFFFFFFFL

        return MessageHeader(
            magic1 = magic1,
            magic2 = magic2,
            version = version,
            messageType = msgType,
            sequence = seq,
            timestampNanos = ts,
            correlationId = corr,
            payloadLength = payloadLen,
            checksum = checksum
        )
    }

    @HotPath("Verify CRC32 checksum for payload slice")
    fun verifyChecksum(payload: ByteBuffer, expectedChecksum: Long): Boolean {
        val crc = crcThreadLocal.get()
        crc.reset()
        val start = payload.position()
        val len = payload.remaining()
        for (i in 0 until len) {
            crc.update(payload.get(start + i).toInt())
        }
        return (crc.value and 0xFFFFFFFFL) == expectedChecksum
    }
}
