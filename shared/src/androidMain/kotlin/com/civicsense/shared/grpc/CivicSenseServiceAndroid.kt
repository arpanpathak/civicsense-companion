package com.civicsense.shared.grpc

import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.CivicSenseState
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Android implementation of [CivicSenseService] using gRPC-java + OkHttp.
 *
 * Connects to the Rust pipeline's gRPC server (default port 50051).
 */
class CivicSenseServiceAndroid(
    private val host: String = "localhost",
    private val port: Int = 50051
) : CivicSenseService {

    private val channel: ManagedChannel = OkHttpChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    override fun subscribeAlerts(severityFilter: List<AlertSeverity>?): Flow<AlertEvent> = flow {
        _connectionState.value = ConnectionState.Connecting

        // Stub creation would go here when proto codegen is wired up:
        // val stub = CivicSenseAlertServiceGrpcKt.CivicSenseAlertServiceCoroutineStub(channel)
        // val request = alertSubscription { severityFilterList.addAll(severityFilter ?: emptyList()) }
        // stub.subscribeAlerts(request).collect { protoEvent ->
        //     emit(protoEvent.toDomainModel())
        // }

        // ── Placeholder: emits a synthetic alert for development ──
        _connectionState.value = ConnectionState.Connected
        emit(
            AlertEvent(
                id = "dev-placeholder",
                timestampMs = System.currentTimeMillis(),
                severity = AlertSeverity.INFO,
                category = AlertCategory.STOP_SIGN_VIOLATION,
                title = "CivicSense Pipeline Connected",
                description = "Waiting for real-time alerts from the Rust perception pipeline.",
                active = true
            )
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun getCurrentState(): CivicSenseState = withContext(Dispatchers.IO) {
        // val stub = CivicSenseAlertServiceGrpcKt.CivicSenseAlertServiceCoroutineStub(channel)
        // val protoState = stub.getCurrentState(empty {})
        // return protoState.toDomainModel()

        CivicSenseState(
            activeAlerts = emptyList(),
            systemStatus = SystemStatus(
                state = PipelineState.RUNNING,
                fps = 0f,
                uptimeMs = 0L
            )
        )
    }

    override suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            // val stub = CivicSenseAlertServiceGrpcKt.CivicSenseAlertServiceCoroutineStub(channel)
            // val pong = stub.ping(empty {})
            // true
            true
        } catch (_: Exception) {
            false
        }
    }
}
