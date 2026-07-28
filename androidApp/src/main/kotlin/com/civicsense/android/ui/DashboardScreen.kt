package com.civicsense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.civicsense.shared.grpc.ConnectionState
import com.civicsense.shared.model.ActiveAlert
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.model.PipelineState
import com.civicsense.shared.model.SystemStatus
import com.civicsense.shared.viewmodel.CivicSenseViewModel

@Composable
fun DashboardScreen(
    viewModel: CivicSenseViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status card
        StatusCard(
            connectionState = connectionState,
            systemStatus = systemStatus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active alerts summary
        AlertSummaryCard(activeAlerts = activeAlerts)

        Spacer(modifier = Modifier.height(16.dp))

        // Recent alerts header
        Text(
            text = "Active Alerts (${activeAlerts.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activeAlerts.isEmpty()) {
            Text(
                text = "No active alerts — all clear.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeAlerts) { alert ->
                    AlertItem(alert = alert)
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    connectionState: ConnectionState,
    systemStatus: SystemStatus?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (statusColor, statusText) = when (connectionState) {
                    is ConnectionState.Connected -> Color(0xFF00D4AA) to "Connected"
                    is ConnectionState.Connecting -> Color(0xFFFFC107) to "Connecting..."
                    is ConnectionState.Disconnected -> Color(0xFFFF5252) to "Disconnected"
                    is ConnectionState.Error -> Color(0xFFFF5252) to "Error"
                    is ConnectionState.Idle -> Color(0xFF9E9E9E) to "Idle"
                }

                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pipeline: $statusText",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            systemStatus?.let { status ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatusChip(label = "FPS", value = String.format("%.1f", status.fps))
                    StatusChip(label = "Uptime", value = formatUptime(status.uptimeMs))
                    StatusChip(
                        label = "State",
                        value = status.state.name.lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                }
                if (status.state == PipelineState.ERROR && status.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AlertSummaryCard(activeAlerts: List<ActiveAlert>) {
    val criticalCount = activeAlerts.count { it.severity == AlertSeverity.CRITICAL }
    val warningCount = activeAlerts.count { it.severity == AlertSeverity.WARNING }
    val infoCount = activeAlerts.count { it.severity == AlertSeverity.INFO }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryBadge(
                label = "Critical",
                count = criticalCount,
                color = Color(0xFFFF5252)
            )
            SummaryBadge(
                label = "Warning",
                count = warningCount,
                color = Color(0xFFFFC107)
            )
            SummaryBadge(
                label = "Info",
                count = infoCount,
                color = Color(0xFF00D4AA)
            )
        }
    }
}

@Composable
private fun SummaryBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AlertItem(alert: ActiveAlert) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFF5252)
        AlertSeverity.WARNING -> Color(0xFFFFC107)
        AlertSeverity.INFO -> Color(0xFF00D4AA)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = alert.category.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun formatUptime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m ${seconds}s"
}
