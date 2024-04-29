package dev.develsinthedetails.eatpoopyoucat.utilities

import org.junit.Test
import java.util.Date
import java.util.TimeZone

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
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        assert(dateTime.localTimestamp() == "11:43:05 PM")
    }
}