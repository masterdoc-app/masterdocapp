package pro.fixaverse.presentation.root

import kotlinx.serialization.Serializable

@Serializable
enum class FlowConfig {
    Scan,
    Camera,
    ChatDescribe,
    Summary,
    KnowledgeBase,
}
