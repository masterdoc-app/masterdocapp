package pro.masterdoc.presentation.equipment

import com.arkivanov.mvikotlin.core.store.Store
import pro.masterdoc.domain.assistant.Assistant

interface EquipmentSelectionStore :
    Store<EquipmentSelectionStore.Intent, EquipmentSelectionStore.State, EquipmentSelectionStore.Label> {

    data class State(
        val assistants: List<Assistant> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isDetecting: Boolean = false,
        val detectError: String? = null,
        val selectedAssistant: Assistant? = null,
    )

    sealed interface Intent {
        /** Fetch assistants when the user opens the equipment list (not on app start). */
        data object Load : Intent
        data object RetryLoad : Intent
        data class Select(val assistant: Assistant) : Intent
        data object ClearSelection : Intent
        data class DetectFromPhoto(
            val imageBytes: ByteArray,
            val fileName: String,
            val contentType: String,
        ) : Intent
        data object ClearDetectError : Intent
    }

    sealed interface Label
}
