package dev.develsinthedetails.eatpoopyoucat.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.ui.previousgames.PreviewData
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.data.type
import dev.develsinthedetails.eatpoopyoucat.ui.theme.app_icon_background
import dev.develsinthedetails.eatpoopyoucat.ui.theme.md_theme_light_drawing_background
import dev.develsinthedetails.eatpoopyoucat.ui.theme.md_theme_light_drawing_pen
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.serialization.json.Json
import kotlin.math.max

class ImageExport(
    private val entries: List<Entry>,
    private val appIcon: Bitmap,
    private val appName: String,
    private val bottomBlurb: String,
) {

    private val penColor = md_theme_light_drawing_pen
    private val eraseColor = md_theme_light_drawing_background

    fun makeBitmap(): Bitmap {
        val bitmaps = mutableListOf<Bitmap>()
        bitmaps.add(headerBitmap())
        val background = Paint()
        background.color = eraseColor.toArgb()

        entries.forEach {
            if (it.type == EntryType.Sentence) {
                bitmaps.add(sentenceBitmap(it.sentence!!))
            }
            if (it.type == EntryType.Drawing) {
                bitmaps.add(drawingBitmap(it.drawing!!))
            }
        }
        bitmaps.add(footerBitmap())
        val height = bitmaps.sumOf { it.height }
        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), height.toFloat(), background)
        var currentY = 0
        bitmaps.forEach {
            canvas.drawBitmap(it, 0f, currentY.toFloat(), null)
            currentY += it.height
        }

        return bitmap
    }

    private fun headerBitmap(): Bitmap {
        val textPaint = TextPaint()
        textPaint.color = Color.Black.toArgb()
        textPaint.textSize = 45f
        textPaint.isFakeBoldText = true
        textPaint.isAntiAlias = true

        val headerPaint = Paint()
        headerPaint.color = app_icon_background.toArgb()
        val textLayout = Layout.Alignment.ALIGN_NORMAL
        val sl = staticLayout(
            appName,
            textPaint,
            textLayout,
            textWidth = WIDTH - padding * 4 - appIcon.width
        )
        val textHeight = sl.height + padding
        val height = max(appIcon.height + padding*2, textHeight)
        val textOffset = if(appName.count()>30) 0 else (height / 2f) - padding * 3
        val tmpBitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(tmpBitmap)
        tmpCanvas.drawRect(0f, 0f, WIDTH.toFloat(), height.toFloat(), headerPaint)
        tmpCanvas.drawBitmap(appIcon, padding.toFloat(), padding.toFloat(), null)
        tmpCanvas.save()
        tmpCanvas.translate(padding.toFloat()*3 + appIcon.width, textOffset.toFloat() )
        sl.draw(tmpCanvas)
        tmpCanvas.restore()
        return tmpBitmap
    }

    private fun drawingBitmap(
        drawing: ByteArray
    ): Bitmap {

        val tmpBitmap = Bitmap.createBitmap(WIDTH, WIDTH, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(tmpBitmap)

        val lines: MutableList<Line> = Json.decodeFromString(Gzip.decompressToString(drawing))
        lines.forEach { line ->
            val stroke = DrawViewModel.scaleStroke(
                Resolution(WIDTH, WIDTH),
                line.resolution,
                if (line.properties.eraseMode) ERASE_STROKE else PEN_STROKE
            )
            val pcolor = if (line.properties.eraseMode) eraseColor.toArgb() else penColor.toArgb()
            val penPaint = Paint()
            penPaint.color = pcolor
            penPaint.style = Paint.Style.STROKE
            penPaint.strokeCap = Paint.Cap.ROUND
            penPaint.strokeJoin = Paint.Join.ROUND
            penPaint.strokeWidth = stroke
            val newPath =
                DrawViewModel.scalePath(line.toPath(), Resolution(WIDTH, WIDTH), line.resolution)
            tmpCanvas.drawPath(newPath.asAndroidPath(), penPaint)
        }
        return tmpBitmap
    }

    private fun sentenceBitmap(
        sentence: String,
    ): Bitmap {
        val textPaint = TextPaint()
        textPaint.color = Color.Black.toArgb()
        textPaint.textSize = 28f
        textPaint.isAntiAlias = true

        val textLayout = Layout.Alignment.ALIGN_NORMAL
        val sl = staticLayout(
            sentence,
            textPaint,
            textLayout
        )

        val textHeight = sl.height + padding * 2
        val tmpBitmap = Bitmap.createBitmap(WIDTH, textHeight, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(tmpBitmap)

        tmpCanvas.save()
        tmpCanvas.translate(padding.toFloat() * 2, padding.toFloat())
        sl.draw(tmpCanvas)
        tmpCanvas.restore()
        return tmpBitmap
    }

    private fun footerBitmap(): Bitmap {
        val footerPaint = Paint()
        footerPaint.color = app_icon_background.toArgb()

        val textPaint = TextPaint()
        textPaint.color = Color.Black.toArgb()
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = true
        textPaint.isAntiAlias = true

        val textLayout = Layout.Alignment.ALIGN_CENTER
        val sl = staticLayout(
            bottomBlurb,
            textPaint,
            textLayout,
        )

        val textHeight = sl.height + padding * 2
        val height = textHeight + padding * 2
        val tmpBitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val tmpCanvas = Canvas(tmpBitmap)
        tmpCanvas.drawRect(0f, 0f, WIDTH.toFloat(), height.toFloat(), footerPaint)
        tmpCanvas.save()
        tmpCanvas.translate(padding.toFloat(), (height / 2f) - padding.toFloat())
        sl.draw(tmpCanvas)
        tmpCanvas.restore()
        return tmpBitmap
    }

    companion object {
        const val WIDTH = 640
        const val PEN_STROKE = 12f
        const val ERASE_STROKE = 48f
        const val padding = 10
        private fun staticLayout(
            text: String,
            tp: TextPaint,
            textLayout: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            textWidth: Int = WIDTH - padding * 4,
            spacingAddition: Float = 0f,
            spacingMultiplier: Float = 1f,
            includePadding: Boolean = false
        ): StaticLayout {
            val sl: StaticLayout
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val builder =
                    StaticLayout.Builder.obtain(text, 0, text.length, tp, textWidth)
                        .setAlignment(textLayout)
                        .setLineSpacing(spacingAddition, spacingMultiplier)
                        .setIncludePad(includePadding)
                        .setMaxLines(5)
                sl = builder.build()
            } else {
                @Suppress("DEPRECATION")
                sl = StaticLayout(
                    text, tp,
                    textWidth, textLayout, spacingMultiplier, spacingAddition, includePadding
                )
            }
            return sl
        }
    }
}

@Preview
@Composable
fun SharePreview() {
    val appName = stringResource(id = R.string.app_name)

    val option = BitmapFactory.Options()
    option.inPreferredConfig = Bitmap.Config.ARGB_8888
    val appIcon = getBitmapFromVectorDrawable(LocalContext.current, R.mipmap.ic_launcher_round)
    val isAvalibleOnFDroidAndGooglePlay =
        stringResource(id = R.string.is_avalible_on_f_droid_and_google_play, appName)
    val ie = ImageExport(PreviewData.entries, appIcon, appName, isAvalibleOnFDroidAndGooglePlay)
    Image(bitmap = ie.makeBitmap().asImageBitmap(), null)
}
