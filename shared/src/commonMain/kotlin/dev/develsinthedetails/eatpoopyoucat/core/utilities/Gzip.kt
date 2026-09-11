package dev.develsinthedetails.eatpoopyoucat.core.utilities

import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import okio.use

class Gzip {
    companion object {
        fun compress(string: String): ByteArray {
            val buffer = Buffer()
            GzipSink(buffer).buffer().use { sink ->
                sink.writeUtf8(string)
            }
            return buffer.readByteArray()
        }
        fun decompressToString(compressed: ByteArray): String {
            val sourceBuffer = Buffer().write(compressed)
            return GzipSource(sourceBuffer).buffer().use { source ->
                source.readUtf8()
            }
        }
    }
}