package pro.fixaverse.data.assistant

/**
 * JPEG for [DetectAssistantIntegrationTest] (stub frame similar to wasm dev camera output).
 */
internal object DetectTestImage {
    fun jpegBytes(): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/detect-integration-stub.jpg")) {
            "missing jvmTest resource detect-integration-stub.jpg"
        }.use { it.readBytes() }
}
