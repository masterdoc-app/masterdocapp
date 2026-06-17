package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable

/**
 * Wasm only: HTML [#fixaverse-scan-shutter] → getUserMedia (trusted click).
 * Native platforms use ImagePickerKMP via [rememberImagePickerLaunchers].
 */
@Composable
expect fun ScanScreenCameraBindings(
    launchers: ImagePickerLaunchers,
    active: Boolean,
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
)
