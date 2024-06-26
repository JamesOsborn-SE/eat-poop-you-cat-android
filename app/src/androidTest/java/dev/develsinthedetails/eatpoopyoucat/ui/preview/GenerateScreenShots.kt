package dev.develsinthedetails.eatpoopyoucat.ui.preview

import android.os.Build
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.ui.draw.PreviewDawingWithSentance
import dev.develsinthedetails.eatpoopyoucat.ui.home.PreviewHomeScreen
import dev.develsinthedetails.eatpoopyoucat.ui.sentence.PreviewSentenceScreen
import dev.develsinthedetails.eatpoopyoucat.ui.sentence.PreviewSentenceScreenWithDrawing
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.saveBitmap
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.mainClock.advanceTimeBy(1500)
        val bitmap =  composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        saveBitmap(context, bitmap, filename)
    }
}
