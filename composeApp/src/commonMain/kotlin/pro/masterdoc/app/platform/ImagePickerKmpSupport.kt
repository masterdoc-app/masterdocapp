package pro.masterdoc.app.platform

import io.github.ismoy.imagepickerkmp.domain.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.domain.config.ImagePickerConfig
import io.github.ismoy.imagepickerkmp.domain.config.PermissionAndConfirmationConfig
import io.github.ismoy.imagepickerkmp.domain.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.domain.models.PhotoResult

internal fun masterdocImagePickerConfig(
    onPhotoCaptured: (PhotoResult) -> Unit,
    onError: (Exception) -> Unit,
    onDismiss: () -> Unit,
): ImagePickerConfig = ImagePickerConfig(
    enableCrop = false,
    cameraCaptureConfig = CameraCaptureConfig(
        permissionAndConfirmationConfig = PermissionAndConfirmationConfig(
            skipConfirmation = true,
        ),
    ),
    onPhotoCaptured = onPhotoCaptured,
    onError = onError,
    onDismiss = onDismiss,
)

internal fun GalleryPhotoResult.toPickedImage(): PickedImage? = PhotoResult(
    uri = uri,
    width = width,
    height = height,
    fileName = fileName,
    fileSize = fileSize,
    mimeType = mimeType,
    exif = exif,
).toPickedImage()

internal fun PhotoResult.toPickedImage(): PickedImage? {
    val bytes = loadBytes()
    if (bytes.isEmpty()) return null
    val name = fileName?.takeIf { it.isNotBlank() } ?: "photo.jpg"
    val type = mimeType?.takeIf { it.isNotBlank() } ?: guessMasterdocContentType(name)
    return PickedImage(bytes = bytes, fileName = name, contentType = type)
}

internal fun mapImagePickerError(exception: Exception): String {
    val raw = exception.message.orEmpty()
    return when {
        raw.contains("permission", ignoreCase = true) ||
            raw.contains("denied", ignoreCase = true) ->
            "Нет доступа к камере. Разрешите камеру в настройках."
        raw.contains("cancel", ignoreCase = true) -> "cancelled"
        raw.isBlank() -> "Камера недоступна"
        else -> raw
    }
}

private fun guessMasterdocContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    else -> "image/jpeg"
}
