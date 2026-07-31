package dev.develsinthedetails.eatpoopyoucat.utilities

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun Date?.localTimestamp(): String =
    if (this != null) DateFormat.getTimeInstance().format(this) else ""

fun Instant?.localTimestamp(): String {
    if (this == null)
        return ""
    val date = Date.from(this.toJavaInstant())
    return DateFormat.getTimeInstance().format(date)
}
fun Date.saveDateFormat(): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(this)
}
fun String?.valueOrEmpty(): String = this ?: ""