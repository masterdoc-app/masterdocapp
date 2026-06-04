package pro.masterdoc.app.ui.theme

/** ISO-8601 from API (`2026-06-03T12:34:56`) → readable label. */
fun masterdocReportDateLabel(iso: String): String {
    val trimmed = iso.trim()
    if (trimmed.length < 10) return trimmed
    val datePart = trimmed.take(10)
    val segments = datePart.split('-')
    if (segments.size != 3) return trimmed.take(19).replace('T', ' ')
    val timeSuffix = trimmed.drop(10).replace('T', ' ').take(9)
    return "${segments[2]}.${segments[1]}.${segments[0]}$timeSuffix"
}
