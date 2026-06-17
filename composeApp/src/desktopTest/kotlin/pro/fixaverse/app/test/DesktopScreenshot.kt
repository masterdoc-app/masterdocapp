package pro.fixaverse.app.test

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.saveGoldenScreenshot(relativePath: String) {
    val file = goldenScreenshotFile(relativePath)
    file.parentFile?.mkdirs()
    val bitmap = onRoot().captureToImage().asSkiaBitmap()
    val encoded = Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode PNG for $relativePath")
    file.writeBytes(encoded.bytes)
    assertTrue(file.length() > 1_000, "Screenshot too small: ${file.absolutePath}")
}

fun goldenScreenshotFile(relativePath: String): File {
    val moduleDir = File(System.getProperty("compose.test.root.dir") ?: ".")
    return File(moduleDir, "src/desktopTest/screenshots/golden/$relativePath")
}
