package dev.develsinthedetails.eatpoopyoucat.utilities

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Gzip {
    companion object {
        @Throws(IOException::class)
        fun compress(bytes: ByteArray): ByteArray {
            val outputStream = ByteArrayOutputStream(bytes.size)
            val gos = GZIPOutputStream(outputStream)
            gos.write(bytes)
            gos.close()
            val compressed: ByteArray = outputStream.toByteArray()
            outputStream.close()
            return compressed
        }
        @Throws(IOException::class)
        fun compress(string: String): ByteArray {
            val bytes = string.toByteArray(Charsets.UTF_8)
            return compress(bytes)
        }
        @Throws(IOException::class)
        fun decompress(compressed: ByteArray): ByteArray {
            val bufferSize = 32
            val inputStream = ByteArrayInputStream(compressed)
            val gis = GZIPInputStream(inputStream, bufferSize)
            val result = gis.readBytes()
            gis.close()
            inputStream.close()
            return result
        }
        @Throws(IOException::class)
        fun decompressToString(compressed: ByteArray): String =
            decompress(compressed).decodeToString()
    }

}