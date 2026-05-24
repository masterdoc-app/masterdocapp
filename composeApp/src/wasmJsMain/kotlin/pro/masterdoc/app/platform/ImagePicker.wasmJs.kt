package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
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
): ImagePickerLaunchers {
    val onResultState = rememberUpdatedState(onResult)
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
                onSuccess = { data ->
                    deliverPickedFile(
                        bytes = jsUint8ArrayToByteArray(data),
                        fileName = "camera.jpg",
                        contentTypeRaw = "image/jpeg",
                        onResult = onResultState.value,
                    )
                },
                onError = { error ->
                    if (error.toString() != "cancelled") {
                        println("[masterdoc detect] wasm camera failed: $error")
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

private fun jsUint8ArrayToByteArray(data: JsAny): ByteArray {
    val view = data.unsafeCast<Int8Array>()
    return ByteArray(view.length) { index -> view[index] }
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
            val view = Int8Array(buffer)
            onReady(ByteArray(view.length) { index -> view[index] })
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
