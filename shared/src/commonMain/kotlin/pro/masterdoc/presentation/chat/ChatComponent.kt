package pro.masterdoc.presentation.chat

import pro.masterdoc.presentation.equipment.EquipmentSelectionStore

interface ChatComponent {
    val equipmentStore: EquipmentSelectionStore
    val store: ChatStore
}
