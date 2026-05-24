package pro.masterdoc.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.platform.rememberImagePickerLaunchers
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocLoadingIndicator
import pro.masterdoc.app.ui.theme.MasterdocPhotoSourceDialog
import pro.masterdoc.app.ui.theme.MasterdocPrimaryButton
import pro.masterdoc.app.ui.theme.MasterdocScreenTitle
import pro.masterdoc.app.ui.theme.MasterdocSecondaryButton
import pro.masterdoc.app.ui.theme.MasterdocSelectableCard
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore

@Composable
fun EquipmentSelectionContent(store: EquipmentSelectionStore) {
    val state by store.states.collectAsState(initial = store.state)
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val imagePickers = rememberImagePickerLaunchers { picked ->
        if (picked == null || picked.bytes.isEmpty()) {
            println("[masterdoc detect] photo pick cancelled or empty")
            return@rememberImagePickerLaunchers
        }
        store.accept(
            EquipmentSelectionStore.Intent.DetectFromPhoto(
                imageBytes = picked.bytes,
                fileName = picked.fileName,
                contentType = picked.contentType,
            ),
        )
    }

    if (showPhotoSourceDialog) {
        MasterdocPhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onGallery = {
                showPhotoSourceDialog = false
                imagePickers.openGallery()
            },
            onCamera = {
                showPhotoSourceDialog = false
                imagePickers.openCamera()
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MasterdocDimens.Space16),
        verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space12),
    ) {
        MasterdocScreenTitle(text = "Выберите оборудование")

        MasterdocPrimaryButton(
            text = when {
                state.isDetecting -> "Определяем по фото…"
                else -> "Определить по фото"
            },
            onClick = {
                store.accept(EquipmentSelectionStore.Intent.ClearDetectError)
                showPhotoSourceDialog = true
            },
            enabled = !state.isLoading && !state.isDetecting,
        )

        if (state.isDetecting) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(MasterdocDimens.Space8),
                contentAlignment = Alignment.Center,
            ) {
                MasterdocLoadingIndicator()
            }
        }

        state.detectError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(MasterdocDimens.Space24),
                contentAlignment = Alignment.Center,
            ) {
                MasterdocLoadingIndicator()
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            MasterdocSecondaryButton(
                text = "Повторить",
                onClick = { store.accept(EquipmentSelectionStore.Intent.RetryLoad) },
            )
        }

        if (!state.isLoading && state.error == null && state.assistants.isEmpty()) {
            Text(
                text = "Список оборудования пуст",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
        ) {
            items(state.assistants, key = { it.id }) { assistant ->
                MasterdocSelectableCard(
                    title = assistant.name,
                    onClick = { store.accept(EquipmentSelectionStore.Intent.Select(assistant)) },
                )
            }
        }
    }
}
