package pro.masterdoc.presentation.root

interface FlowNavigationPersistence {
    fun read(): FlowNavigationSnapshot?
    fun write(snapshot: FlowNavigationSnapshot)
    fun clear()
}

expect fun platformFlowNavigationPersistence(): FlowNavigationPersistence

class NoOpFlowNavigationPersistence : FlowNavigationPersistence {
    override fun read(): FlowNavigationSnapshot? = null
    override fun write(snapshot: FlowNavigationSnapshot) = Unit
    override fun clear() = Unit
}
