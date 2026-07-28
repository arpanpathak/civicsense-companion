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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.civicsense.shared.model.AlertCategory
import com.civicsense.shared.model.AlertEvent
import com.civicsense.shared.model.AlertSeverity
import com.civicsense.shared.viewmodel.CivicSenseViewModel

@Composable
fun AlertListScreen(
    viewModel: CivicSenseViewModel,
    modifier: Modifier = Modifier
) {
    val alertLog by viewModel.alertLog.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Alert Log (${alertLog.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (alertLog.isEmpty()) {
            Text(
                text = "No alerts yet. Connect to the pipeline to receive real-time alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(alertLog) { alert ->
                    AlertLogItem(alert = alert)
                }
            }
        }
    }
}

@Composable
private fun AlertLogItem(alert: AlertEvent) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFF5252)
        AlertSeverity.WARNING -> Color(0xFFFFC107)
        AlertSeverity.INFO -> Color(0xFF00D4AA)
    }

    val categoryIcon = when (alert.category) {
        AlertCategory.STOP_SIGN_VIOLATION -> Icons.Default.Stop
        AlertCategory.SPEED_WARNING -> Icons.Default.Speed
        AlertCategory.NO_TURN_SIGNAL,
        AlertCategory.LATE_TURN_SIGNAL,
        AlertCategory.MULTI_LANE_CUT -> Icons.Default.DirectionsCar
        AlertCategory.INTERSECTION_OCCUPANCY,
        AlertCategory.LEFT_LANE_CAMPING -> Icons.Default.PedalBike
        else -> Icons.Default.Warning
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
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!alert.active) {
                        Text(
                            text = "RESOLVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    alert.distanceM?.let { d ->
                        DetailChip(
                            icon = Icons.Default.Speed,
                            text = String.format("%.0f m", d)
                        )
                    }
                    alert.egoSpeedKmh?.let { s ->
                        DetailChip(
                            icon = Icons.Default.AccessTime,
                            text = String.format("%.0f km/h", s)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
