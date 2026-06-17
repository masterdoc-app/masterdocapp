package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable

@Composable
actual fun ScanScreenCameraBindings(
    launchers: ImagePickerLaunchers,
    active: Boolean,
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
) = Unit
