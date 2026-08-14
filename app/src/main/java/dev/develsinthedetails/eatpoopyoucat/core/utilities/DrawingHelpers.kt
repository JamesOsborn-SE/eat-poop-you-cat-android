package dev.develsinthedetails.eatpoopyoucat.core.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.set
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
    val bitmap: Bitmap =
        drawable?.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight) ?: createBitmap(1, 1)

    if (bitmap.height == 1) {
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
