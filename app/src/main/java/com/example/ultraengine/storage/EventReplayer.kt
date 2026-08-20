package com.example.ultraengine.storage

import com.example.ultraengine.core.ColdPath
import com.example.ultraengine.core.HotPath
import com.example.ultraengine.events.FastEvent
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.zip.CRC32

data class ReplayStats(
    val totalRecordsReplayed: Long,
    val corruptedRecordsCount: Long,
    val elapsedNanos: Long,
    val eventsPerSecond: Long
)

/**
 * Deterministic Event Replay Engine.
 * Reads sequential binary records from AppendOnlyEventLog, verifies CRC32 integrity,
 * and replays them into downstream handlers or state machines identically.
 */
class EventReplayer(private val journalFile: File) {
    private val readBuffer = ByteBuffer.allocateDirect(128 * 1024)
    private val crc = CRC32()

    @ColdPath("Replay all journaled events with checksum verification")
    fun replay(onEvent: (FastEvent) -> Unit): ReplayStats {
        if (!journalFile.exists() || journalFile.length() < AppendOnlyEventLog.RECORD_SIZE_BYTES) {
            return ReplayStats(0, 0, 0, 0)
        }

        val startNs = System.nanoTime()
        var recordsReplayed = 0L
        var corruptedCount = 0L
        val eventObj = FastEvent()

        RandomAccessFile(journalFile, "r").use { raf ->
            val channel = raf.channel
            readBuffer.clear()

            while (channel.read(readBuffer) > 0) {
                readBuffer.flip()

                while (readBuffer.remaining() >= AppendOnlyEventLog.RECORD_SIZE_BYTES) {
                    val magic = readBuffer.getShort()
                    if (magic != 0x454C.toShort()) {
                        corruptedCount++
                        break
                    }
                    val version = readBuffer.get()
                    val reserved = readBuffer.get()
                    val eventType = readBuffer.getInt()
                    val sequence = readBuffer.getLong()
                    val timestampNanos = readBuffer.getLong()
                    val payload1 = readBuffer.getLong()
                    val payload2 = readBuffer.getLong()
                    val payload3 = readBuffer.getDouble()
                    val symbolCode = readBuffer.getInt()
                    val checksum = readBuffer.getInt().toLong() and 0xFFFFFFFFL

                    // Checksum verification
                    crc.reset()
                    crc.update(eventType)
                    crc.update(sequence.toInt())
                    crc.update((sequence ushr 32).toInt())
                    crc.update(payload1.toInt())
                    crc.update((payload1 ushr 32).toInt())
                    crc.update(payload2.toInt())
                    crc.update((payload2 ushr 32).toInt())
                    val computedCrc = crc.value and 0xFFFFFFFFL

                    if (computedCrc != checksum) {
                        corruptedCount++
                    } else {
                        eventObj.populate(
                            id = sequence,
                            type = eventType,
                            p1 = payload1,
                            p2 = payload2,
                            p3 = payload3,
                            sym = symbolCode
                        )
                        eventObj.sequence = sequence
                        eventObj.timestampNanos = timestampNanos
                        onEvent(eventObj)
                        recordsReplayed++
                    }
                }
                readBuffer.compact()
            }
        }

        val elapsedNs = System.nanoTime() - startNs
        val eventsPerSec = if (elapsedNs > 0) ((recordsReplayed * 1_000_000_000.0) / elapsedNs).toLong() else 0L

        return ReplayStats(
            totalRecordsReplayed = recordsReplayed,
            corruptedRecordsCount = corruptedCount,
            elapsedNanos = elapsedNs,
            eventsPerSecond = eventsPerSec
        )
    }
}
