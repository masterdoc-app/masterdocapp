package pro.fixaverse.data.citation

/** Onyx inline citations use empty markdown links: `[[1]]()`. */
fun preprocessCitationMarkdown(content: String): String =
    CITATION_LINK_REGEX.replace(content) { match ->
        val index = match.groupValues[1].ifBlank { match.groupValues[2] }
        "[$index]($CITATION_HTTPS_BASE$index)"
    }

fun parseCitationUri(uri: String): String? {
    val trimmed = uri.trim()
    return when {
        trimmed.startsWith(CITATION_SCHEME) ->
            trimmed.removePrefix(CITATION_SCHEME).takeIf { it.isNotBlank() }
        trimmed.startsWith(CITATION_HTTPS_BASE) ->
            trimmed.removePrefix(CITATION_HTTPS_BASE).substringBefore('/').takeIf { it.isNotBlank() }
        else -> null
    }
}

/** Custom scheme kept for backwards compatibility with already-rendered messages. */
private const val CITATION_SCHEME = "fixaverse://citation/"
/** HTTPS URLs are parsed reliably by markdown renderers and Skiko link hit-testing. */
internal const val CITATION_HTTPS_BASE = "https://copilot.fixaverse.ru/.citation/"
private val CITATION_LINK_REGEX = Regex("""\[\[(\d+)\]\]\(\)|\[(\d+)\]\(\)""")
