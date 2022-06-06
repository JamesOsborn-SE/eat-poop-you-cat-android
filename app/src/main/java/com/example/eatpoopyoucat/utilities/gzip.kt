package com.example.eatpoopyoucat.utilities

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class gzip {
    @Throws(IOException::class)
    fun compress(string: String): ByteArray {
        val os = ByteArrayOutputStream(string.length)
        val gos = GZIPOutputStream(os)
        gos.write(string.toByteArray())
        gos.close()
        val compressed: ByteArray = os.toByteArray()
        os.close()
        return compressed
    }

    @Throws(IOException::class)
    fun decompress(compressed: ByteArray): String {
        val bufferSize = 32
        val `is` = ByteArrayInputStream(compressed)
        val gis = GZIPInputStream(`is`, bufferSize)
        val string = StringBuilder()
        val data = ByteArray(bufferSize)
        var bytesRead: Int
        while (gis.read(data).also { bytesRead = it } != -1) {
            string.append(String(data, 0, bytesRead))
        }
        gis.close()
        `is`.close()
        return string.toString()
    }

}