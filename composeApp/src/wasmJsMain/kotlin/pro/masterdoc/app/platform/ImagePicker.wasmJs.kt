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

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
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
    )
}

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
            // Camera-only hint for mobile browsers; desktop may still offer a file picker.
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
            file.readBytes { bytes ->
                if (bytes.isEmpty()) {
                    onResult(null)
                } else {
                    println("[masterdoc detect] wasm ${source.name.lowercase()} picked ${bytes.size} bytes from ${file.name}")
                    onResult(
                        PickedImage(
                            bytes = bytes,
                            fileName = file.name.ifBlank {
                                if (source == PhotoInputSource.Camera) "camera.jpg" else "photo.jpg"
                            },
                            contentType = file.type.ifBlank { guessContentType(file.name) },
                        ),
                    )
                }
            }
        }
    }
    document.body?.appendChild(input)
    return input
}

private fun File.readBytes(onReady: (ByteArray) -> Unit) {
    val reader = FileReader()
    reader.onload = {
        val buffer = reader.result as? ArrayBuffer
        if (buffer == null) {
            onReady(ByteArray(0))
        } else {
            val view = Int8Array(buffer)
            val bytes = ByteArray(view.length)
            for (i in bytes.indices) {
                bytes[i] = view[i]
            }
            onReady(bytes)
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
