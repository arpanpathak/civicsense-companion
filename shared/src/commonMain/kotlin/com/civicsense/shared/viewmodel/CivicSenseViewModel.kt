package com.civicsense.shared.viewmodel

import com.civicsense.shared.grpc.CivicSenseService
import com.civicsense.shared.grpc.ConnectionState
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.SystemStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Shared ViewModel used by both Jetpack Compose (Android) and SwiftUI (iOS).
 *
 * Consumed via:
 *  - Android: integrated into androidx.lifecycle.ViewModel via a factory.
 *  - iOS: constructed directly from Swift and observed via Kotlin StateFlows.
 */
class CivicSenseViewModel(
    private val service: CivicSenseService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    // ── Connection state ─────────────────────────────────

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── Alert log (most recent 200 alerts) ───────────────

    private val _alertLog = MutableStateFlow<List<AlertEvent>>(emptyList())
    val alertLog: StateFlow<List<AlertEvent>> = _alertLog.asStateFlow()

    // ── Active alerts (for dashboard badge / list) ───────

    private val _activeAlerts = MutableStateFlow<List<ActiveAlert>>(emptyList())
    val activeAlerts: StateFlow<List<ActiveAlert>> = _activeAlerts.asStateFlow()

    // ── System status ────────────────────────────────────

    private val _systemStatus = MutableStateFlow<SystemStatus?>(null)
    val systemStatus: StateFlow<SystemStatus?> = _systemStatus.asStateFlow()

    // ── Derived: critical alert count ────────────────────

    val criticalAlertCount = _activeAlerts.map { alerts ->
        alerts.count { it.severity == AlertSeverity.CRITICAL }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    // ── Init: wire up external connection state ──────────

    init {
        scope.launch {
            service.connectionState.collect { state ->
                _connectionState.value = state
            }
        }
    }

    // ── Public API ───────────────────────────────────────

    /**
     * Start listening to real-time alerts from the Rust pipeline.
     * Automatically reconnects on transient failures.
     */
    fun connect() {
        scope.launch {
            service.subscribeAlerts()
                .retry(Long.MAX_VALUE) { cause ->
                    _connectionState.value = ConnectionState.Disconnected(cause.message)
                    kotlinx.coroutines.delay(3.seconds)
                    _connectionState.value = ConnectionState.Connecting
                    true // always retry
                }
                .catch { e ->
                    _connectionState.value = ConnectionState.Error(
                        message = e.message ?: "Unknown error",
                        throwable = e
                    )
                }
                .collect { event ->
                    _connectionState.value = ConnectionState.Connected
                    appendAlert(event)
                    updateActiveStatus(event)
                }
        }

        // Also start a periodic state poll for dashboard refresh.
        scope.launch { pollStatePeriodically() }
    }

    /**
     * Manually refresh the full state snapshot.
     */
    suspend fun refreshState(): CivicSenseState? {
        return try {
            val state = service.getCurrentState()
            _activeAlerts.value = state.activeAlerts
            _systemStatus.value = state.systemStatus
            state
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(
                message = "Failed to refresh state: ${e.message}",
                throwable = e
            )
            null
        }
    }

    /**
     * Health check ping.
     */
    suspend fun isPipelineAlive(): Boolean {
        return try {
            service.ping()
        } catch (_: Exception) {
            false
        }
    }

    // ── Internals ────────────────────────────────────────

    private fun appendAlert(event: AlertEvent) {
        val current = _alertLog.value.toMutableList()
        current.add(0, event) // newest first
        if (current.size > 200) current.removeAt(current.lastIndex)
        _alertLog.value = current
    }

    private fun updateActiveStatus(event: AlertEvent) {
        val current = _activeAlerts.value.toMutableList()
        if (event.active) {
            // Add or update
            val idx = current.indexOfFirst { it.id == event.id }
            val activeAlert = ActiveAlert(
                id = event.id,
                category = event.category,
                severity = event.severity,
                title = event.title,
                triggeredAtMs = event.timestampMs
            )
            if (idx >= 0) current[idx] = activeAlert else current.add(activeAlert)
        } else {
            current.removeAll { it.id == event.id }
        }
        _activeAlerts.value = current
    }

    private suspend fun pollStatePeriodically() {
        while (true) {
            kotlinx.coroutines.delay(5.seconds)
            try {
                val state = service.getCurrentState()
                _activeAlerts.value = state.activeAlerts
                _systemStatus.value = state.systemStatus
            } catch (_: Exception) {
                // silent — connectivity loss is handled by the alert stream
            }
        }
    }
}
