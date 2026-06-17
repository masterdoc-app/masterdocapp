package pro.fixaverse.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import kotlin.js.JsAny
import kotlin.js.JsName

@Composable
actual fun ScanScreenCameraBindings(
    launchers: ImagePickerLaunchers,
    active: Boolean,
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val activeState = rememberUpdatedState(active)
    val onResultState = rememberUpdatedState(onResult)
    val onErrorState = rememberUpdatedState(onCameraError)

    DisposableEffect(activeState.value) {
        if (activeState.value) {
            fixaverseActivateScanCamera(
                onSuccess = { data ->
                    handleCameraPayload(data, onResultState.value)
                },
                onError = { error ->
                    val message = error.toString()
                    if (message != "cancelled") {
                        onErrorState.value(cameraErrorMessage(message))
                    }
                    onResultState.value(null)
                },
            )
        } else {
            fixaverseDeactivateScanCamera()
        }
        onDispose {
            fixaverseDeactivateScanCamera()
        }
    }
}

@JsName("fixaverseActivateScanCamera")
private external fun fixaverseActivateScanCamera(
    onSuccess: (JsAny) -> Unit,
    onError: (JsAny) -> Unit,
)

@JsName("fixaverseDeactivateScanCamera")
private external fun fixaverseDeactivateScanCamera()
