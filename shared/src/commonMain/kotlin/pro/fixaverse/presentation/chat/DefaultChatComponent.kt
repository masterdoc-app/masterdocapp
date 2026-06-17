package pro.fixaverse.presentation.chat

import com.arkivanov.decompose.ComponentContext
import pro.fixaverse.presentation.equipment.EquipmentSelectionStoreFactory

class DefaultChatComponent(
    componentContext: ComponentContext,
    storeFactory: ChatStoreFactory,
    equipmentStoreFactory: EquipmentSelectionStoreFactory,
) : ChatComponent, ComponentContext by componentContext {
    override val equipmentStore: pro.fixaverse.presentation.equipment.EquipmentSelectionStore =
        equipmentStoreFactory.create()
    override val store: ChatStore = storeFactory.create()
}
