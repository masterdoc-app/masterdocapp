package pro.masterdoc.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.SwingUtilities

@Composable
actual fun rememberImagePickerLaunchers(
    onResult: (PickedImage?) -> Unit,
    onCameraError: (String) -> Unit,
): ImagePickerLaunchers {
    fun pickFromDisk() {
        SwingUtilities.invokeLater {
            val dialog = FileDialog(null as Frame?, "Выберите фото", FileDialog.LOAD)
            dialog.file = "*.jpg;*.jpeg;*.png;*.webp"
            dialog.isVisible = true
            val path = dialog.file
            val dir = dialog.directory
            if (path == null || dir == null) {
                onResult(null)
                return@invokeLater
            }
            val file = File(dir, path)
            if (!file.exists()) {
                onResult(null)
                return@invokeLater
            }
            val bytes = file.readBytes()
            val name = file.name
            val type = guessContentType(name)
            println("[masterdoc detect] desktop picked ${bytes.size} bytes from $name")
            onResult(PickedImage(bytes, name, type))
        }
    }

    return remember {
        ImagePickerLaunchers(
            openGallery = { pickFromDisk() },
            openCamera = { pickFromDisk() },
        )
    }
}

private fun guessContentType(fileName: String): String = when {
    fileName.endsWith(".png", ignoreCase = true) -> "image/png"
    fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
    else -> "image/jpeg"
}
