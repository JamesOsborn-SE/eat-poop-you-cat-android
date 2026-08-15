package dev.develsinthedetails.eatpoopyoucat.data.local

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class Converters {

    // --- Instant Converters ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    // --- Uuid Converters ---
    @TypeConverter
    fun fromUuidByteArray(bytes: ByteArray?): Uuid? {
        if (bytes == null) return null
        val buffer = ByteBuffer.wrap(bytes)
        val msb = buffer.long
        val lsb = buffer.long

        // Read the old bytes using Java UUID, then safely bridge to Kotlin Uuid
        return UUID(msb, lsb).toKotlinUuid()
    }

    @TypeConverter
    fun uuidToByteArray(uuid: Uuid?): ByteArray? {
        if (uuid == null) return null
        val buffer = ByteBuffer.allocate(16) // UUIDs are exactly 16 bytes

        // Bridge to Java UUID to extract bits exactly as they were saved before
        val javaUuid = uuid.toJavaUuid()
        buffer.putLong(javaUuid.mostSignificantBits)
        buffer.putLong(javaUuid.leastSignificantBits)

        return buffer.array()
    }

    // --- Uri Converters ---
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.toUri()
    }
}