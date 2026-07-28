package com.civicsense.shared.grpc

/**
 * Tracks the connectivity state between the companion app and the Rust pipeline.
 */
sealed class ConnectionState {
    /** Initial state; no connection attempt has been made. */
    data object Idle : ConnectionState()

    /** Actively connecting to the pipeline gRPC server. */
    data object Connecting : ConnectionState()

    /** Connected and subscribing to alerts. */
    data object Connected : ConnectionState()

    /** Connection lost; will auto-reconnect. */
    data class Disconnected(val reason: String?) : ConnectionState()

    /** Fatal error; manual intervention needed. */
    data class Error(val message: String, val throwable: Throwable? = null) : ConnectionState()
}
