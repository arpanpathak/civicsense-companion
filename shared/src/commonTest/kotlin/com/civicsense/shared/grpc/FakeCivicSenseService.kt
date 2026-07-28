package com.civicsense.shared.grpc

import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.CivicSenseState
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Deterministic fake implementation of [CivicSenseService] for unit tests.
 *
 * Allows tests to:
 * - Emit synthetic alerts via [emitAlert]
 * - Control connection state via [setConnectionState]
 * - Pre-set state snapshot via [setStateResponse]
 * - Simulate failures via [setPingResponse]
 */
class FakeCivicSenseService : CivicSenseService {

    // ── Connection state ─────────────────────────────────

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    // ── Alert stream ─────────────────────────────────────

    private var alertCounter = 0
    private val _alertStream = MutableSharedFlow<AlertEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Emit a synthetic alert that subscribers will receive via [subscribeAlerts].
     */
    fun emitAlert(
        id: String = "test-${++alertCounter}",
        severity: AlertSeverity = AlertSeverity.WARNING,
        category: AlertCategory = AlertCategory.STOP_SIGN_VIOLATION,
        title: String = "Test Alert",
        description: String = "Test description",
        active: Boolean = true,
        distanceM: Float? = null,
        egoSpeedKmh: Float? = null
    ) {
        _alertStream.tryEmit(
            AlertEvent(
                id = id,
                timestampMs = 0L,
                severity = severity,
                category = category,
                title = title,
                description = description,
                active = active,
                distanceM = distanceM,
                egoSpeedKmh = egoSpeedKmh
            )
        )
    }

    override fun subscribeAlerts(severityFilter: List<AlertSeverity>?): Flow<AlertEvent> {
        return _alertStream.asSharedFlow()
    }

    // ── State snapshot ───────────────────────────────────

    private var stateResponse: CivicSenseState = CivicSenseState(
        activeAlerts = emptyList(),
        systemStatus = SystemStatus(
            state = PipelineState.RUNNING,
            fps = 30f,
            uptimeMs = 60000L
        )
    )

    fun setStateResponse(state: CivicSenseState) {
        stateResponse = state
    }

    override suspend fun getCurrentState(): CivicSenseState = stateResponse

    // ── Ping / health ────────────────────────────────────

    private var pingResponse = true

    fun setPingResponse(ok: Boolean) {
        pingResponse = ok
    }

    override suspend fun ping(): Boolean = pingResponse
}
