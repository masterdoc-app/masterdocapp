package pro.fixaverse.presentation.root

sealed class FlowChild {
    data object Scan : FlowChild()
    data object Camera : FlowChild()
    data object ChatDescribe : FlowChild()
    data object Summary : FlowChild()
    data object KnowledgeBase : FlowChild()
}
