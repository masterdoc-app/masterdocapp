package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/** Camera and gallery via [ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP). */
@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers {
    val onResultState = rememberUpdatedState(onResult)
    val onErrorState = rememberUpdatedState(onCameraError)
    var cameraSession by remember { mutableIntStateOf(0) }
    var gallerySession by remember { mutableIntStateOf(0) }

    ImagePickerKmpCameraHost(
        session = cameraSession,
        onFinished = { cameraSession = 0 },
        onResult = { onResultState.value(it) },
        onCameraError = { onErrorState.value(it) },
    )
    ImagePickerKmpGalleryHost(
        session = gallerySession,
        onFinished = { gallerySession = 0 },
        onResult = { onResultState.value(it) },
    )

    return remember {
        ImagePickerLaunchers(
            openGallery = { gallerySession += 1 },
            openCamera = { cameraSession += 1 },
            openLiveCamera = { cameraSession += 1 },
        )
    }
}
