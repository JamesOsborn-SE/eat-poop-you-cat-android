package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.ktor.http.Url
import korlibs.io.lang.toByteArray
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.io.encoding.Base64
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val timeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

fun Instant?.localTimestamp(localTimeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    if (this == null) return ""
    val localTime = this.toLocalDateTime(localTimeZone).time
    return localTime.format(timeFormat)
}

private val mediumDateTimeFormat = LocalDateTime.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(',')
    char(' ')
    year()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

fun Instant?.localDateTimestamp(): String {
    if (this == null) return ""
    val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.format(mediumDateTimeFormat)
}

private val saveFormat = LocalDateTime.Format {
    year()
    monthNumber()
    day()
    char('_')
    hour()
    minute()
    second()
}

// Note: Receiver changed from Java Date to Kotlin Instant
fun Instant.saveDateFormat(): String {
    val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.format(saveFormat)
}


fun String?.valueOrEmpty(): String = this ?: ""

fun String.shareEncode(): String {
    return Base64.UrlSafe.encode(this.toByteArray())
}

fun String.shareDecode(): String {
    return Base64.UrlSafe.decode(this).decodeToString()
}

fun Uuid.shareEncode(): String {
    return Base64.UrlSafe.encode(this.toByteArray())
}

fun String.shareDecodeUuid(): Uuid {
    return Uuid.fromByteArray(Base64.UrlSafe.decode(this))
}

fun String.shareDecodeUrl(): Pair<Uuid, String> {
    val url = Url(this)
    val gameId = url.parameters["game"]?.shareDecodeUuid()
    val address = url.parameters["server"]?.shareDecode()
    if (gameId != null && address != null) {
        return Pair(gameId, address)
    }
    return Pair(Uuid.NIL, "")
}

@Composable
expect fun SystemBackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun shareLink(link: String)