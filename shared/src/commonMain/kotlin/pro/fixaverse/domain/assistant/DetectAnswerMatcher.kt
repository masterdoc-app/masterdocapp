package pro.fixaverse.domain.assistant

/** Maps a free-form LLM answer to one of the known assistant display names. */
object DetectAnswerMatcher {
    fun match(answer: String, candidateNames: List<String>): String? {
        if (candidateNames.isEmpty()) return null
        val normalizedAnswer = normalize(answer)
        if (normalizedAnswer.isBlank()) return null

        val quoted = QUOTED_NAME.find(answer)?.groupValues?.get(1)?.let(::normalize)
        if (quoted != null) {
            matchCandidate(quoted, candidateNames)?.let { return it }
        }

        val sorted = candidateNames.sortedByDescending { it.length }
        for (name in sorted) {
            if (normalizedAnswer.contains(normalize(name))) {
                return name
            }
        }

        for (name in sorted) {
            val tokens = normalize(name).split(' ').filter { it.length >= 4 }
            if (tokens.isNotEmpty() && tokens.all { normalizedAnswer.contains(it) }) {
                return name
            }
        }

        return null
    }

    private fun matchCandidate(fragment: String, candidateNames: List<String>): String? {
        val exact = candidateNames.firstOrNull { normalize(it) == fragment }
        if (exact != null) return exact
        return candidateNames.firstOrNull { fragment.contains(normalize(it)) }
    }

    private fun normalize(value: String): String =
        value
            .lowercase()
            .replace('«', '"')
            .replace('»', '"')
            .replace(Regex("""["'`]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

    private val QUOTED_NAME = Regex("""["«]([^"»]+)["»]""")
}
