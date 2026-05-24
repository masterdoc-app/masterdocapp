package pro.masterdoc.app.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
): ImagePickerLaunchers {
    val context = LocalContext.current
    val cameraPhotoFile = remember { createCameraPhotoFile(context) }
    val cameraPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cameraPhotoFile,
        )
    }

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
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (!success || !cameraPhotoFile.exists() || cameraPhotoFile.length() == 0L) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        val bytes = cameraPhotoFile.readBytes()
        println("[masterdoc detect] android camera picked ${bytes.size} bytes")
        onResult(PickedImage(bytes, "camera.jpg", "image/jpeg"))
    }

    return remember {
        ImagePickerLaunchers(
            openGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            openCamera = {
                cameraPhotoFile.parentFile?.mkdirs()
                cameraPhotoFile.delete()
                cameraLauncher.launch(cameraPhotoUri)
            },
        )
    }
}

private fun createCameraPhotoFile(context: Context): File =
    File(context.cacheDir, "camera/detect.jpg")

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    else -> "image/jpeg"
}
