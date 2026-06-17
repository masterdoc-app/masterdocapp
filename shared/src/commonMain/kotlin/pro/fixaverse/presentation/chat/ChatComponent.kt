package pro.fixaverse.presentation.chat

import pro.fixaverse.presentation.equipment.EquipmentSelectionStore

interface ChatComponent {
    val equipmentStore: EquipmentSelectionStore
    val store: ChatStore
}
