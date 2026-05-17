package pro.masterdoc.platform

/**
 * Platform feature flags for camera, files, and microphone (v1 stubs).
 * Replace with real expect/actual implementations when adding media flows.
 */
expect object PlatformCapabilities {
    val supportsCamera: Boolean
    val supportsFilePicker: Boolean
    val supportsMicrophone: Boolean
}
