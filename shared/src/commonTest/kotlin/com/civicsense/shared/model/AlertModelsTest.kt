package com.civicsense.shared.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AlertModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // ── AlertSeverity ────────────────────────────────────

    @Test
    fun `AlertSeverity enum has expected values`() {
        val values = AlertSeverity.entries
        assertEquals(3, values.size)
        assertTrue(values.contains(AlertSeverity.INFO))
        assertTrue(values.contains(AlertSeverity.WARNING))
        assertTrue(values.contains(AlertSeverity.CRITICAL))
    }

    @Test
    fun `AlertSeverity serializes and deserializes`() {
        val jsonStr = json.encodeToString(AlertSeverity.CRITICAL)
        assertContains(jsonStr, "CRITICAL")
        val decoded = json.decodeFromString<AlertSeverity>(jsonStr)
        assertEquals(AlertSeverity.CRITICAL, decoded)
    }

    // ── AlertCategory ────────────────────────────────────

    @Test
    fun `AlertCategory enum has all expected values`() {
        val values = AlertCategory.entries
        assertEquals(9, values.size)
        assertTrue(values.contains(AlertCategory.STOP_SIGN_VIOLATION))
        assertTrue(values.contains(AlertCategory.INTERSECTION_OCCUPANCY))
        assertTrue(values.contains(AlertCategory.LEFT_LANE_CAMPING))
        assertTrue(values.contains(AlertCategory.NO_TURN_SIGNAL))
        assertTrue(values.contains(AlertCategory.LATE_TURN_SIGNAL))
        assertTrue(values.contains(AlertCategory.MULTI_LANE_CUT))
        assertTrue(values.contains(AlertCategory.ROAD_HAZARD))
        assertTrue(values.contains(AlertCategory.EMERGENCY_VEHICLE))
        assertTrue(values.contains(AlertCategory.SPEED_WARNING))
    }

    // ── PipelineState ────────────────────────────────────

    @Test
    fun `PipelineState enum has expected values`() {
        val values = PipelineState.entries
        assertEquals(5, values.size)
        assertTrue(values.contains(PipelineState.STARTING))
        assertTrue(values.contains(PipelineState.RUNNING))
        assertTrue(values.contains(PipelineState.PAUSED))
        assertTrue(values.contains(PipelineState.ERROR))
        assertTrue(values.contains(PipelineState.SHUTDOWN))
    }

    // ── AlertEvent ───────────────────────────────────────

    @Test
    fun `AlertEvent serializes with all fields`() {
        val event = AlertEvent(
            id = "alert-001",
            timestampMs = 1712345678000L,
            severity = AlertSeverity.CRITICAL,
            category = AlertCategory.INTERSECTION_OCCUPANCY,
            title = "Intersection Blocked",
            description = "Do not enter — intersection is gridlocked.",
            active = true,
            distanceM = 15.0f,
            egoSpeedKmh = 45.0f
        )

        val jsonStr = json.encodeToString(event)

        // Verify all fields are present in JSON
        assertContains(jsonStr, "alert-001")
        assertContains(jsonStr, "CRITICAL")
        assertContains(jsonStr, "INTERSECTION_OCCUPANCY")
        assertContains(jsonStr, "Intersection Blocked")
        assertContains(jsonStr, "15.0")
        assertContains(jsonStr, "45.0")

        // Roundtrip
        val decoded = json.decodeFromString<AlertEvent>(jsonStr)
        assertEquals(event.id, decoded.id)
        assertEquals(event.severity, decoded.severity)
        assertEquals(event.category, decoded.category)
        assertEquals(event.title, decoded.title)
        assertEquals(event.description, decoded.description)
        assertEquals(event.active, decoded.active)
        assertEquals(event.distanceM, decoded.distanceM)
        assertEquals(event.egoSpeedKmh, decoded.egoSpeedKmh)
    }

    @Test
    fun `AlertEvent serializes with null optional fields`() {
        val event = AlertEvent(
            id = "alert-002",
            timestampMs = 1712345678000L,
            severity = AlertSeverity.INFO,
            category = AlertCategory.SPEED_WARNING,
            title = "Speed Feedback",
            description = "You're below traffic flow.",
            active = false
        )

        val jsonStr = json.encodeToString(event)
        assertContains(jsonStr, "alert-002")
        assertContains(jsonStr, "false")

        val decoded = json.decodeFromString<AlertEvent>(jsonStr)
        assertEquals("alert-002", decoded.id)
        assertNull(decoded.distanceM)
        assertNull(decoded.egoSpeedKmh)
    }

    // ── ActiveAlert ──────────────────────────────────────

    @Test
    fun `ActiveAlert serializes correctly`() {
        val alert = ActiveAlert(
            id = "aa-001",
            category = AlertCategory.LEFT_LANE_CAMPING,
            severity = AlertSeverity.WARNING,
            title = "Move Right",
            triggeredAtMs = 1712345678000L
        )

        val jsonStr = json.encodeToString(alert)
        assertContains(jsonStr, "aa-001")
        assertContains(jsonStr, "LEFT_LANE_CAMPING")
        assertContains(jsonStr, "WARNING")
        assertContains(jsonStr, "Move Right")

        val decoded = json.decodeFromString<ActiveAlert>(jsonStr)
        assertEquals(alert.id, decoded.id)
        assertEquals(alert.category, decoded.category)
        assertEquals(alert.severity, decoded.severity)
        assertEquals(alert.title, decoded.title)
    }

    // ── SystemStatus ─────────────────────────────────────

    @Test
    fun `SystemStatus serializes with all fields`() {
        val status = SystemStatus(
            state = PipelineState.RUNNING,
            fps = 29.97f,
            uptimeMs = 3600000L,
            errorMessage = null
        )

        val jsonStr = json.encodeToString(status)
        assertContains(jsonStr, "RUNNING")
        assertContains(jsonStr, "29.97")

        val decoded = json.decodeFromString<SystemStatus>(jsonStr)
        assertEquals(PipelineState.RUNNING, decoded.state)
        assertEquals(29.97f, decoded.fps)
        assertEquals(3600000L, decoded.uptimeMs)
        assertNull(decoded.errorMessage)
    }

    @Test
    fun `SystemStatus serializes with error message`() {
        val status = SystemStatus(
            state = PipelineState.ERROR,
            fps = 0f,
            uptimeMs = 5000L,
            errorMessage = "Camera feed lost"
        )

        val jsonStr = json.encodeToString(status)
        assertContains(jsonStr, "ERROR")
        assertContains(jsonStr, "Camera feed lost")

        val decoded = json.decodeFromString<SystemStatus>(jsonStr)
        assertEquals(PipelineState.ERROR, decoded.state)
        assertEquals("Camera feed lost", decoded.errorMessage)
    }

    // ── CivicSenseState ──────────────────────────────────

    @Test
    fun `CivicSenseState serializes with alerts and status`() {
        val state = CivicSenseState(
            activeAlerts = listOf(
                ActiveAlert("a1", AlertCategory.STOP_SIGN_VIOLATION, AlertSeverity.CRITICAL, "Stop!", 100L),
                ActiveAlert("a2", AlertCategory.SPEED_WARNING, AlertSeverity.INFO, "Speed up", 200L)
            ),
            systemStatus = SystemStatus(
                state = PipelineState.RUNNING,
                fps = 30f,
                uptimeMs = 100000L
            )
        )

        val jsonStr = json.encodeToString(state)
        assertContains(jsonStr, "a1")
        assertContains(jsonStr, "a2")
        assertContains(jsonStr, "RUNNING")

        val decoded = json.decodeFromString<CivicSenseState>(jsonStr)
        assertEquals(2, decoded.activeAlerts.size)
        assertEquals("a1", decoded.activeAlerts[0].id)
        assertEquals(PipelineState.RUNNING, decoded.systemStatus.state)
        assertEquals(30f, decoded.systemStatus.fps)
    }

    @Test
    fun `CivicSenseState serializes with empty alerts`() {
        val state = CivicSenseState(
            activeAlerts = emptyList(),
            systemStatus = SystemStatus(
                state = PipelineState.STARTING,
                fps = 0f,
                uptimeMs = 0L
            )
        )

        val jsonStr = json.encodeToString(state)
        assertContains(jsonStr, "STARTING")

        val decoded = json.decodeFromString<CivicSenseState>(jsonStr)
        assertTrue(decoded.activeAlerts.isEmpty())
        assertEquals(PipelineState.STARTING, decoded.systemStatus.state)
    }
}
