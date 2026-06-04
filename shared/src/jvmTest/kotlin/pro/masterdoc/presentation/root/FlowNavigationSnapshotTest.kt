package pro.masterdoc.presentation.root

import kotlin.test.Test
import kotlin.test.assertEquals

class FlowNavigationSnapshotTest {

    @Test
    fun sanitizedStack_withoutAssistant_fallsBackToScan() {
        val snapshot = FlowNavigationSnapshot(
            stack = listOf(FlowConfig.ChatDescribe),
            assistantId = null,
        )
        assertEquals(listOf(FlowConfig.Scan), snapshot.sanitizedStack())
    }

    @Test
    fun sanitizedStack_withAssistant_keepsStack() {
        val snapshot = FlowNavigationSnapshot(
            stack = listOf(FlowConfig.Scan, FlowConfig.Summary),
            assistantId = 2,
            assistantName = "Стиралки",
        )
        assertEquals(listOf(FlowConfig.Scan, FlowConfig.Summary), snapshot.sanitizedStack())
    }

    @Test
    fun sanitizedStack_cameraWithoutAssistant_isAllowed() {
        val snapshot = FlowNavigationSnapshot(
            stack = listOf(FlowConfig.Scan, FlowConfig.Camera),
        )
        assertEquals(listOf(FlowConfig.Scan, FlowConfig.Camera), snapshot.sanitizedStack())
    }
}
