package pro.masterdoc.presentation.equipment

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
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
            bootstrapper = SimpleBootstrapper(Action.Load),
            executorFactory = { Executor(repository) },
            reducer = ReducerImpl,
        ) {}
}

private sealed interface Action {
    data object Load : Action
}

private sealed interface Msg {
    data class SetLoading(val loading: Boolean) : Msg
    data class SetError(val error: String?) : Msg
    data class Loaded(val assistants: List<Assistant>) : Msg
    data class Selected(val assistant: Assistant?) : Msg
}

private object ReducerImpl : Reducer<EquipmentSelectionStore.State, Msg> {
    override fun EquipmentSelectionStore.State.reduce(msg: Msg): EquipmentSelectionStore.State = when (msg) {
        is Msg.SetLoading -> copy(isLoading = msg.loading)
        is Msg.SetError -> copy(error = msg.error)
        is Msg.Loaded -> copy(assistants = msg.assistants, isLoading = false, error = null)
        is Msg.Selected -> copy(selectedAssistant = msg.assistant)
    }
}

private class Executor(
    private val repository: AssistantsRepository,
) : CoroutineExecutor<
    EquipmentSelectionStore.Intent,
    Action,
    EquipmentSelectionStore.State,
    Msg,
    EquipmentSelectionStore.Label,
    >() {

    override fun executeAction(action: Action) {
        when (action) {
            Action.Load -> load()
        }
    }

    override fun executeIntent(intent: EquipmentSelectionStore.Intent) {
        when (intent) {
            EquipmentSelectionStore.Intent.RetryLoad -> load()
            is EquipmentSelectionStore.Intent.Select -> dispatch(Msg.Selected(intent.assistant))
            EquipmentSelectionStore.Intent.ClearSelection -> dispatch(Msg.Selected(null))
        }
    }

    private fun load() {
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
