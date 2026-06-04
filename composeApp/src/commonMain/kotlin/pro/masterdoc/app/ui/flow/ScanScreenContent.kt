package pro.masterdoc.app.ui.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import com.arkivanov.mvikotlin.extensions.coroutines.states
import pro.masterdoc.app.platform.ScanCameraHeroButton
import pro.masterdoc.app.platform.ScanScreenCameraBindings
import pro.masterdoc.app.platform.rememberImagePickerLaunchers
import pro.masterdoc.app.ui.theme.LiteAppHead
import pro.masterdoc.app.ui.theme.LiteChip
import pro.masterdoc.app.ui.theme.MasterdocDimens
import pro.masterdoc.app.ui.theme.MasterdocDetectLoadingOverlay
import pro.masterdoc.app.ui.theme.MasterdocLoadingIndicator
import pro.masterdoc.app.ui.theme.MasterdocMonoLabel
import pro.masterdoc.app.ui.theme.MasterdocSecondaryButton
import pro.masterdoc.app.ui.theme.MasterdocSelectableCard
import pro.masterdoc.app.ui.theme.masterdocConvoBackground
import pro.masterdoc.presentation.equipment.EquipmentSelectionStore
import pro.masterdoc.presentation.root.RootComponent

@Composable
fun ScanScreenContent(
    root: RootComponent,
    equipmentStore: EquipmentSelectionStore,
    modifier: Modifier = Modifier,
) {
    val menu = rememberLiteFlowMenuState(root)
    val state by equipmentStore.states.collectAsState(initial = equipmentStore.state)
    var showEquipmentList by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val imagePickers = rememberImagePickerLaunchers(
        onResult = { picked ->
            if (picked == null || picked.bytes.isEmpty()) return@rememberImagePickerLaunchers
            cameraError = null
            equipmentStore.accept(
                EquipmentSelectionStore.Intent.DetectFromPhoto(
                    imageBytes = picked.bytes,
                    fileName = picked.fileName,
                    contentType = picked.contentType,
                ),
            )
        },
        onCameraError = { message ->
            if (message == "cancelled") return@rememberImagePickerLaunchers
            cameraError = message
        },
    )

    var lastNavigatedId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.selectedAssistant?.id) {
        val id = state.selectedAssistant?.id
        if (id != null && id != lastNavigatedId) {
            lastNavigatedId = id
            showEquipmentList = false
            root.onEquipmentReady()
        }
        if (id == null) {
            lastNavigatedId = null
        }
    }

    LiteFlowDropdownMenu(menu)

    ScanScreenCameraBindings(
        launchers = imagePickers,
        active = !showEquipmentList,
        onResult = { picked ->
            if (picked == null || picked.bytes.isEmpty()) return@ScanScreenCameraBindings
            cameraError = null
            equipmentStore.accept(
                EquipmentSelectionStore.Intent.DetectFromPhoto(
                    imageBytes = picked.bytes,
                    fileName = picked.fileName,
                    contentType = picked.contentType,
                ),
            )
        },
        onCameraError = { message ->
            if (message == "cancelled") return@ScanScreenCameraBindings
            cameraError = message
        },
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showEquipmentList) {
                EquipmentListPane(
                    state = state,
                    equipmentStore = equipmentStore,
                    onBack = { showEquipmentList = false },
                    onMenuClick = menu.onOpen,
                )
            } else {
                LiteAppHead(
                    title = "Masterdoc",
                    subtitle = "Скан · выбор станции",
                    onMenuClick = menu.onOpen,
                )
                ScanMainPane(
                    state = state,
                    cameraError = cameraError,
                    onCameraClick = {
                        equipmentStore.accept(EquipmentSelectionStore.Intent.ClearDetectError)
                        cameraError = null
                        imagePickers.openCamera()
                    },
                    onListClick = { showEquipmentList = true },
                )
            }
        }
        MasterdocDetectLoadingOverlay(visible = state.isDetecting)
    }
}

@Composable
internal fun ScanMainPane(
    state: EquipmentSelectionStore.State,
    cameraError: String?,
    onCameraClick: () -> Unit,
    onListClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .masterdocConvoBackground(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = MasterdocDimens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ScanCameraHeroButton(
                onClick = onCameraClick,
                enabled = !state.isLoading && !state.isDetecting,
                isLoading = false,
            )

            Spacer(modifier = Modifier.height(MasterdocDimens.Space20))

            Text(
                text = "Сканировать QR или шильдик",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Нажмите, чтобы открыть камеру",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = MasterdocDimens.Space8),
            )

            state.selectedAssistant?.let { assistant ->
                Spacer(modifier = Modifier.height(MasterdocDimens.Space16))
                LiteChip(text = "Найдено · ${assistant.name}", flare = true)
            }

            state.detectError?.let { error ->
                Spacer(modifier = Modifier.height(MasterdocDimens.Space12))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            cameraError?.let { error ->
                Spacer(modifier = Modifier.height(MasterdocDimens.Space12))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MasterdocDimens.Space24,
                    vertical = MasterdocDimens.Space20,
                ),
            verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space12),
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    MasterdocLoadingIndicator()
                }
            }

            MasterdocSecondaryButton(
                text = "Список оборудования",
                onClick = onListClick,
                fillMaxWidth = true,
            )

            MasterdocMonoLabel(
                text = "или выберите станцию вручную",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EquipmentListPane(
    state: EquipmentSelectionStore.State,
    equipmentStore: EquipmentSelectionStore,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MasterdocDimens.Space16),
    ) {
        LiteAppHead(
            title = "Оборудование",
            subtitle = "Выберите станцию",
            onBack = onBack,
            onMenuClick = onMenuClick,
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(MasterdocDimens.Space24),
                contentAlignment = Alignment.Center,
            ) {
                MasterdocLoadingIndicator()
            }
        }

        state.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
            MasterdocSecondaryButton(
                text = "Повторить",
                onClick = { equipmentStore.accept(EquipmentSelectionStore.Intent.RetryLoad) },
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MasterdocDimens.Space8),
        ) {
            items(state.assistants, key = { it.id }) { assistant ->
                MasterdocSelectableCard(
                    title = assistant.name,
                    onClick = {
                        equipmentStore.accept(EquipmentSelectionStore.Intent.Select(assistant))
                    },
                )
            }
        }
    }
}
