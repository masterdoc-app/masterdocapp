package pro.masterdoc.presentation.root

class InMemoryFlowNavigationPersistence : FlowNavigationPersistence {
    var snapshot: FlowNavigationSnapshot? = null

    override fun read(): FlowNavigationSnapshot? = snapshot

    override fun write(snapshot: FlowNavigationSnapshot) {
        this.snapshot = snapshot
    }

    override fun clear() {
        snapshot = null
    }
}
