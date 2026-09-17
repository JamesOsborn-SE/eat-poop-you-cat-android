package dev.develsinthedetails.eatpoopyoucat.core.utilities

import dev.develsinthedetails.eatpoopyoucat.platform
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ExtensionsTest {
    @Test
    fun `don't Render Null For NullString`() {
        val nullString: String? = null
        assertEquals(nullString.valueOrEmpty(), "")
    }

    @Test
    fun `player Name Is Player Name`() {
        val nullString = "not Null"
        assertEquals(nullString.valueOrEmpty(), "not Null")
    }

    @Test
    fun `ensure Human Readable Time`() {
        if (platform() == "WebAssembly") {
            println("Skipping test on WasmJs")
            return
        }
        val dateTime = Instant.fromEpochMilliseconds(1714286585712)
        val laTimeZone = TimeZone.of("America/Los_Angeles")

        val result = dateTime.localTimestamp(laTimeZone)

        assertTrue(
            result.contains(":43:05"),
            "Expected time to be 11:43:05 PM but was $result"
        )
    }
}