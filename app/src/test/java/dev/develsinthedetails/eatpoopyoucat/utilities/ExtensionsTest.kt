package dev.develsinthedetails.eatpoopyoucat.utilities

import org.junit.Test
import java.util.Date

class ExtensionsTest {
    @Test
    fun dontRenderNullForNullString() {
        val nullString: String? = null
        assert(nullString.valueOrEmpty() == "")
    }
    @Test
    fun playerNameIsPlayerName() {
        val nullString = "not Null"
        assert(nullString.valueOrEmpty() == "not Null")
    }
    @Test
    fun ensureHumanReadableTime() {
        val dateTime = Date(1714286585712)
        assert(dateTime.localTimestamp() == "11:43:05 PM")
    }
}