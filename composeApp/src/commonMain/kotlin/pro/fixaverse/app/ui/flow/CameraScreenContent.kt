package pro.fixaverse.app.ui.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pro.fixaverse.app.platform.rememberImagePickerLaunchers
import pro.fixaverse.app.ui.theme.LiteAppHead
import pro.fixaverse.app.ui.theme.FixaversePalette
import pro.fixaverse.presentation.root.RootComponent

/** Fallback route: opens fullscreen camera overlay immediately (web JS / native). */
@Composable
fun CameraScreenContent(
    root: RootComponent,
    modifier: Modifier = Modifier,
    autoOpenCamera: Boolean = true,
) {
    var openAttempt by remember { mutableStateOf(0) }

    val imagePickers = rememberImagePickerLaunchers(
        onResult = { picked ->
            if (picked == null || picked.bytes.isEmpty()) {
                root.onCameraCancelled()
                return@rememberImagePickerLaunchers
            }
            root.onCameraPhotoResult(
                imageBytes = picked.bytes,
                fileName = picked.fileName,
                contentType = picked.contentType,
            )
        },
        onCameraError = { message ->
            if (message == "cancelled") {
                root.onCameraCancelled()
            } else {
                root.onCameraCancelled()
            }
        },
    )

    LaunchedEffect(openAttempt) {
        if (autoOpenCamera) {
            imagePickers.openLiveCamera()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LiteAppHead(
            title = "Камера",
            subtitle = "Сканирование шильдика",
            onBack = root::onCameraCancelled,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(FixaversePalette.Ink),
        )
    }
}
