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

/**
 * Web camera via hidden file input + [capture] (system picker / native camera UI).
 * Live [masterdocCaptureCamera] overlay is opt-in via [ImagePickerLaunchers.openLiveCamera].
 */
@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers {
    val onResultState = rememberUpdatedState(onResult)
    val galleryInput = remember {
        createFileInput(source = PhotoInputSource.Gallery) { onResultState.value(it) }
    }
    val cameraInput = remember {
        createFileInput(source = PhotoInputSource.Camera) { onResultState.value(it) }
    }

    DisposableEffect(Unit) {
        onDispose {
            galleryInput.remove()
            cameraInput.remove()
        }
    }

    return ImagePickerLaunchers(
        openGallery = { galleryInput.click() },
        openCamera = { cameraInput.click() },
        openLiveCamera = {
            masterdocCaptureCamera(
                onSuccess = { data -> handleCameraPayload(data, onResultState.value) },
                onError = { error ->
                    val message = error.toString()
                    if (message == "cancelled") {
                        onResultState.value(null)
                    } else {
                        println("[masterdoc detect] wasm live camera failed: $message, falling back to file picker")
                        cameraInput.click()
                    }
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

@JsName("masterdocReadImageFile")
private external fun masterdocReadImageFile(
    file: File,
    onReady: (JsAny?) -> Unit,
)

private enum class PhotoInputSource {
    Gallery,
    Camera,
}

private fun createFileInput(
    source: PhotoInputSource,
    onResult: (PickedImage?) -> Unit,
): HTMLInputElement {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.style.display = "none"
    when (source) {
        PhotoInputSource.Gallery -> {
            input.accept = "image/png,image/jpeg,image/jpg,image/webp,image/gif"
            input.removeAttribute("capture")
        }
        PhotoInputSource.Camera -> {
            input.accept = "image/*"
            input.setAttribute("capture", "environment")
        }
    }
    input.onchange = {
        val file = input.files?.item(0)
        input.value = ""
        if (file == null) {
            onResult(null)
        } else {
            val defaultFileName = if (source == PhotoInputSource.Camera) "camera.jpg" else "photo.jpg"
            masterdocReadImageFile(file) { data ->
                val bytes = if (data == null) ByteArray(0) else jsPayloadToByteArray(data)
                deliverPickedFile(
                    bytes = bytes,
                    fileName = file.name,
                    contentTypeRaw = file.type,
                    defaultFileName = defaultFileName,
                    onResult = onResult,
                )
            }
        }
    }
    document.body?.appendChild(input)
    return input
}

private fun handleCameraPayload(data: JsAny, onResult: (PickedImage?) -> Unit) {
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

@JsName("masterdocCompressDetectImage")
private external fun masterdocCompressDetectImage(
    input: JsAny,
    onReady: (JsAny?) -> Unit,
)

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

private fun deliverPickedFile(
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

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
    else -> "image/jpeg"
}
