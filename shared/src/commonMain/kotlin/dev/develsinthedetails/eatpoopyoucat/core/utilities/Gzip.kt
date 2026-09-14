package dev.develsinthedetails.eatpoopyoucat.core.utilities

import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress

class Gzip {
    companion object {
        fun compress(string: String): ByteArray {
            return string.encodeToByteArray().compress(GZIP)
        }

        fun decompressToString(compressed: ByteArray): String {
            return compressed.uncompress(GZIP).decodeToString()
        }
    }
}

