package dev.develsinthedetails.eatpoopyoucat.data

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable

@Serializable
data class LineProperties(
    var strokeWidth: Float = 12f,
    var color: Int = Color.Black.toArgb(),
    var eraseMode: Boolean = false,
) {
    fun drawColor(): Color = if(eraseMode) Color.White else Color(color)
    fun blendMode(): BlendMode = if(eraseMode) BlendMode.SrcOver else BlendMode.SrcOver
}