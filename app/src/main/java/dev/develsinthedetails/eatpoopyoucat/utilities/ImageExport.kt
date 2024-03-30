package dev.develsinthedetails.eatpoopyoucat.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.Resolution
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.serialization.json.Json


class ImageExport(private val entries: List<Entry>) {

    fun getImageFile(): Bitmap {
        val (svg, documentHeight) = buildSVG()
        val bitmap: Bitmap = Bitmap.createBitmap(WIDTH, documentHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val s: SVG = SVG.getFromString(svg.toString())
        s.renderToCanvas(canvas)

//        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//        val byteArray = byteArrayOutputStream.toByteArray()
//
//        val encoded: String = Base64.encode(byteArray)
        return bitmap
    }

    private fun buildSVG(): Pair<StringBuilder, Int> {
        val svg: StringBuilder = StringBuilder()
        var documentHeight = 0
        svg.appendLine(
            "  <style\n" +
                    "     id=\"style1\">\n" +
                    "    .sentence {\n" +
                    "      font: bold 18px monospace;\n" +
                    "    }\n" +
                    "  </style>"
        )
        for (entry in entries) {
            if (entry.sentence != null) {
                svg.append(sentenceToSVG(entry.sentence, documentHeight))
                documentHeight += TEXT_HEIGHT
            } else if (entry.drawing != null) {
                svg.append(drawingToSVG(entry.drawing, documentHeight))
                documentHeight += WIDTH
            }
        }

        svg.appendLine("</svg>")
        svg.insert(0,
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<svg\n" +
                "   viewBox=\"0 0 $WIDTH $documentHeight\"\n" +
                "   version=\"1.1\"\n" +
                "   id=\"svg5423\"\n" +
                "   xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "   xmlns:svg=\"http://www.w3.org/2000/svg\">\n" +
                "  <defs\n" +
                "     id=\"defs5423\" />"
        )

        return Pair(svg, documentHeight)

    }

    private fun sentenceToSVG(sentence: String, documentHeight: Int): StringBuilder {
        val svg: StringBuilder = StringBuilder()
        svg.appendLine("<rect style=\"fill:#00ffff;fill-opacity:1;stroke:none;stroke-width:0.01125;stroke-linecap:round;stroke-dasharray:none;stroke-opacity:1\" x=\"0\" y=\"$documentHeight\" width=\"$WIDTH\" height=\"${Companion.TEXT_HEIGHT}\" />")
        svg.appendLine("<text style=\"inline-size:623.452\" x=\"10\" y=\"${25 + documentHeight}\" class=\"sentence\">$sentence</text>")
        return svg
    }

    private fun drawingToSVG(drawing: ByteArray, documentHeight: Int): StringBuilder {
        val lines: MutableList<Line> = Json.decodeFromString(Gzip.decompressToString(drawing))
        val svg: StringBuilder = StringBuilder()
        svg.append("<rect style=\"fill:#ffffff;fill-opacity:1;stroke:none;stroke-width:0.01125;stroke-linecap:round;stroke-dasharray:none;stroke-opacity:1\" x=\"0\" y=\"$documentHeight\" width=\"$WIDTH\" height=\"$WIDTH\" />")
        lines.forEach {
            val ratio = WIDTH.toFloat() / it.resolution.height.toFloat()
            val stroke = DrawViewModel.scaleStroke(
                Resolution(WIDTH, WIDTH),
                it.resolution,
                if (it.properties.eraseMode) ERASE_STROKE else PEN_STROKE
            )
            val penColor = "#000000"
            val eraseColor = "#FFFFFF"
            val lineColor = if (it.properties.eraseMode) eraseColor else penColor
            val lineStyle =
                "fill:none;fill-opacity:1;stroke:$lineColor;stroke-width:$stroke;stroke-linecap:round;stroke-dasharray:none;stroke-opacity:1"
            it.lineSegments.forEach { ls ->
                val x1 = ls.start.x * ratio
                val y1 = (ls.start.y * ratio) + documentHeight
                val x2 = ls.end.x * ratio
                val y2 = (ls.end.y * ratio) + documentHeight
                svg.appendLine("<line style=\"${lineStyle}\" x1=\"${x1}\" y1=\"${y1}\" x2=\"${x2}\" y2=\"${y2}\"/>")
            }
        }
        return svg
    }

    companion object {
        const val WIDTH = 640
        const val PEN_STROKE=12f
        const val ERASE_STROKE=48f
        const val TEXT_HEIGHT = 80
    }
}