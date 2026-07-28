package com.civicsense.shared.grpc

import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.CivicSenseState
import kotlinx.coroutines.flow.Flow

/**
 * Abstract service interface for communicating with the Rust perception pipeline.
 *
 * Each platform (Android / iOS) provides its own implementation:
 *  - Android: uses gRPC-java / grpc-kotlin against the pipeline gRPC server.
 *  - iOS: uses Ktor + gRPC-web or a lightweight HTTP bridge.
 *
 * The shared ViewModel calls this interface — never platform-specific APIs.
 */
interface CivicSenseService {

    /**
     * Server-streaming: subscribe to real-time alerts from the pipeline.
     * Returns a cold Flow that emits [AlertEvent] as they arrive.
     *
     * @param severityFilter optional — only receive alerts of these severities.
     */
    fun subscribeAlerts(severityFilter: List<AlertSeverity>? = null): Flow<AlertEvent>

    /**
     * Unary: fetch the current snapshot of all active detections and system status.
     */
    suspend fun getCurrentState(): CivicSenseState

    /**
     * Unary: health check. Returns true if the pipeline responds.
     */
    suspend fun ping(): Boolean

    /**
     * Connection status of the underlying transport.
     */
    val connectionState: Flow<ConnectionState>
}
