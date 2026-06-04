package pro.masterdoc.app.platform

import kotlin.js.JsAny
import kotlin.js.JsName
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

@JsName("masterdocCompressDetectImage")
internal external fun masterdocCompressDetectImage(
    input: JsAny,
    onReady: (JsAny?) -> Unit,
)

internal fun handleCameraPayload(data: JsAny, onResult: (PickedImage?) -> Unit) {
    masterdocCompressDetectImage(data) { compressed ->
        val bytes = if (compressed == null) ByteArray(0) else jsPayloadToByteArray(compressed)
        deliverPickedFile(
            bytes = bytes,
            fileName = "camera.jpg",
            contentTypeRaw = "image/jpeg",
            defaultFileName = "camera.jpg",
            onResult = onResult,
        )
    }
}

internal fun jsPayloadToByteArray(data: JsAny): ByteArray = when (data) {
    is ArrayBuffer -> arrayBufferToByteArray(data)
    else -> {
        val uint8 = data.unsafeCast<Uint8Array>()
        ByteArray(uint8.length) { index -> uint8[index].toByte() }
    }
}

internal fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    val view = Int8Array(buffer)
    return ByteArray(view.length) { index -> view[index] }
}

internal fun deliverPickedFile(
    bytes: ByteArray,
    fileName: String,
    contentTypeRaw: String,
    defaultFileName: String,
    onResult: (PickedImage?) -> Unit,
) {
    if (bytes.isEmpty()) {
        onResult(null)
        return
    }
    val resolvedName = fileName.ifBlank { defaultFileName }
    println("[masterdoc detect] wasm ready ${bytes.size} bytes from $resolvedName")
    val contentType = contentTypeRaw.ifBlank { guessContentType(fileName) }
    val outName = if (contentType == "image/jpeg" || resolvedName.endsWith(".jpg", true)) {
        resolvedName
    } else {
        resolvedName.substringBeforeLast('.') + ".jpg"
    }
    val outType = if (bytes.size <= 1_800_000 && contentType.startsWith("image/")) contentType else "image/jpeg"
    onResult(
        PickedImage(
            bytes = bytes,
            fileName = outName.ifBlank { defaultFileName },
            contentType = outType,
        ),
    )
}

internal fun cameraErrorMessage(raw: String): String = when {
    raw.contains("Permission", ignoreCase = true) ||
        raw.contains("NotAllowed", ignoreCase = true) ->
        "Нет доступа к камере. Разрешите камеру в браузере или выберите станцию из списка."
    raw.contains("NotFound", ignoreCase = true) ||
        raw.contains("DevicesNotFound", ignoreCase = true) ->
        "Камера не найдена. Используйте список оборудования."
    raw.contains("NotReadable", ignoreCase = true) ->
        "Камера занята другим приложением. Закройте другие вкладки с камерой."
    raw.contains("insecure-context", ignoreCase = true) ->
        "Камера доступна только по HTTPS."
    raw.contains("mediaDevices unavailable", ignoreCase = true) ->
        "Браузер не поддерживает камеру. Используйте Chrome или Safari."
    raw.contains("not ready", ignoreCase = true) ||
        raw.contains("empty photo", ignoreCase = true) ||
        raw.contains("preview timeout", ignoreCase = true) ->
        "Не удалось получить кадр. Дождитесь превью и нажмите «Снять» ещё раз."
    else -> "Камера недоступна: $raw"
}

internal fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
    else -> "image/jpeg"
}
