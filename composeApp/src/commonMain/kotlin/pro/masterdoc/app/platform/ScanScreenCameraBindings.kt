package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable

/**
 * Wasm only: HTML [#masterdoc-scan-shutter] → getUserMedia (trusted click).
 * Native platforms use ImagePickerKMP via [rememberImagePickerLaunchers].
 */
@Composable
expect fun ScanScreenCameraBindings(
    launchers: ImagePickerLaunchers,
    active: Boolean,
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
)
