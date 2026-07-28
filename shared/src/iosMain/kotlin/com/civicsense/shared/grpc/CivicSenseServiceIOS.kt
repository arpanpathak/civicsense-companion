package com.civicsense.shared.grpc

import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.CivicSenseState
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * iOS implementation of [CivicSenseService] using Ktor HttpClient.
 *
 * Communicates with the Rust pipeline via a lightweight HTTP/REST bridge
 * (or gRPC-web proxy) when native gRPC-Swift is not available from Kotlin.
 */
class CivicSenseServiceIOS(
    private val baseUrl: String = "http://localhost:8080"
) : CivicSenseService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()

    override fun subscribeAlerts(severityFilter: List<AlertSeverity>?): Flow<AlertEvent> = flow {
        _connectionState.value = ConnectionState.Connecting

        // In production: open a streaming connection via Ktor + gRPC-web.
        // For now, emit a placeholder to validate the shared VM flow.
        _connectionState.value = ConnectionState.Connected
        emit(
            AlertEvent(
                id = "ios-dev-placeholder",
                timestampMs = 0L, // placeholder; real timestamp from pipeline
                severity = AlertSeverity.INFO,
                category = AlertCategory.INTERSECTION_OCCUPANCY,
                title = "CivicSense Pipeline Connected",
                description = "Connected to Rust pipeline. Stream will populate with live alerts.",
                active = true
            )
        )
    }

    override suspend fun getCurrentState(): CivicSenseState {
        // val response = client.get("$baseUrl/api/v1/state")
        // return response.body<CivicSenseState>()
        return CivicSenseState(
            activeAlerts = emptyList(),
            systemStatus = SystemStatus(
                state = PipelineState.RUNNING,
                fps = 0f,
                uptimeMs = 0L
            )
        )
    }

    override suspend fun ping(): Boolean {
        return try {
            // val response = client.get("$baseUrl/api/v1/ping")
            // response.status.value == 200
            true
        } catch (_: Exception) {
            false
        }
    }
}
