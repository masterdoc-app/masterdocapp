package pro.masterdoc.data.assistant

import kotlin.test.Test
import kotlin.test.assertTrue

class DetectTestImageTest {

    @Test
    fun stubJpeg_isValidSizeForDetect() {
        val bytes = DetectTestImage.jpegBytes()
        assertTrue(bytes.size in 500..2_000_000)
        assertTrue(bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte(), "JPEG SOI")
    }
}
