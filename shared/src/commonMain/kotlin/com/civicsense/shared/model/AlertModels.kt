package com.civicsense.shared.model

import kotlinx.serialization.Serializable

/**
 * Shared domain models for CivicSense alerts.
 * These mirror the protobuf definitions at proto/civicsense.proto
 * but are Kotlin-native for cross-platform use without protobuf codegen.
 */

@Serializable
enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

@Serializable
enum class AlertCategory {
    STOP_SIGN_VIOLATION,
    INTERSECTION_OCCUPANCY,
    LEFT_LANE_CAMPING,
    NO_TURN_SIGNAL,
    LATE_TURN_SIGNAL,
    MULTI_LANE_CUT,
    ROAD_HAZARD,
    EMERGENCY_VEHICLE,
    SPEED_WARNING
}

@Serializable
enum class PipelineState {
    STARTING,
    RUNNING,
    PAUSED,
    ERROR,
    SHUTDOWN
}

/**
 * An alert event emitted by the Rust perception pipeline.
 */
@Serializable
data class AlertEvent(
    val id: String,
    val timestampMs: Long,
    val severity: AlertSeverity,
    val category: AlertCategory,
    val title: String,
    val description: String,
    val active: Boolean,
    val distanceM: Float? = null,
    val egoSpeedKmh: Float? = null
)

/**
 * Lightweight snapshot of a currently-active alert (for dashboard).
 */
@Serializable
data class ActiveAlert(
    val id: String,
    val category: AlertCategory,
    val severity: AlertSeverity,
    val title: String,
    val triggeredAtMs: Long
)

/**
 * Health / status of the Rust perception pipeline.
 */
@Serializable
data class SystemStatus(
    val state: PipelineState,
    val fps: Float,
    val uptimeMs: Long,
    val errorMessage: String? = null
)

/**
 * Full state snapshot returned by GetCurrentState.
 */
@Serializable
data class CivicSenseState(
    val activeAlerts: List<ActiveAlert>,
    val systemStatus: SystemStatus
)
