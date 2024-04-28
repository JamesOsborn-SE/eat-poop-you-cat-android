package dev.develsinthedetails.eatpoopyoucat.utilities

import java.text.DateFormat
import java.util.Date

fun Date?.localTimestamp(): String =
    if (this != null) DateFormat.getTimeInstance().format(this) else ""

fun String?.valueOrEmpty(): String = this ?: ""