package pro.fixaverse.data.assistant

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import pro.fixaverse.data.HttpClientFactory
import pro.fixaverse.data.integrationApiConfig
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * POST /v1/assistants/detect — same path as wasm camera → [EquipmentSelectionStore.Intent.DetectFromPhoto].
 *
 * Run: `MASTERDOC_INTEGRATION=1 ./gradlew :shared:jvmTest --tests DetectAssistantIntegrationTest`
 *
 * Expect ~30–240s (LLM on backend). Skipped in CI without [MASTERDOC_INTEGRATION].
 */
class DetectAssistantIntegrationTest {

    @Test
    fun detectAssistant_fromJpeg_returnsKnownAssistantName() {
        if (System.getenv("MASTERDOC_INTEGRATION") != "1") return

        val apiConfig = integrationApiConfig()
        val imageBytes = DetectTestImage.jpegBytes()

        runBlocking {
            withTimeout(320_000) {
                val api = AssistantsApi(HttpClientFactory().create(), apiConfig)
                val assistants = api.listAssistants()
                assertTrue(assistants.isNotEmpty(), "need assistants list from ${apiConfig.baseUrl}")

                assertTrue(
                    imageBytes.size in 500..2_000_000,
                    "test image size ${imageBytes.size} out of detect limits",
                )

                val detected = detectWithRetry(
                    api = api,
                    imageBytes = imageBytes,
                )
                val name = detected?.trim().orEmpty()
                assertFalse(name.isBlank(), "detect returned empty assistant name")

                val knownNames = assistants.map { it.name.trim() }.toSet()
                assertTrue(
                    knownNames.any { known ->
                        name.equals(known, ignoreCase = true) ||
                            name.contains(known, ignoreCase = true) ||
                            known.contains(name, ignoreCase = true)
                    },
                    "detected '$name' not matched to $knownNames",
                )
            }
        }
    }

    private suspend fun detectWithRetry(
        api: AssistantsApi,
        imageBytes: ByteArray,
    ): String? {
        var lastError: IllegalStateException? = null
        repeat(2) { attempt ->
            try {
                return api.detectAssistant(
                    imageBytes = imageBytes,
                    fileName = "detect-integration.jpg",
                    contentType = "image/jpeg",
                )
            } catch (error: IllegalStateException) {
                lastError = error
                val retryable = error.message?.contains("500") == true &&
                    error.message?.contains("timeout", ignoreCase = true) == true
                if (attempt == 0 && retryable) {
                    println("[fixaverse detect] retry after backend timeout")
                } else {
                    throw error
                }
            }
        }
        throw lastError ?: IllegalStateException("detect failed")
    }
}
