package com.example.ultraengine.storage

import com.example.ultraengine.core.HotPath
import com.example.ultraengine.core.IoPath
import com.example.ultraengine.events.FastEvent
import com.example.ultraengine.timing.NanoClock
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.CRC32

data class JournalRecordHeader(
    val magic: Short = 0x454C, // 'EL' (EventLog)
    val version: Byte = 1,
    val eventType: Int,
    val sequence: Long,
    val timestampNanos: Long,
    val payload1: Long,
    val payload2: Long,
    val payload3: Double,
    val symbolCode: Int,
    val checksum: Long
)

/**
 * Ultra-low-latency Append-Only Binary Event Log.
 * Uses sequential direct ByteBuffer writes, CRC32 data integrity verification, and preallocated journal files.
 */
class AppendOnlyEventLog(
    private val journalFile: File,
    private val preallocateSizeBytes: Long = 16 * 1024 * 1024L // 16 MB preallocation
) {
    private var fileChannel: FileChannel? = null
    private val writeBuffer: ByteBuffer = ByteBuffer.allocateDirect(64 * 1024) // 64 KB write cache
    private val writeSequence = AtomicLong(0L)
    private val crc = CRC32()

    companion object {
        const val RECORD_SIZE_BYTES = 56 // Fixed size binary record for unboxed FastEvent
    }

    init {
        openJournal()
    }

    @Synchronized
    private fun openJournal() {
        if (!journalFile.parentFile.exists()) {
            journalFile.parentFile.mkdirs()
        }
        val raf = RandomAccessFile(journalFile, "rw")
        if (raf.length() < preallocateSizeBytes) {
            raf.setLength(preallocateSizeBytes)
        }
        fileChannel = raf.channel
    }

    @HotPath("Append fast event into sequential binary log with CRC32 integrity")
    @Synchronized
    fun append(event: FastEvent): Long {
        val seq = writeSequence.incrementAndGet()
        event.sequence = seq

        if (writeBuffer.remaining() < RECORD_SIZE_BYTES) {
            flush()
        }

        val startPos = writeBuffer.position()

        // 1. Calculate CRC32 checksum of event fields
        crc.reset()
        crc.update(event.eventType)
        crc.update(seq.toInt())
        crc.update((seq ushr 32).toInt())
        crc.update(event.payload1.toInt())
        crc.update((event.payload1 ushr 32).toInt())
        crc.update(event.payload2.toInt())
        crc.update((event.payload2 ushr 32).toInt())
        val checksum = crc.value and 0xFFFFFFFFL

        // 2. Pack binary record
        writeBuffer.putShort(0x454C.toShort()) // Magic
        writeBuffer.put(1.toByte())           // Version
        writeBuffer.put(0.toByte())           // Reserved
        writeBuffer.putInt(event.eventType)
        writeBuffer.putLong(seq)
        writeBuffer.putLong(event.timestampNanos)
        writeBuffer.putLong(event.payload1)
        writeBuffer.putLong(event.payload2)
        writeBuffer.putDouble(event.payload3)
        writeBuffer.putInt(event.symbolCode)
        writeBuffer.putInt(checksum.toInt())

        return seq
    }

    @IoPath("Flush memory write buffer to kernel page cache")
    @Synchronized
    fun flush() {
        if (writeBuffer.position() > 0) {
            writeBuffer.flip()
            fileChannel?.write(writeBuffer)
            writeBuffer.clear()
        }
    }

    @IoPath("Sync data to durable physical disk")
    @Synchronized
    fun sync() {
        flush()
        fileChannel?.force(false)
    }

    @Synchronized
    fun close() {
        flush()
        fileChannel?.close()
        fileChannel = null
    }

    fun getAppendedCount(): Long = writeSequence.get()
}
