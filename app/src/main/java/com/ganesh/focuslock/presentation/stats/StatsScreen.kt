package com.ganesh.focuslock.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.focuslock.core.theme.*
import com.ganesh.focuslock.core.utils.TimeUtils
import com.ganesh.focuslock.domain.model.FocusSession

@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Productivity Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Track your discipline and interview prep consistency.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // 3 Key Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Focus Time",
                    value = "${state.totalFocusMinutes / 60}h ${state.totalFocusMinutes % 60}m",
                    icon = Icons.Default.HourglassTop,
                    accentColor = Emerald400,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Deflected",
                    value = "${state.totalDistractionsBlocked}",
                    icon = Icons.Default.Shield,
                    accentColor = Indigo500,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Completion",
                    value = "${state.completionRate}%",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Amber500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Sessions Header
        item {
            Text(
                text = "Session History",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (state.recentSessions.isEmpty()) {
            item {
                Text(
                    text = "No focus sessions recorded yet. Start your first session from the Focus tab!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            items(state.recentSessions) { session ->
                SessionHistoryCard(session = session)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = TextPrimary)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
        }
    }
}

@Composable
fun SessionHistoryCard(session: FocusSession) {
    val isCompleted = session.sessionStatus.name == "COMPLETED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.sessionName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${TimeUtils.getReadableDate(session.startTimeEpochMs)} • ${session.durationMinutes} mins",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isCompleted) "Completed" else "Interrupted",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Emerald400 else Crimson400
                    )
                )
                Text(
                    text = "${session.distractionsBlockedCount} blocked",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

