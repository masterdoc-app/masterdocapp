package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.get
import kotlin.js.JsAny
import kotlin.js.JsName

/**
 * Web (Wasm): ImagePickerKMP wasmJs is a stub — camera/gallery via [fixaverse-camera.js].
 * Scan screen: [ScanScreenCameraBindings] + HTML [#fixaverse-scan-shutter] for trusted gestures.
 * Other screens: [fixaverseCaptureCamera] (live preview).
 */
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

    val openLive = {
        if (!fixaverseOpenScanCamera()) {
            requestLiveCamera(
                onResult = { onResultState.value(it) },
                onError = { onErrorState.value(it) },
            )
        }
    }

    return ImagePickerLaunchers(
        openGallery = { galleryInput.click() },
        openCamera = openLive,
        openLiveCamera = openLive,
    )
}

@JsName("fixaverseOpenScanCamera")
private external fun fixaverseOpenScanCamera(): Boolean

@JsName("fixaverseCaptureCamera")
private external fun fixaverseCaptureCamera(
    onSuccess: (JsAny) -> Unit,
    onError: (JsAny) -> Unit,
)

@JsName("fixaverseReadImageFile")
private external fun fixaverseReadImageFile(
    file: File,
    onReady: (JsAny?) -> Unit,
)

private fun requestLiveCamera(
    onResult: (PickedImage?) -> Unit,
    onError: (String) -> Unit,
) {
    fixaverseCaptureCamera(
        onSuccess = { data -> handleCameraPayload(data, onResult) },
        onError = { error ->
            val message = error.toString()
            if (message == "cancelled") {
                onResult(null)
            } else {
                println("[fixaverse detect] wasm camera failed: $message")
                onError(cameraErrorMessage(message))
                onResult(null)
            }
        },
    )
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
            fixaverseReadImageFile(file) { data ->
                val bytes = if (data == null) ByteArray(0) else jsPayloadToByteArray(data)
                deliverPickedFile(
                    bytes = bytes,
                    fileName = file.name,
                    contentTypeRaw = file.type,
                    defaultFileName = "photo.jpg",
                    onResult = onResult,
                )
            }
        }
    }
    document.body?.appendChild(input)
    return input
}
