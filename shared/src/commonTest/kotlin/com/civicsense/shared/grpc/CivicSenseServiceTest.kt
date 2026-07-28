package com.civicsense.shared.grpc

import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.CivicSenseState
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CivicSenseServiceTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `FakeCivicSenseService starts in Connected state`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val state = service.connectionState.first()
        assertIs<ConnectionState.Connected>(state)
    }

    @Test
    fun `FakeCivicSenseService can change connection state`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()

        service.setConnectionState(ConnectionState.Connecting)
        var state = service.connectionState.first()
        assertIs<ConnectionState.Connecting>(state)

        service.setConnectionState(ConnectionState.Disconnected("Network lost"))
        state = service.connectionState.first()
        assertIs<ConnectionState.Disconnected>(state)
        assertEquals("Network lost", (state as ConnectionState.Disconnected).reason)
    }

    @Test
    fun `subscribeAlerts emits alerts via emitAlert`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()

        val collected = mutableListOf<com.civicsense.shared.model.AlertEvent>()
        val collectJob = launch {
            service.subscribeAlerts().collect { collected.add(it) }
        }

        service.emitAlert(id = "a1", title = "Alert One")
        service.emitAlert(id = "a2", title = "Alert Two")

        collectJob.cancel()

        assertEquals(2, collected.size)
        assertEquals("a1", collected[0].id)
        assertEquals("a2", collected[1].id)
        assertEquals("Alert One", collected[0].title)
        assertEquals("Alert Two", collected[1].title)
    }

    @Test
    fun `getCurrentState returns pre-configured state`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val state = service.getCurrentState()

        assertIs<CivicSenseState>(state)
        assertTrue(state.activeAlerts.isEmpty())
        assertEquals(PipelineState.RUNNING, state.systemStatus.state)
        assertEquals(30f, state.systemStatus.fps)
    }

    @Test
    fun `getCurrentState returns custom state after setStateResponse`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()

        val customState = CivicSenseState(
            activeAlerts = listOf(
                com.civicsense.shared.model.ActiveAlert(
                    id = "custom-1",
                    category = AlertCategory.ROAD_HAZARD,
                    severity = AlertSeverity.CRITICAL,
                    title = "Debris on road",
                    triggeredAtMs = 100L
                )
            ),
            systemStatus = SystemStatus(
                state = PipelineState.ERROR,
                fps = 0f,
                uptimeMs = 5000L,
                errorMessage = "ONNX model load failed"
            )
        )
        service.setStateResponse(customState)

        val result = service.getCurrentState()
        assertEquals(1, result.activeAlerts.size)
        assertEquals("custom-1", result.activeAlerts[0].id)
        assertEquals(PipelineState.ERROR, result.systemStatus.state)
        assertEquals("ONNX model load failed", result.systemStatus.errorMessage)
    }

    @Test
    fun `ping returns true by default`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        assertTrue(service.ping())
    }

    @Test
    fun `ping returns false after setPingResponse disabled`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        service.setPingResponse(false)
        assertFalse(service.ping())
    }
}
