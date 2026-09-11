package dev.develsinthedetails.eatpoopyoucat.core.utilities

import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.Sink
import okio.buffer
import okio.use

class Gzip {
    companion object {
        fun compress(bytes: ByteArray): ByteArray {
            val buffer = Buffer()
            GzipSink(buffer).buffer().use { sink ->
                sink.write(bytes)
            }
            return buffer.readByteArray()
        }

        fun compress(string: String): ByteArray {
            val buffer = Buffer()
            GzipSink(buffer).buffer().use { sink ->
                sink.writeUtf8(string)
            }
            return buffer.readByteArray()
        }

        fun compress(string: String, sink: Sink) {
            GzipSink(sink).buffer().use { gzipSink ->
                gzipSink.writeUtf8(string)
            }
        }

        fun decompress(compressed: ByteArray): ByteArray {
            val sourceBuffer = Buffer().write(compressed)
            return GzipSource(sourceBuffer).buffer().use { source ->
                source.readByteArray()
            }
        }

        fun decompressToString(compressed: ByteArray): String {
            val sourceBuffer = Buffer().write(compressed)
            return GzipSource(sourceBuffer).buffer().use { source ->
                source.readUtf8()
            }
        }
    }
}