package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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

@Composable
fun rememberBitmapFromResource(
    resource: DrawableResource,
    fallbackSize: Int = 1
): ImageBitmap {
    val painter = painterResource(resource)
    val density = LocalDensity.current

    return remember(painter, density) {
        val intrinsicSize = painter.intrinsicSize

        val width = if (intrinsicSize.isSpecified && intrinsicSize.width > 0) {
            intrinsicSize.width.toInt()
        } else {
            fallbackSize
        }
        val height = if (intrinsicSize.isSpecified && intrinsicSize.height > 0) {
            intrinsicSize.height.toInt()
        } else {
            fallbackSize
        }

        val imageBitmap = ImageBitmap(width, height)
        val canvas = Canvas(imageBitmap)

        CanvasDrawScope().draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
        ) {
            with(painter) {
                draw(size = this@draw.size)
            }
        }

        imageBitmap
    }
}