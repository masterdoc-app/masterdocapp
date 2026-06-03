package pro.masterdoc.app.platform

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "photo.jpg"
                val type = context.contentResolver.getType(uri) ?: guessContentType(name)
                println("[masterdoc detect] android gallery picked ${bytes.size} bytes")
                PickedImage(bytes, name, type)
            }
        }
            .onSuccess { picked -> onResult(picked) }
            .onFailure {
                println("[masterdoc detect] android gallery failed: ${it.message}")
                onResult(null)
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val bytes = stream.toByteArray()
        println("[masterdoc detect] android camera picked ${bytes.size} bytes")
        onResult(PickedImage(bytes, "camera.jpg", "image/jpeg"))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            println("[masterdoc detect] android camera permission denied")
            onCameraError("Нет доступа к камере")
            onResult(null)
        }
    }

    fun launchCamera() {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch(null)
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    return ImagePickerLaunchers(
        openGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        openCamera = ::launchCamera,
    )
}

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    else -> "image/jpeg"
}
