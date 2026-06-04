package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pro.masterdoc.app.ui.theme.LiteCameraHeroButton

@Composable
actual fun ScanCameraHeroButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    isLoading: Boolean,
) {
    LiteCameraHeroButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
    )
}
