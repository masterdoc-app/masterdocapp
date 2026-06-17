package pro.fixaverse.app.ui.theme

/** User-visible product name (Fixaverse B2B). */
object AppBranding {
    const val NAME = "Fixaverse"
    const val ASSISTANT_LABEL = "FIXAVERSE"

    fun screenTitle(equipmentName: String?): String =
        equipmentName?.let { "$NAME · $it" } ?: NAME
}
