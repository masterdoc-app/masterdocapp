package pro.masterdoc.presentation.root

import kotlinx.browser.sessionStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val STORAGE_KEY = "masterdoc.flow.v1"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

actual fun platformFlowNavigationPersistence(): FlowNavigationPersistence =
    SessionStorageFlowNavigationPersistence()

private class SessionStorageFlowNavigationPersistence : FlowNavigationPersistence {
    override fun read(): FlowNavigationSnapshot? = try {
        val raw = sessionStorage.getItem(STORAGE_KEY) ?: return null
        json.decodeFromString<FlowNavigationSnapshot>(raw)
    } catch (_: Throwable) {
        null
    }

    override fun write(snapshot: FlowNavigationSnapshot) {
        sessionStorage.setItem(STORAGE_KEY, json.encodeToString(snapshot))
    }

    override fun clear() {
        sessionStorage.removeItem(STORAGE_KEY)
    }
}
