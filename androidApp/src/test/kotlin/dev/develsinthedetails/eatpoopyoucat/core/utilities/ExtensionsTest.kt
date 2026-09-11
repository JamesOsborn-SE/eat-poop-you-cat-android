package dev.develsinthedetails.eatpoopyoucat.core.utilities

import org.junit.Test
import java.util.Date
import java.util.TimeZone
import kotlin.time.toKotlinInstant

class ExtensionsTest {
    @Test
    fun `don't Render Null For NullString`() {
        val nullString: String? = null
        assert(nullString.valueOrEmpty() == "")
    }

    @Test
    fun `player Name Is Player Name`() {
        val nullString = "not Null"
        assert(nullString.valueOrEmpty() == "not Null")
    }

    @Test
    fun `ensure Human Readable Time`() {
        val dateTime = Date(1714286585712).toInstant().toKotlinInstant()
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        assert(
            dateTime.localTimestamp().startsWith("11:43:05") && dateTime.localTimestamp()
                .endsWith("PM")
        )
    }
}