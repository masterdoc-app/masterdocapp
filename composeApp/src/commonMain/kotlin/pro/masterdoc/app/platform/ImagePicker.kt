package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
): ImagePickerLaunchers
