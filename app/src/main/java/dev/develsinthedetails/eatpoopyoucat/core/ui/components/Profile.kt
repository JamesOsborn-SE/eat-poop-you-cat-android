package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.utilities.Profile
import dev.develsinthedetails.eatpoopyoucat.core.utilities.generateProfile
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

@Preview(showBackground = true)
@Composable
fun PolygonPreview(
    @PreviewParameter(
        PlayerIdPreviewParameterProvider::class
    ) playerId: Uuid
) {
    val generatedProfile = remember { generateProfile(playerId) }
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