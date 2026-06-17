package pro.fixaverse.app.platform

internal fun normalizeUrl(uri: String): String {
    val trimmed = uri.trim()
    if (trimmed.isEmpty()) return trimmed
    if ("://" in trimmed) return trimmed
    return "https://$trimmed"
}
