package com.civicsense.shared.grpc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionStateTest {

    @Test
    fun `Idle is the default initial state`() {
        val state = ConnectionState.Idle
        assertTrue(state is ConnectionState.Idle)
    }

    @Test
    fun `Connecting state has no extra data`() {
        val state = ConnectionState.Connecting
        assertTrue(state is ConnectionState.Connecting)
    }

    @Test
    fun `Connected state has no extra data`() {
        val state = ConnectionState.Connected
        assertTrue(state is ConnectionState.Connected)
    }

    @Test
    fun `Disconnected state carries optional reason`() {
        val stateWithReason = ConnectionState.Disconnected("Connection timeout")
        assertTrue(stateWithReason is ConnectionState.Disconnected)
        assertEquals("Connection timeout", stateWithReason.reason)

        val stateWithoutReason = ConnectionState.Disconnected(null)
        assertNull(stateWithoutReason.reason)
    }

    @Test
    fun `Error state carries message and optional throwable`() {
        val cause = RuntimeException("Pipeline unavailable")
        val state = ConnectionState.Error("Failed to connect", cause)

        assertTrue(state is ConnectionState.Error)
        assertEquals("Failed to connect", state.message)
        assertNotNull(state.throwable)
        assertEquals("Pipeline unavailable", state.throwable?.message)
    }

    @Test
    fun `Error state without throwable`() {
        val state = ConnectionState.Error("Unknown failure")
        assertEquals("Unknown failure", state.message)
        assertNull(state.throwable)
    }

    @Test
    fun `All sealed subclasses are covered`() {
        val states = listOf(
            ConnectionState.Idle,
            ConnectionState.Connecting,
            ConnectionState.Connected,
            ConnectionState.Disconnected("test"),
            ConnectionState.Error("test")
        )
        assertEquals(5, states.size)
    }
}
