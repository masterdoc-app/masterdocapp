package pro.masterdoc.presentation.chat

import com.arkivanov.decompose.ComponentContext
import pro.masterdoc.presentation.equipment.EquipmentSelectionStoreFactory

class DefaultChatComponent(
    componentContext: ComponentContext,
    storeFactory: ChatStoreFactory,
    equipmentStoreFactory: EquipmentSelectionStoreFactory,
) : ChatComponent, ComponentContext by componentContext {
    override val equipmentStore: pro.masterdoc.presentation.equipment.EquipmentSelectionStore =
        equipmentStoreFactory.create()
    override val store: ChatStore = storeFactory.create()
}
