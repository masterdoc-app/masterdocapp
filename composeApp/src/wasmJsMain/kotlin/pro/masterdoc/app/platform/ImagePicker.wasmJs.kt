package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
    val galleryInput = remember { createFileInput(capture = false, onResult) }
    val cameraInput = remember { createFileInput(capture = true, onResult) }

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

private fun createFileInput(
    capture: Boolean,
    onResult: (PickedImage?) -> Unit,
): HTMLInputElement {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = "image/*"
    if (capture) {
        input.setAttribute("capture", "environment")
    }
    input.style.display = "none"
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
                println("[masterdoc detect] wasm picked ${bytes.size} bytes from ${file.name}")
                onResult(
                    PickedImage(
                        bytes = bytes,
                        fileName = file.name.ifBlank { if (capture) "camera.jpg" else "photo.jpg" },
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
