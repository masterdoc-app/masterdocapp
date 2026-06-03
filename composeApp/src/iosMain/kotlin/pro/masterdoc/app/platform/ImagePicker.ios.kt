package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers = remember {
    ImagePickerLaunchers(
        openGallery = { onResult(null) },
        openCamera = { onResult(null) },
    )
}
