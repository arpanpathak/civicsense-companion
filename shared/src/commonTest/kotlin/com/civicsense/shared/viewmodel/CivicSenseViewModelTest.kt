package com.civicsense.shared.viewmodel

import com.civicsense.shared.grpc.ConnectionState
import com.civicsense.shared.grpc.FakeCivicSenseService
import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.CivicSenseState
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CivicSenseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + testDispatcher)

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    // ── Connection state ─────────────────────────────────

    @Test
    fun `viewModel starts in Idle connection state`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)
        assertIs<ConnectionState.Idle>(vm.connectionState.value)
    }

    @Test
    fun `viewModel reflects connection state from service`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        assertEquals(ConnectionState.Idle, vm.connectionState.value)

        service.setConnectionState(ConnectionState.Connecting)
        assertIs<ConnectionState.Connecting>(vm.connectionState.value)
    }

    // ── Alert log ────────────────────────────────────────

    @Test
    fun `alertLog starts empty`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)
        assertTrue(vm.alertLog.value.isEmpty())
    }

    @Test
    fun `alertLog receives alerts after connect`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        service.emitAlert(id = "e1", title = "First", severity = AlertSeverity.CRITICAL)
        service.emitAlert(id = "e2", title = "Second", severity = AlertSeverity.WARNING)

        assertEquals(2, vm.alertLog.value.size)
        assertEquals("e2", vm.alertLog.value[0].id) // newest first
        assertEquals("First", vm.alertLog.value[1].title)
    }

    @Test
    fun `alertLog caps at 200 entries`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        repeat(210) { i ->
            service.emitAlert(id = "e$i", title = "Alert $i")
        }

        assertEquals(200, vm.alertLog.value.size)
        assertEquals("e209", vm.alertLog.value[0].id) // newest
    }

    // ── Active alerts ────────────────────────────────────

    @Test
    fun `activeAlerts adds when alert active is true`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        service.emitAlert(id = "a1", title = "Active Alert", active = true)

        assertEquals(1, vm.activeAlerts.value.size)
        assertEquals("a1", vm.activeAlerts.value[0].id)
    }

    @Test
    fun `activeAlerts removes when alert active becomes false`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        service.emitAlert(id = "a1", title = "Active", active = true)
        assertEquals(1, vm.activeAlerts.value.size)

        service.emitAlert(id = "a1", title = "Resolved", active = false)
        assertTrue(vm.activeAlerts.value.isEmpty())
    }

    @Test
    fun `activeAlerts updates existing alert on re-emission`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        service.emitAlert(id = "a1", title = "First title", active = true)
        assertEquals("First title", vm.activeAlerts.value[0].title)

        service.emitAlert(id = "a1", title = "Updated title", active = true)
        assertEquals(1, vm.activeAlerts.value.size)
        assertEquals("Updated title", vm.activeAlerts.value[0].title)
    }

    // ── Critical alert count ─────────────────────────────

    @Test
    fun `criticalAlertCount is zero initially`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)
        assertEquals(0, vm.criticalAlertCount.value)
    }

    @Test
    fun `criticalAlertCount counts only critical alerts`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        vm.connect()

        service.emitAlert(id = "c1", severity = AlertSeverity.CRITICAL, active = true)
        service.emitAlert(id = "w1", severity = AlertSeverity.WARNING, active = true)
        service.emitAlert(id = "c2", severity = AlertSeverity.CRITICAL, active = true)
        service.emitAlert(id = "i1", severity = AlertSeverity.INFO, active = true)

        assertEquals(2, vm.criticalAlertCount.value)
    }

    // ── System status ────────────────────────────────────

    @Test
    fun `systemStatus is null initially`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)
        assertNull(vm.systemStatus.value)
    }

    @Test
    fun `refreshState updates systemStatus`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        service.setStateResponse(
            CivicSenseState(
                activeAlerts = emptyList(),
                systemStatus = SystemStatus(
                    state = PipelineState.RUNNING,
                    fps = 30f,
                    uptimeMs = 50000L
                )
            )
        )

        val result = vm.refreshState()
        assertNotNull(result)
        assertEquals(PipelineState.RUNNING, vm.systemStatus.value?.state)
        assertEquals(30f, vm.systemStatus.value?.fps)
    }

    @Test
    fun `refreshState updates activeAlerts from server state`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        service.setStateResponse(
            CivicSenseState(
                activeAlerts = listOf(
                    ActiveAlert("s1", AlertCategory.ROAD_HAZARD, AlertSeverity.CRITICAL, "Hazard", 100L)
                ),
                systemStatus = SystemStatus(PipelineState.RUNNING, 30f, 1000L)
            )
        )

        vm.refreshState()
        assertEquals(1, vm.activeAlerts.value.size)
        assertEquals("s1", vm.activeAlerts.value[0].id)
    }

    // ── Ping / health ────────────────────────────────────

    @Test
    fun `isPipelineAlive returns true when pipeline responds`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)
        assertTrue(vm.isPipelineAlive())
    }

    @Test
    fun `isPipelineAlive returns false when ping fails`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        service.setPingResponse(false)
        val vm = CivicSenseViewModel(service, scope)
        assertFalse(vm.isPipelineAlive())
    }

    // ── Connection state flow from service ────────────────

    @Test
    fun `viewModel reflects Disconnected and Error states`() = runTest(testDispatcher) {
        val service = FakeCivicSenseService()
        val vm = CivicSenseViewModel(service, scope)

        service.setConnectionState(ConnectionState.Disconnected("Wi-Fi off"))
        assertEquals("Wi-Fi off", (vm.connectionState.value as ConnectionState.Disconnected).reason)

        service.setConnectionState(ConnectionState.Error("Fatal"))
        assertEquals("Fatal", (vm.connectionState.value as ConnectionState.Error).message)
    }
}
