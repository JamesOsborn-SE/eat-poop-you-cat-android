package dev.develsinthedetails.eatpoopyoucat.core.utilities

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid


fun Date?.localTimestamp(): String =
    if (this != null) DateFormat.getTimeInstance().format(this) else ""

fun Instant?.localTimestamp(): String {
    if (this == null)
        return ""
    val date = Date.from(this.toJavaInstant())
    return DateFormat.getTimeInstance().format(date)
}

fun Instant?.toDate(): Date? {
    if (this == null) return null
    return Date.from(this.toJavaInstant())
}

fun Instant?.localDateTimestamp(): String {
    if (this == null)
        return ""
    val date = Date.from(this.toJavaInstant())
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date)
}
fun Date.saveDateFormat(): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(this)
}
fun String?.valueOrEmpty(): String = this ?: ""

fun String.shareEncode():String{
    return Base64.UrlSafe.encode(this.toByteArray())
}

fun String.shareDecode(): String {
    return Base64.UrlSafe.decode(this).decodeToString()
}

fun Uuid.shareEncode():String{
    return Base64.UrlSafe.encode(this.toByteArray())
}

fun String.shareDecodeUuid(): Uuid {
    return Uuid.fromByteArray(Base64.UrlSafe.decode(this))
}