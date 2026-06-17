package pro.fixaverse.app.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.delay
import pro.fixaverse.app.platform.rememberImagePickerLaunchers
import pro.fixaverse.app.ui.theme.FixaverseDimens
import pro.fixaverse.app.ui.theme.FixaverseDetectLoadingOverlay
import pro.fixaverse.app.ui.theme.FixaverseLoadingIndicator
import pro.fixaverse.app.ui.theme.FixaversePhotoSourceDialog
import pro.fixaverse.app.ui.theme.FixaversePrimaryButton
import pro.fixaverse.app.ui.theme.FixaverseScreenTitle
import pro.fixaverse.app.ui.theme.FixaverseSecondaryButton
import pro.fixaverse.app.ui.theme.FixaverseSelectableCard
import pro.fixaverse.presentation.equipment.EquipmentSelectionStore

private enum class PendingPhotoSource {
    Gallery,
    Camera,
}

@Composable
fun EquipmentSelectionContent(store: EquipmentSelectionStore) {
    val state by store.states.collectAsState(initial = store.state)
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingPhotoSource by remember { mutableStateOf<PendingPhotoSource?>(null) }

    val imagePickers = rememberImagePickerLaunchers(
        onResult = { picked ->
            if (picked == null || picked.bytes.isEmpty()) {
                println("[fixaverse detect] photo pick cancelled or empty")
                return@rememberImagePickerLaunchers
            }
            store.accept(
                EquipmentSelectionStore.Intent.DetectFromPhoto(
                    imageBytes = picked.bytes,
                    fileName = picked.fileName,
                    contentType = picked.contentType,
                ),
            )
        },
    )

    LaunchedEffect(pendingPhotoSource) {
        when (pendingPhotoSource) {
            null -> return@LaunchedEffect
            PendingPhotoSource.Gallery -> {
                showPhotoSourceDialog = false
                delay(PHOTO_PICKER_DISMISS_DELAY_MS)
                imagePickers.openGallery()
            }
            PendingPhotoSource.Camera -> {
                showPhotoSourceDialog = false
                delay(PHOTO_PICKER_DISMISS_DELAY_MS)
                imagePickers.openCamera()
            }
        }
        pendingPhotoSource = null
    }

    if (showPhotoSourceDialog) {
        FixaversePhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onGallery = { pendingPhotoSource = PendingPhotoSource.Gallery },
            onCamera = { pendingPhotoSource = PendingPhotoSource.Camera },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FixaverseDimens.Space16),
        verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space12),
    ) {
        FixaverseScreenTitle(text = "Выберите оборудование")

        FixaversePrimaryButton(
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

        state.detectError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(FixaverseDimens.Space24),
                contentAlignment = Alignment.Center,
            ) {
                FixaverseLoadingIndicator()
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            FixaverseSecondaryButton(
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
            verticalArrangement = Arrangement.spacedBy(FixaverseDimens.Space8),
        ) {
            items(state.assistants, key = { it.id }) { assistant ->
                FixaverseSelectableCard(
                    title = assistant.name,
                    onClick = { store.accept(EquipmentSelectionStore.Intent.Select(assistant)) },
                )
            }
        }
    }
        FixaverseDetectLoadingOverlay(
            visible = state.isDetecting,
            hint = state.detectProgress,
        )
    }
}

private const val PHOTO_PICKER_DISMISS_DELAY_MS = 150L
