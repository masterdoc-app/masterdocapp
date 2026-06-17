package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit = {},
): ImagePickerLaunchers
