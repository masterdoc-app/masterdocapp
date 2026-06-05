package pro.masterdoc.platform

actual object PlatformCapabilities {
    actual val supportsCamera: Boolean = true
    actual val supportsFilePicker: Boolean = true
    actual val supportsMicrophone: Boolean = true
    actual val supportsEnterToSend: Boolean = false
}
