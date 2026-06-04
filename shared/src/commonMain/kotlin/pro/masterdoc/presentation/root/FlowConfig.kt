package pro.masterdoc.presentation.root

import kotlinx.serialization.Serializable

@Serializable
enum class FlowConfig {
    Scan,
    Camera,
    ChatDescribe,
    Summary,
    FrequentIssues,
}
