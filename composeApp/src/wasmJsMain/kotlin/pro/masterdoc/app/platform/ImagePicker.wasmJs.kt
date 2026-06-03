package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.JsAny
import kotlin.js.JsName

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers {
    val onResultState = rememberUpdatedState(onResult)
    val onErrorState = rememberUpdatedState(onCameraError)
    val galleryInput = remember {
        createGalleryFileInput { onResultState.value(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            galleryInput.remove()
        }
    }

    return ImagePickerLaunchers(
        openGallery = { galleryInput.click() },
        openCamera = {
            masterdocCaptureCamera(
                onSuccess = { data -> handleCameraPayload(data, onResultState.value) },
                onError = { error ->
                    val message = error.toString()
                    if (message != "cancelled") {
                        println("[masterdoc detect] wasm camera failed: $message")
                        onErrorState.value(cameraErrorMessage(message))
                    }
                    onResultState.value(null)
                },
            )
        },
    )
}

@JsName("masterdocCaptureCamera")
private external fun masterdocCaptureCamera(
    onSuccess: (JsAny) -> Unit,
    onError: (JsAny) -> Unit,
)

private fun handleCameraPayload(data: JsAny, onResult: (PickedImage?) -> Unit) {
    val bytes = jsPayloadToByteArray(data)
    deliverPickedFile(
        bytes = bytes,
        fileName = "camera.jpg",
        contentTypeRaw = "image/jpeg",
        onResult = onResult,
    )
}

private fun jsPayloadToByteArray(data: JsAny): ByteArray = when (data) {
    is ArrayBuffer -> arrayBufferToByteArray(data)
    else -> {
        val uint8 = data.unsafeCast<Uint8Array>()
        ByteArray(uint8.length) { index -> uint8[index].toByte() }
    }
}

private fun arrayBufferToByteArray(buffer: ArrayBuffer): ByteArray {
    val view = Int8Array(buffer)
    return ByteArray(view.length) { index -> view[index] }
}

private fun cameraErrorMessage(raw: String): String = when {
    raw.contains("Permission", ignoreCase = true) ||
        raw.contains("NotAllowed", ignoreCase = true) ->
        "Нет доступа к камере. Разрешите камеру в браузере или выберите станцию из списка."
    raw.contains("NotFound", ignoreCase = true) ||
        raw.contains("DevicesNotFound", ignoreCase = true) ->
        "Камера не найдена. Используйте список оборудования."
    raw.contains("NotReadable", ignoreCase = true) ->
        "Камера занята другим приложением или недоступна. Закройте другие вкладки с камерой и попробуйте снова."
    raw.contains("insecure-context", ignoreCase = true) ->
        "Камера в браузере доступна только по HTTPS. Откройте приложение по защищённому адресу."
    raw.contains("mediaDevices unavailable", ignoreCase = true) ->
        "Браузер не поддерживает камеру. Используйте Chrome или Safari."
    raw.contains("not ready", ignoreCase = true) ||
        raw.contains("empty photo", ignoreCase = true) ||
        raw.contains("preview timeout", ignoreCase = true) ->
        "Не удалось снять кадр. Дождитесь превью и нажмите «Снять» ещё раз."
    else -> "Камера недоступна: $raw"
}

private fun createGalleryFileInput(
    onResult: (PickedImage?) -> Unit,
): HTMLInputElement {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = "image/png,image/jpeg,image/jpg,image/webp,image/gif"
    input.removeAttribute("capture")
    input.style.display = "none"
    input.onchange = {
        val file = input.files?.item(0)
        input.value = ""
        if (file == null) {
            onResult(null)
        } else {
            file.readBytes { bytes ->
                deliverPickedFile(bytes, file.name, file.type, onResult)
            }
        }
    }
    document.body?.appendChild(input)
    return input
}

private fun deliverPickedFile(
    bytes: ByteArray,
    fileName: String,
    contentTypeRaw: String,
    onResult: (PickedImage?) -> Unit,
) {
    if (bytes.isEmpty()) {
        onResult(null)
        return
    }
    val contentType = contentTypeRaw.ifBlank { guessContentType(fileName) }
    println("[masterdoc detect] wasm picked ${bytes.size} bytes from $fileName")
    onResult(PickedImage(bytes, fileName.ifBlank { "photo.jpg" }, contentType))
}

private fun File.readBytes(onReady: (ByteArray) -> Unit) {
    val reader = FileReader()
    reader.onload = {
        val buffer = reader.result as? ArrayBuffer
        if (buffer == null) {
            onReady(ByteArray(0))
        } else {
            onReady(arrayBufferToByteArray(buffer))
        }
    }
    reader.onerror = {
        println("[masterdoc detect] wasm FileReader error")
        onReady(ByteArray(0))
    }
    reader.readAsArrayBuffer(this)
}

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
    else -> "image/jpeg"
}
