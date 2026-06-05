package pro.masterdoc.app.voice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.runBlocking
import space.kodio.core.AudioQuality
import space.kodio.core.Kodio

/**
 * Verifies Kodio can capture audio on desktop JVM (requires a working microphone).
 * Skipped in CI without [MASTERDOC_INTEGRATION].
 */
class KodioRecordingDesktopTest {

    @Test
    fun recordShortClip_producesNonEmptyWav() = runBlocking {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return@runBlocking
        val recording = Kodio.record(duration = 800.milliseconds, quality = AudioQuality.Default)
        val wav = encodeRecordingAsWav(recording)
        assertTrue(wav.size > 44, "expected WAV header + samples, got ${wav.size} bytes")
        assertEquals("RIFF", String(wav.copyOfRange(0, 4)))
    }
}
