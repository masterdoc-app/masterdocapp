package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import io.github.ismoy.imagepickerkmp.domain.models.MimeType
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import io.github.ismoy.imagepickerkmp.presentation.ui.components.ImagePickerLauncher

/**
 * Hosts [ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP) camera UI.
 * Not used on Wasm — upstream wasmJs actual is a stub; see [ImagePicker.wasmJs].
 */
@Composable
internal fun ImagePickerKmpCameraHost(
    session: Int,
    onFinished: () -> Unit,
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
) {
    if (session == 0) return
    key(session) {
        ImagePickerLauncher(
            config = fixaverseImagePickerConfig(
                onPhotoCaptured = { photo ->
                    onFinished()
                    onResult(photo.toPickedImage())
                },
                onError = { error ->
                    onFinished()
                    val message = mapImagePickerError(error)
                    if (message == "cancelled") {
                        onResult(null)
                    } else {
                        onCameraError(message)
                        onResult(null)
                    }
                },
                onDismiss = {
                    onFinished()
                    onResult(null)
                },
            ),
        )
    }
}

@Composable
internal fun ImagePickerKmpGalleryHost(
    session: Int,
    onFinished: () -> Unit,
    onResult: (PickedImage?) -> Unit,
) {
    if (session == 0) return
    key(session) {
        GalleryPickerLauncher(
            allowMultiple = false,
            mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG, MimeType.IMAGE_WEBP),
            enableCrop = false,
            onPhotosSelected = { photos ->
                onFinished()
                onResult(photos.firstOrNull()?.toPickedImage())
            },
            onError = {
                onFinished()
                onResult(null)
            },
            onDismiss = {
                onFinished()
                onResult(null)
            },
        )
    }
}
