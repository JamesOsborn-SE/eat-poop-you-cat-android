package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ColorSerializer
import dev.develsinthedetails.eatpoopyoucat.core.utilities.OffsetSerializer
import dev.develsinthedetails.eatpoopyoucat.core.utilities.PIXEL_PALETTE_2_BIT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.PIXEL_PALETTE_4_BIT
import kotlinx.serialization.Serializable
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.Uuid

@Composable
fun CustomRoundedPolygon(
    generated: Profile, modifier: Modifier = Modifier
) {
    val path = remember(generated.coordinates) {
        Path().apply {
            if (generated.coordinates.isNotEmpty()) {
                moveTo(generated.coordinates[0].x, generated.coordinates[0].y)
                for (i in 1 until generated.coordinates.size) {
                    lineTo(generated.coordinates[i].x, generated.coordinates[i].y)
                }
                close()
            }
        }
    }

    val paint = remember(generated.cornerRadius, generated.color) {
        Paint().apply {
            this.color = generated.color
            this.style = PaintingStyle.Fill
            this.pathEffect = PathEffect.cornerPathEffect(generated.cornerRadius)
            this.isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val bounds = path.getBounds()
        val scaleX = if (bounds.width > 0) size.width / bounds.width else 1f
        val scaleY = if (bounds.height > 0) size.height / bounds.height else 1f
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            translate(left = -bounds.left, top = -bounds.top) {
                drawIntoCanvas { canvas ->
                    canvas.drawPath(path, paint)
                }
            }
        }
    }
}

class PlayerIdPreviewParameterProvider : PreviewParameterProvider<Uuid> {
    override val values = sequenceOf(
        Uuid.parse("de2bc56c-ea73-4f3c-8a37-5a46fdb2d79a"),
        Uuid.parse("0e4832ed-1f47-42df-8db8-cd9860229782"),
        Uuid.parse("96a2ff33-1f47-42df-8db8-cd9860229782"),
        Uuid.parse("e7a2ff33-1f47-42df-8db8-cd9860229782"),
    )
}

@Serializable
data class Profile(
    // Apply directly to the type inside the List
    val coordinates: List<@Serializable(with = OffsetSerializer::class) Offset>,
    val cornerRadius: Float,
    @Serializable(with = ColorSerializer::class) val backgroundColor: Color,
    @Serializable(with = ColorSerializer::class) val color: Color,
)

fun generateOrganicProfile(uuid: Uuid): Profile {
    // color (3 bytes)
    // rounding (1 byte)
    // x,y (12 bytes)
    val bytes = uuid.toByteArray()
    val color = Color(
        red = bytes[0].toInt() and 0xFF,
        green = bytes[1].toInt() and 0xFF,
        blue = bytes[2].toInt() and 0xFF
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

fun generatePixelProfile4Bit(uuid: Uuid): List<String> {
    val bytes = uuid.toByteArray()
    val a = mutableListOf<String>()
    val cache: StringBuilder = StringBuilder()
    for ((i, byte) in bytes.withIndex()) {
        val intVal = byte.toInt()
        val highNibble = (intVal ushr 4) and 0x0F
        val lowNibble = intVal and 0x0F
        cache.append((65+highNibble).toChar())
        cache.append((65+lowNibble).toChar())
        if((i+1).mod(4)==0){
            a.add(cache.toString())
            cache.clear()
        }
    }
    a.addAll(a.reversed())
    return a.toList()
}

fun generatePixelProfile2Bit(uuid: Uuid): List<String> {
    val bytes = uuid.toByteArray()
    val all = mutableListOf<String>()
    val cache: StringBuilder = StringBuilder()
    for ((i, byte) in bytes.withIndex()) {
        val intVal = byte.toInt()
        val a= (intVal ushr 6) and 0x03
        val b =(intVal ushr 4) and 0x03
        val c =(intVal ushr 2) and 0x03
        val d= intVal and 0x03
        cache.append((65+a).toChar())
        cache.append((65+b).toChar())
        cache.append((65+c).toChar())
        cache.append((65+d).toChar())
        if((i+1).mod(2)==0){
            all.add(cache.toString()+cache.reversed())

            cache.clear()
        }
    }
    all.addAll(all.reversed())
    return all.toList()
}

@Preview
@Composable
fun TwoBitTest(){
    Column() {
        PixelArtImage(
            generatePixelProfile2Bit(Uuid.random()),
            PIXEL_PALETTE_2_BIT,
            Modifier.size(120.dp).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile2Bit(Uuid.random()),
            PIXEL_PALETTE_2_BIT,
            Modifier.size(120.dp).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile2Bit(Uuid.random()),
            PIXEL_PALETTE_2_BIT,
            Modifier.size(120.dp).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile2Bit(Uuid.random()),
            PIXEL_PALETTE_2_BIT,
            Modifier.size(120.dp).padding(10.dp)
        )
    }
}

@Preview
@Composable
fun FourBitTest(){
    Column() {
        PixelArtImage(
            generatePixelProfile4Bit(Uuid.random()),
            PIXEL_PALETTE_4_BIT,
            Modifier.size(120.dp).rotate(90f).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile4Bit(Uuid.random()),
            PIXEL_PALETTE_4_BIT,
            Modifier.size(120.dp).rotate(90f).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile4Bit(Uuid.random()),
            PIXEL_PALETTE_4_BIT,
            Modifier.size(120.dp).rotate(90f).padding(10.dp)
        )
        HorizontalDivider()
        PixelArtImage(
            generatePixelProfile4Bit(Uuid.random()),
            PIXEL_PALETTE_4_BIT,
            Modifier.size(120.dp).rotate(90f).padding(10.dp)
        )

    }
}

@Composable
fun PixelArtImage(
    sprite: List<String>,
    palette: Map<Char, Color>,
    modifier: Modifier = Modifier
) {
    val width = sprite.firstOrNull()?.length ?: 0
    val height = sprite.size

    val bitmap = remember(sprite, palette) {
        val androidBitmap = createBitmap(width, height)

        sprite.forEachIndexed { y, row ->
            row.forEachIndexed { x, char ->
                val color = palette[char] ?: Color.Transparent
                androidBitmap[x, y] = color.toArgb()
            }
        }
        androidBitmap.asImageBitmap()
    }

    Image(
        bitmap = bitmap,
        contentDescription = "Visualization of UUID",
        filterQuality = FilterQuality.None,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PolygonPreview(
    @PreviewParameter(
        PlayerIdPreviewParameterProvider::class
    ) playerId: Uuid
) {
    val generatedProfile = remember { generateOrganicProfile(playerId) }
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(generatedProfile.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CustomRoundedPolygon(
            generated = generatedProfile, modifier = Modifier.fillMaxSize()
        )
    }
}