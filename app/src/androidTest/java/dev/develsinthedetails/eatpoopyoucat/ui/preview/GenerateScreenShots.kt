package dev.develsinthedetails.eatpoopyoucat.ui.preview

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.compose.draw.PreviewDawingWithSentance
import dev.develsinthedetails.eatpoopyoucat.compose.home.PreviewHomeScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.PreviewSentenceScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.PreviewSentenceScreenWithDrawing
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test


class GenerateScreenShots {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun takePreviewHomeScreenScreenShots(){
        val filename = "1"
        composeTestRule.setContent {
            AppTheme {
                PreviewHomeScreen()
            }
        }
        saveScreenshot(filename)
    }

    @Test
    fun takePreviewSentenceScreenScreenShots(){
        val filename = "2"
        composeTestRule.setContent {
            AppTheme {
                PreviewSentenceScreen()
            }
        }
        saveScreenshot(filename)
    }
    @Test
    fun takePreviewDawingWithSentanceScreenShots(){
        val filename = "3"
        composeTestRule.setContent {
            AppTheme {
                PreviewDawingWithSentance()
            }
        }
        saveScreenshot(filename)

    }
    @Test
    fun takePreviewSentenceScreenWithDrawingScreenShots(){
        val filename = "4"
        composeTestRule.setContent {
            AppTheme {
                PreviewSentenceScreenWithDrawing()
            }
        }
        saveScreenshot(filename)
    }

    private fun saveScreenshot(filename: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.mainClock.advanceTimeBy(100)
        val bitmap =  composeTestRule.onRoot().captureToImage().asAndroidBitmap()

        context.openFileOutput("$filename.png", Context.MODE_PRIVATE)
            .use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        println("Saved screenshot $filename.png")
        runBlocking {
            delay(3_000)
        }
    }
}
