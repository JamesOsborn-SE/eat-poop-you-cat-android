package dev.develsinthedetails.eatpoopyoucat.data.local

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlin.uuid.Uuid

class Converters {

    // --- Instant Converters ---
    @ColumnTypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @ColumnTypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    // --- Uuid Converters ---
    @ColumnTypeConverter
    fun fromUuidByteArray(bytes: ByteArray?): Uuid? {
        if (bytes == null) return null

        return Uuid.fromByteArray(bytes)
    }

    @ColumnTypeConverter
    fun uuidToByteArray(uuid: Uuid?): ByteArray? {
        return uuid?.toByteArray()
    }
}