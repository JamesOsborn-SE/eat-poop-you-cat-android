package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.app_icon_background
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.md_theme_light_drawing_background
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.md_theme_light_drawing_pen
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.Line
import dev.develsinthedetails.eatpoopyoucat.data.models.Resolution
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviewData
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.app_name
import eatpoopyoucat.shared.generated.resources.ic_launcher_foreground
import eatpoopyoucat.shared.generated.resources.is_available_on
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.time.Instant

data class EstimatedTextLayout(
    val lines: List<String>,
    val totalHeightPx: Float,
    val lineSpacingPx: Float
)

class ImageExport(
    private val entries: List<Entry>,
    appIcon: ImageBitmap,
    private val appName: String,
    private val bottomBlurb: String,
    private val textMeasurer: TextMeasurer
) {
    private val penColor = md_theme_light_drawing_pen
    private val eraseColor = md_theme_light_drawing_background
    private val density = Density(density = 1f, fontScale = 1f)
    private val layoutDirection = LayoutDirection.Ltr
    private val scaledAppIcon = scaleBitmap(appIcon, targetWidth = 100, targetHeight = 100)

    fun makeBitmap(): ImageBitmap {
        val bitmaps = mutableListOf<ImageBitmap>()
        bitmaps.add(headerBitmap())

        if (entries.first().createdAt != null) {
            val dateText = entries.first().createdAt.localDateTimestamp()
            bitmaps.add(sentenceBitmap(dateText, center = true, isBubble = false))
        }

        entries.forEach { entry ->
            if (entry.type == EntryType.Sentence) {
                bitmaps.add(sentenceBitmap(entry.sentence!!, isBubble = true))
            }
            if (entry.type == EntryType.Drawing) {
                bitmaps.add(drawingBitmap(entry.drawing!!))
            }
            if (entry.createdAt != null || entry.localPlayerName != null) {
                bitmaps.add(
                    metadataBitmap(
                        entry.createdAt,
                        entry.localPlayerName,
                        entry.type == EntryType.Drawing
                    )
                )
            }
        }
        bitmaps.add(footerBitmap())

        val totalHeight = bitmaps.sumOf { it.height }
        val finalBitmap = ImageBitmap(WIDTH, totalHeight)
        val canvas = Canvas(finalBitmap)

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            canvas,
            Size(WIDTH.toFloat(), totalHeight.toFloat())
        ) {
            drawRect(color = eraseColor, size = size)

            var currentY = 0f
            bitmaps.forEach { bmp ->
                drawImage(image = bmp, topLeft = Offset(0f, currentY))
                currentY += bmp.height
            }
        }

        return finalBitmap
    }

    private fun estimateLayout(
        text: String,
        fontSizePx: Float,
        maxWidthPx: Float,
        maxLines: Int = 5
    ): EstimatedTextLayout {
        val monoCharWidthPx = fontSizePx * 0.6f
        val lineHeightPx = fontSizePx * 1.2f
        val maxCharsPerLine = (maxWidthPx / monoCharWidthPx).toInt()

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            if ((currentLine.length + word.length + 1) <= maxCharsPerLine) {
                currentLine += if (currentLine.isEmpty()) word else " $word"
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        val truncatedLines = lines.take(maxLines)

        return EstimatedTextLayout(
            lines = truncatedLines,
            totalHeightPx = truncatedLines.size * lineHeightPx,
            lineSpacingPx = lineHeightPx
        )
    }

    private fun DrawScope.drawManualLines(
        layout: EstimatedTextLayout,
        style: TextStyle,
        maxWidth: Float
    ) {
        var currentY = 0f
        layout.lines.forEach { line ->
            val paintedLine = textMeasurer.measure(
                text = AnnotatedString(line),
                style = style,
                density = this@ImageExport.density
            )

            val xOffset = when (style.textAlign) {
                TextAlign.Center -> (maxWidth - paintedLine.size.width) / 2f
                TextAlign.Right, TextAlign.End -> maxWidth - paintedLine.size.width.toFloat()
                else -> 0f
            }

            drawText(paintedLine, topLeft = Offset(xOffset, currentY))

            currentY += layout.lineSpacingPx
        }
    }

    private fun metadataBitmap(
        createdAt: Instant?,
        playerName: String?,
        isRight: Boolean = false
    ): ImageBitmap {
        val dateText = createdAt?.localTimestamp() ?: ""
        val text = "${playerName.valueOrEmpty()} $dateText"

        val style = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontSize = 20.sp,
            textAlign = if (isRight) TextAlign.Right else TextAlign.Left
        )
        val layout = estimateLayout(text, style.fontSize.value, WIDTH - PADDING * 4f, 1)
        val textHeight = layout.totalHeightPx + PADDING * 2
        val tmpBitmap = ImageBitmap(WIDTH, textHeight.toInt())

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), textHeight)
        ) {
            translate(left = PADDING.toFloat() * 2, top = 0f) {
                drawManualLines(layout, style, WIDTH - PADDING * 4f)
            }
        }
        return tmpBitmap
    }

    private fun headerBitmap(): ImageBitmap {
        val style = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
            fontSize = 45.sp,
            fontWeight = FontWeight.Bold,
        )
        val layout =
            estimateLayout(
                appName,
                style.fontSize.value,
                WIDTH - PADDING * 4f - scaledAppIcon.width,
                5
            )

        val textHeight = layout.totalHeightPx
        val height = max(scaledAppIcon.height + PADDING * 2f, textHeight)
        val textOffset = (height - textHeight) / 2f

        val tmpBitmap = ImageBitmap(WIDTH, height.toInt())
        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), height)
        ) {
            drawRect(color = app_icon_background, size = size)
            drawImage(image = scaledAppIcon, topLeft = Offset(PADDING.toFloat(), PADDING.toFloat()))

            translate(left = PADDING.toFloat() * 3 + scaledAppIcon.width, top = textOffset) {
                drawManualLines(layout, style, WIDTH - PADDING * 4f - scaledAppIcon.width)
            }
        }
        return tmpBitmap
    }

    private fun drawingBitmap(drawing: ByteArray): ImageBitmap {
        val bubbleColor = Color(0xFFE1F5FE)
        val tailHeight = 20f
        val innerSize = WIDTH - PADDING * 8

        val rectHeight = innerSize.toFloat() + PADDING * 4f
        val totalHeight = (rectHeight + tailHeight + PADDING * 2).toInt()
        val tmpBitmap = ImageBitmap(WIDTH, totalHeight)

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), totalHeight.toFloat())
        ) {
            val offset = Offset(PADDING.toFloat() * 2, PADDING.toFloat())
            val rectSize = Size(WIDTH.toFloat() - PADDING * 4, rectHeight)

            drawRoundRect(
                color = bubbleColor,
                topLeft = offset,
                size = rectSize,
                cornerRadius = CornerRadius(40f, 40f)
            )
            val tailXOffset = 150f
            val tail = Path().apply {
                moveTo(
                    offset.x + rectSize.width - 50f - tailXOffset,
                    offset.y + rectSize.height - 10f
                )
                lineTo(
                    offset.x + rectSize.width - 20f - tailXOffset,
                    offset.y + rectSize.height + tailHeight
                )
                lineTo(offset.x + rectSize.width - 80f - tailXOffset, offset.y + rectSize.height)
                close()
            }
            drawPath(tail, bubbleColor)

            translate(left = offset.x + PADDING * 2, top = offset.y + PADDING * 2) {
                val lines: MutableList<Line> =
                    Json.decodeFromString(Gzip.decompressToString(drawing))
                lines.forEach { line ->
                    val strokeWidth = DrawViewModel.scaleStroke(
                        Resolution(innerSize, innerSize),
                        line.resolution,
                        if (line.properties.eraseMode) ERASE_STROKE else PEN_STROKE
                    )

                    val color = if (line.properties.eraseMode) bubbleColor else penColor

                    val newPath = DrawViewModel.scalePath(
                        line.toPath(),
                        Resolution(innerSize, innerSize),
                        line.resolution
                    )

                    drawPath(
                        path = newPath,
                        color = color,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
        return tmpBitmap
    }

    private fun sentenceBitmap(
        sentence: String,
        center: Boolean = false,
        isBubble: Boolean
    ): ImageBitmap {
        val bubbleColor = Color(0xFFF0F2F5)
        val tailHeight = if (isBubble) 20f else 0f

        val style = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
            fontSize = 28.sp,
            textAlign = if (center) TextAlign.Center else TextAlign.Start
        )
        val layout = estimateLayout(sentence, style.fontSize.value, WIDTH - PADDING * 8f, 5)

        val rectHeight = layout.totalHeightPx + PADDING * 4f
        val totalHeight = (rectHeight + tailHeight + PADDING * 2).toInt()
        val tmpBitmap = ImageBitmap(WIDTH, totalHeight)

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), totalHeight.toFloat())
        ) {
            val offset = Offset(PADDING.toFloat() * 2, PADDING.toFloat())
            val rectSize = Size(WIDTH.toFloat() - PADDING * 4, rectHeight)

            drawRoundRect(
                color = if (isBubble) bubbleColor else Color.White,
                topLeft = offset,
                size = rectSize,
                cornerRadius = CornerRadius(40f, 40f)
            )
            if (isBubble) {
                val tail = Path().apply {
                    moveTo(offset.x + 50f, offset.y + rectSize.height - 10f)
                    lineTo(offset.x + 20f, offset.y + rectSize.height + tailHeight)
                    lineTo(offset.x + 80f, offset.y + rectSize.height)
                    close()
                }
                drawPath(tail, bubbleColor)
            }
            translate(left = offset.x + PADDING * 2, top = offset.y + PADDING * 2) {
                drawManualLines(layout, style, WIDTH - PADDING * 8f)
            }
        }
        return tmpBitmap
    }

    private fun footerBitmap(): ImageBitmap {
        val style = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        val layout = estimateLayout(bottomBlurb, style.fontSize.value, WIDTH - PADDING * 4f, 5)

        val textHeight = layout.totalHeightPx + PADDING * 2
        val height = textHeight + PADDING * 2
        val tmpBitmap = ImageBitmap(WIDTH, height.toInt())
        val textOffset = (height - layout.totalHeightPx) / 2f

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), height)
        ) {
            drawRect(color = app_icon_background, size = size)
            translate(left = PADDING.toFloat(), top = textOffset) {
                drawManualLines(layout, style, WIDTH - PADDING * 4f)
            }
        }
        return tmpBitmap
    }

    private fun scaleBitmap(
        original: ImageBitmap,
        targetWidth: Int,
        targetHeight: Int
    ): ImageBitmap {
        val scaledBitmap = ImageBitmap(targetWidth, targetHeight)

        CanvasDrawScope().draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = Canvas(scaledBitmap),
            size = Size(targetWidth.toFloat(), targetHeight.toFloat())
        ) {
            drawImage(
                image = original,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(targetWidth, targetHeight)
            )
        }
        return scaledBitmap
    }

    companion object {
        const val WIDTH = 640
        const val PEN_STROKE = 12f
        const val ERASE_STROKE = 48f
        const val PADDING = 12
    }
}

@Preview
@Composable
fun SharePreview() {
    val appName = stringResource(Res.string.app_name)
    val appIcon = rememberBitmapFromResource(Res.drawable.ic_launcher_foreground)
    val isAvailableOnFDroidAndGooglePlay = stringResource(Res.string.is_available_on, appName)
    val ie = ImageExport(
        PreviewData.entries, appIcon, appName, isAvailableOnFDroidAndGooglePlay,
        textMeasurer = rememberTextMeasurer()
    )
    Image(bitmap = ie.makeBitmap(), null)
}