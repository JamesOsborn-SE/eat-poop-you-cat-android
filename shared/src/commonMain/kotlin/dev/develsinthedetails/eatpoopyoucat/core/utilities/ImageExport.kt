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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
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
import eatpoopyoucat.shared.generated.resources.is_available_on_f_droid_and_google_play
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import kotlin.math.max
import kotlin.time.Instant

class ImageExport(
    private val entries: List<Entry>,
    private val appIcon: ImageBitmap,
    private val textMeasurer: TextMeasurer
) {
    private val penColor = md_theme_light_drawing_pen
    private val eraseColor = md_theme_light_drawing_background
    private val density = Density(1f)
    private val layoutDirection = LayoutDirection.Ltr

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var appName: String
    private lateinit var bottomBlurb: String


    init {
        scope.launch {
            appName = getString(Res.string.app_name)
            bottomBlurb = getString(Res.string.is_available_on_f_droid_and_google_play, appName)
        }
    }

    fun makeBitmap(): ImageBitmap {
        val bitmaps = mutableListOf<ImageBitmap>()
        bitmaps.add(headerBitmap())

        if (entries.first().createdAt != null) {
            val dateText = entries.first().createdAt.localDateTimestamp()
            bitmaps.add(sentenceBitmap(dateText, center = true))
        }

        entries.forEach {
            if (it.type == EntryType.Sentence) {
                bitmaps.add(sentenceBitmap(it.sentence!!))
            }
            if (it.type == EntryType.Drawing) {
                bitmaps.add(drawingBitmap(it.drawing!!))
            }
            if (it.createdAt != null || it.localPlayerName != null) {
                bitmaps.add(metadataBitmap(it.createdAt, it.localPlayerName))
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

    private fun metadataBitmap(createdAt: Instant?, playerName: String?): ImageBitmap {
        val dateText = createdAt?.localTimestamp() ?: ""
        val text = "^ ${playerName.valueOrEmpty()} $dateText"

        val textLayout = textMeasurer.measure(
            text = AnnotatedString(text),
            style = TextStyle(color = Color.Gray, fontSize = 20.sp, textAlign = TextAlign.Right),
            constraints = Constraints(maxWidth = WIDTH - PADDING * 4)
        )

        val textHeight = textLayout.size.height + PADDING * 2
        val tmpBitmap = ImageBitmap(WIDTH, textHeight)

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), textHeight.toFloat())
        ) {
            translate(left = PADDING.toFloat() * 2, top = 0f) {
                drawText(textLayout)
            }
        }
        return tmpBitmap
    }

    private fun headerBitmap(): ImageBitmap {
        val textLayout = textMeasurer.measure(
            text = AnnotatedString(appName),
            style = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            constraints = Constraints(maxWidth = WIDTH - PADDING * 4 - appIcon.width),
            maxLines = 5
        )

        val textHeight = textLayout.size.height + PADDING
        val height = max(appIcon.height + PADDING * 2, textHeight)
        val textOffset = if (appName.count() > 30) 0f else (height / 2f) - PADDING * 4

        val tmpBitmap = ImageBitmap(WIDTH, height)
        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), height.toFloat())
        ) {
            drawRect(color = app_icon_background, size = size)
            drawImage(image = appIcon, topLeft = Offset(PADDING.toFloat(), PADDING.toFloat()))

            translate(left = PADDING.toFloat() * 3 + appIcon.width, top = textOffset) {
                drawText(textLayout)
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

            val tail = Path().apply {
                moveTo(offset.x + rectSize.width - 50f, offset.y + rectSize.height - 10f)
                lineTo(offset.x + rectSize.width - 20f, offset.y + rectSize.height + tailHeight)
                lineTo(offset.x + rectSize.width - 80f, offset.y + rectSize.height)
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

    private fun sentenceBitmap(sentence: String, center: Boolean = false): ImageBitmap {
        val bubbleColor = Color(0xFFF0F2F5)
        val tailHeight = 20f

        val textLayout = textMeasurer.measure(
            text = AnnotatedString(sentence),
            style = TextStyle(
                color = Color.Black,
                fontSize = 28.sp,
                textAlign = if (center) TextAlign.Center else TextAlign.Start
            ),
            constraints = Constraints(maxWidth = WIDTH - PADDING * 8),
            maxLines = 5
        )

        val rectHeight = textLayout.size.height + PADDING * 4f
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
            val tail = Path().apply {
                moveTo(offset.x + 50f, offset.y + rectSize.height - 10f)
                lineTo(offset.x + 20f, offset.y + rectSize.height + tailHeight)
                lineTo(offset.x + 80f, offset.y + rectSize.height)
                close()
            }
            drawPath(tail, bubbleColor)

            translate(left = offset.x + PADDING * 2, top = offset.y + PADDING * 2) {
                drawText(textLayout)
            }
        }
        return tmpBitmap
    }

    private fun footerBitmap(): ImageBitmap {
        val textLayout = textMeasurer.measure(
            text = AnnotatedString(bottomBlurb),
            style = TextStyle(
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            constraints = Constraints(maxWidth = WIDTH - PADDING * 4),
            maxLines = 5
        )

        val textHeight = textLayout.size.height + PADDING * 2
        val height = textHeight + PADDING * 2
        val tmpBitmap = ImageBitmap(WIDTH, height)

        CanvasDrawScope().draw(
            density,
            layoutDirection,
            Canvas(tmpBitmap),
            Size(WIDTH.toFloat(), height.toFloat())
        ) {
            drawRect(color = app_icon_background, size = size)
            translate(left = PADDING.toFloat(), top = PADDING.toFloat()) {
                drawText(textLayout)
            }
        }
        return tmpBitmap
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
    val appIcon = rememberBitmapFromResource(Res.drawable.ic_launcher_foreground)
    val ie = ImageExport( PreviewData.entries, appIcon, textMeasurer = rememberTextMeasurer() )
    Image(bitmap = ie.makeBitmap(), null)
}