package dev.develsinthedetails.eatpoopyoucat.ui.preview

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
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream


class GenerateScreenShots {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun takePreviewHomeScreenScreenShots(){
        val filename = "1"
        composeTestRule.setContent {
            AppTheme() {
                PreviewHomeScreen()
            }
        }
        saveScreenshot(filename)
        assertFileExists(filename)
    }

    @Test
    fun takePreviewSentenceScreenScreenShots(){
        val filename = "2"
        composeTestRule.setContent {
            AppTheme() {
                PreviewSentenceScreen()
            }
        }
        saveScreenshot(filename)
        assertFileExists(filename)
    }
    @Test
    fun takePreviewDawingWithSentanceScreenShots(){
        val filename = "3"
        composeTestRule.setContent {
            AppTheme() {
                PreviewDawingWithSentance()
            }
        }
        saveScreenshot(filename)
        assertFileExists(filename)
    }
    @Test
    fun takePreviewSentenceScreenWithDrawingScreenShots(){
        val filename = "4"
        composeTestRule.setContent {
            AppTheme() {
                PreviewSentenceScreenWithDrawing()
            }
        }
        saveScreenshot(filename)
        assertFileExists(filename)
    }

    private fun assertFileExists(filename: String) {
        val path = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath
        val absolueFilePath = "$path/$filename.png"
        val file = File(absolueFilePath)
        assert(file.exists())
    }


    private fun saveScreenshot(filename: String) {
        composeTestRule.mainClock.advanceTimeBy(100)
        val bitmap =  composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val path = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath
        FileOutputStream("$path/$filename.png").use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Saved screenshot to $path/$filename.png")
    }
}