package dev.develsinthedetails.eatpoopyoucat.utilities

import dev.develsinthedetails.eatpoopyoucat.core.utilities.localTimestamp
import dev.develsinthedetails.eatpoopyoucat.core.utilities.valueOrEmpty
import org.junit.Test
import java.util.Date
import java.util.TimeZone
import kotlin.time.toKotlinInstant

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
        val dateTime = Date(1714286585712).toInstant().toKotlinInstant()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        assert(dateTime.localTimestamp().startsWith("11:43:05") && dateTime.localTimestamp().endsWith("PM"))
    }
}