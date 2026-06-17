package pro.fixaverse.app.platform

data class PickedImage(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PickedImage
        return bytes.contentEquals(other.bytes) &&
            fileName == other.fileName &&
            contentType == other.contentType
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

class ImagePickerLaunchers(
    val openGallery: () -> Unit,
    val openCamera: () -> Unit,
    /** Fullscreen live preview (web JS overlay). Defaults to [openCamera] on non-web targets. */
    val openLiveCamera: () -> Unit = openCamera,
)
