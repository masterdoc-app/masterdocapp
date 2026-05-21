package pro.masterdoc.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.domain.assistant.Assistant
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore

@Composable
fun EquipmentSelectionContent(store: EquipmentSelectionStore) {
    val state by store.states.collectAsState(initial = store.state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Выберите оборудование",
            style = MaterialTheme.typography.headlineSmall,
        )

        Button(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Определить по фото (скоро)")
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = { store.accept(EquipmentSelectionStore.Intent.RetryLoad) }) {
                Text("Повторить")
            }
        }

        if (!state.isLoading && state.error == null && state.assistants.isEmpty()) {
            Text(
                text = "Список оборудования пуст",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.assistants, key = { it.id }) { assistant ->
                AssistantCard(
                    assistant = assistant,
                    onClick = { store.accept(EquipmentSelectionStore.Intent.Select(assistant)) },
                )
            }
        }
    }
}

@Composable
private fun AssistantCard(
    assistant: Assistant,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(
            text = assistant.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}
