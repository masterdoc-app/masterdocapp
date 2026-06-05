package pro.masterdoc.platform

actual object PlatformCapabilities {
    actual val supportsCamera: Boolean = false
    actual val supportsFilePicker: Boolean = true
    actual val supportsMicrophone: Boolean = true
}
