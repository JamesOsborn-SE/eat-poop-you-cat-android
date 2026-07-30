package dev.develsinthedetails.eatpoopyoucat.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.Uuid

fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    val drawable = ResourcesCompat.getDrawable(context.resources, drawableId, context.theme)

    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap: Bitmap = drawable?.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight) ?: createBitmap(1, 1)

    if (bitmap.height==1){
        return bitmap
    }

    val canvas = Canvas(bitmap)
    drawable!!.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

@Serializable
@SerialName("Offset")
private class OffsetSurrogate(val x: Float, val y: Float)

object OffsetSerializer : KSerializer<Offset> {
    override val descriptor = OffsetSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Offset) {
        val surrogate = OffsetSurrogate(value.x, value.y)
        encoder.encodeSerializableValue(OffsetSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): Offset {
        val surrogate = decoder.decodeSerializableValue(OffsetSurrogate.serializer())
        return Offset(surrogate.x, surrogate.y)
    }
}

@Serializable
@SerialName("Color")
private class ColorSurrogate(val value: ULong)

object ColorSerializer : KSerializer<Color> {
    override val descriptor = ColorSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Color) {
        val surrogate = ColorSurrogate(value.value)
        encoder.encodeSerializableValue(ColorSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): Color {
        val surrogate = decoder.decodeSerializableValue(ColorSurrogate.serializer())
        return Color(surrogate.value)
    }
}

@Serializable
data class Profile(
    // Apply directly to the type inside the List
    val coordinates: List<@Serializable(with = OffsetSerializer::class) Offset>,
    val cornerRadius: Float,
    @Serializable(with = ColorSerializer::class) val backgroundColor: Color,
    @Serializable(with = ColorSerializer::class) val color: Color,
)

fun generateProfile(uuid: Uuid): Profile {
    // color (3 bytes)
    // rounding (1 byte)
    // x,y (12 bytes)
    val bytes = uuid.toByteArray()
    val color = Color(
        red = bytes[0].toInt() and 0xFF, green = bytes[1].toInt() and 0xFF, blue = bytes[2].toInt() and 0xFF
    )
    val radius = (bytes[3].toInt() and 0xFF).toFloat()
    val offsets = bytes.copyOfRange(4, 16).map {
        val angle = (it.toInt() and 0xFF) / 255f * (2 * 3.141592657)
        val x = (cos(angle)).toFloat()
        val y = (sin(angle)).toFloat()
        Offset(x, y)
    }
    return Profile(
        coordinates = offsets,
        cornerRadius = radius,
        backgroundColor = if (color.luminance() <= .4) Color.LightGray else Color.DarkGray,
        color = color
    )
}
