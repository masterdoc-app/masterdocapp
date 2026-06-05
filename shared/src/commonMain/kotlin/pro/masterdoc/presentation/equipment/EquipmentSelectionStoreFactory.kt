package pro.masterdoc.presentation.equipment

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import pro.masterdoc.data.assistant.AssistantsRepository
import pro.masterdoc.domain.assistant.Assistant

class EquipmentSelectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: AssistantsRepository,
) {
    fun create(): EquipmentSelectionStore = object : EquipmentSelectionStore,
        com.arkivanov.mvikotlin.core.store.Store<
            EquipmentSelectionStore.Intent,
            EquipmentSelectionStore.State,
            EquipmentSelectionStore.Label,
            > by storeFactory.create(
            name = "EquipmentSelectionStore",
            initialState = EquipmentSelectionStore.State(),
            executorFactory = { Executor(repository) },
            reducer = ReducerImpl,
        ) {}
}

private sealed interface Msg {
    data class SetLoading(val loading: Boolean) : Msg
    data class SetError(val error: String?) : Msg
    data class SetDetecting(val detecting: Boolean) : Msg
    data class SetDetectProgress(val progress: String) : Msg
    data class SetDetectError(val error: String?) : Msg
    data class Loaded(val assistants: List<Assistant>) : Msg
    data class Selected(val assistant: Assistant?) : Msg
}

private object ReducerImpl : Reducer<EquipmentSelectionStore.State, Msg> {
    override fun EquipmentSelectionStore.State.reduce(msg: Msg): EquipmentSelectionStore.State = when (msg) {
        is Msg.SetLoading -> copy(isLoading = msg.loading)
        is Msg.SetError -> copy(error = msg.error)
        is Msg.SetDetecting -> copy(
            isDetecting = msg.detecting,
            detectProgress = if (msg.detecting) detectProgress else "",
        )
        is Msg.SetDetectProgress -> copy(detectProgress = msg.progress)
        is Msg.SetDetectError -> copy(detectError = msg.error)
        is Msg.Loaded -> copy(assistants = msg.assistants, isLoading = false, error = null)
        is Msg.Selected -> copy(selectedAssistant = msg.assistant, detectError = null)
    }
}

private class Executor(
    private val repository: AssistantsRepository,
) : CoroutineExecutor<
    EquipmentSelectionStore.Intent,
    Nothing,
    EquipmentSelectionStore.State,
    Msg,
    EquipmentSelectionStore.Label,
    >() {

    override fun executeIntent(intent: EquipmentSelectionStore.Intent) {
        when (intent) {
            EquipmentSelectionStore.Intent.Load,
            EquipmentSelectionStore.Intent.RetryLoad,
            -> load()
            is EquipmentSelectionStore.Intent.Select -> dispatch(Msg.Selected(intent.assistant))
            EquipmentSelectionStore.Intent.ClearSelection -> dispatch(Msg.Selected(null))
            EquipmentSelectionStore.Intent.ClearDetectError -> dispatch(Msg.SetDetectError(null))
            is EquipmentSelectionStore.Intent.DetectFromPhoto -> detect(intent)
        }
    }

    private fun detect(intent: EquipmentSelectionStore.Intent.DetectFromPhoto) {
        dispatch(Msg.SetDetectError(null))
        dispatch(Msg.SetDetectProgress("Отправляем фото…"))
        dispatch(Msg.SetDetecting(true))
        scope.launch {
            var assistants = state().assistants
            if (assistants.isEmpty()) {
                repository.listAssistants()
                    .onFailure { error ->
                        dispatch(Msg.SetDetecting(false))
                        dispatch(
                            Msg.SetDetectError(
                                error.message?.takeIf { it.isNotBlank() }
                                    ?: "Не удалось загрузить список станций",
                            ),
                        )
                        return@launch
                    }
                    .onSuccess { loaded ->
                        dispatch(Msg.Loaded(loaded))
                        assistants = loaded
                    }
            }
            repository.detectAssistant(
                imageBytes = intent.imageBytes,
                fileName = intent.fileName,
                contentType = intent.contentType,
                candidateNames = assistants.map { it.name },
                onProgress = { progress ->
                    dispatch(Msg.SetDetectProgress(progress))
                },
            )
                .onSuccess { detectedName ->
                    dispatch(Msg.SetDetecting(false))
                    val assistant = resolveAssistant(state().assistants, detectedName)
                    if (assistant != null) {
                        println("[masterdoc detect] matched assistant id=${assistant.id} name=${assistant.name}")
                        dispatch(Msg.Selected(assistant))
                    } else {
                        println("[masterdoc detect] no match for detectedName=$detectedName")
                        dispatch(
                            Msg.SetDetectError("Не удалось определить оборудование по фото"),
                        )
                    }
                }
                .onFailure { error ->
                    dispatch(Msg.SetDetecting(false))
                    println("[masterdoc detect] failed: ${error.message}")
                    dispatch(
                        Msg.SetDetectError(
                            error.message?.takeIf { it.isNotBlank() }
                                ?: "Не удалось определить оборудование по фото",
                        ),
                    )
                }
        }
    }

    private fun resolveAssistant(assistants: List<Assistant>, detectedName: String?): Assistant? {
        val name = detectedName?.trim().orEmpty()
        if (name.isEmpty()) return null
        assistants.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        val normalized = name.lowercase()
        return assistants.firstOrNull { assistant ->
            val assistantNorm = assistant.name.lowercase()
            normalized.contains(assistantNorm) || assistantNorm.contains(normalized)
        }
    }

    private fun load() {
        if (state().isLoading || state().assistants.isNotEmpty()) return
        dispatch(Msg.SetLoading(true))
        dispatch(Msg.SetError(null))
        scope.launch {
            repository.listAssistants()
                .onSuccess { dispatch(Msg.Loaded(it)) }
                .onFailure { error ->
                    dispatch(Msg.SetLoading(false))
                    dispatch(Msg.SetError(error.message ?: "Не удалось загрузить список"))
                }
        }
    }
}
