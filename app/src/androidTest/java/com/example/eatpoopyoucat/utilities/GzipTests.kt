package com.example.eatpoopyoucat.utilities

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test


class GzipTests {

    @Test
    fun compressImage(){
        val gzip = Gzip()
        val compressed = gzip.compress(testImage)
        MatcherAssert.assertThat(compressed, Matchers.equalTo(testImageGzipBytes))
    }

    @Test
    fun decompress(){
        val gzip = Gzip()
        val decompressed = gzip.decompress(testImageGzipBytes)
        MatcherAssert.assertThat(decompressed, Matchers.equalTo(testImage))
    }
}