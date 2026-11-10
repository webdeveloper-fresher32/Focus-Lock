package com.ganesh.focuslock.presentation.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.focuslock.core.theme.*
import com.ganesh.focuslock.core.utils.TimeUtils
import com.ganesh.focuslock.domain.model.FocusSession
import com.ganesh.focuslock.domain.model.StrictnessLevel
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(
    viewModel: FocusViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    if (activeSession != null && !activeSession!!.isFinished) {
        ActiveFocusSessionView(
            session = activeSession!!,
            remainingMs = state.remainingMs,
            progress = state.progressFraction,
            onEmergencyExit = { viewModel.cancelSessionEmergency(activeSession!!.id) }
        )
    } else {
        FocusSetupView(
            state = state,
            onNameChange = { viewModel.updateSessionName(it) },
            onDurationChange = { viewModel.updateDuration(it) },
            onStrictnessChange = { viewModel.updateStrictness(it) },
            onStartSession = { viewModel.startFocusSession() }
        )
    }
}

@Composable
fun FocusSetupView(
    state: FocusSetupUiState,
    onNameChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onStrictnessChange: (StrictnessLevel) -> Unit,
    onStartSession: () -> Unit
) {
    val interviewGoals = listOf("Interview Preparation", "LeetCode Sprint", "System Design Study", "Mock Interview", "DSA Practice")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column {
            Text(
                text = "Focus Session",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Lock out distractions and conquer your study goals.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        // Session Goal Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SESSION FOCUS GOAL",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
            OutlinedTextField(
                value = state.sessionName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Preset Goal Quick Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(interviewGoals) { goal ->
                    SuggestionChip(
                        onClick = { onNameChange(goal) },
                        label = { Text(goal, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        border = null
                    )
                }
            }
        }

        // Duration Presets & Manual Custom Selection
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SESSION DURATION",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = TimeUtils.formatMinutes(state.durationMinutes),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Emerald400,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Quick Duration Chips (Minutes and Hours)
            val quickPresets = listOf(
                25 to "25m",
                45 to "45m",
                60 to "1 hr",
                90 to "1.5h",
                120 to "2 hrs",
                180 to "3 hrs"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPresets) { (mins, label) ->
                    val isSelected = state.durationMinutes == mins
                    Card(
                        modifier = Modifier
                            .clickable { onDurationChange(mins) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Emerald500 else DarkSurface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSelected) Emerald400 else DarkBorder
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkBackground else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Manual Hours & Minutes Custom Picker
            val hours = state.durationMinutes / 60
            val minutes = state.durationMinutes % 60

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "MANUAL CUSTOM SELECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hours Stepper
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "HOURS",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        if (hours > 0) {
                                            val newTotal = (hours - 1) * 60 + minutes
                                            onDurationChange(maxOf(5, newTotal))
                                        }
                                    },
                                    enabled = hours > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease hours",
                                        tint = if (hours > 0) TextPrimary else TextMuted
                                    )
                                }

                                Text(
                                    text = "$hours hr${if (hours != 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (hours < 12) {
                                            val newTotal = (hours + 1) * 60 + minutes
                                            onDurationChange(newTotal)
                                        }
                                    },
                                    enabled = hours < 12,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase hours",
                                        tint = if (hours < 12) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }

                        // Minutes Stepper
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MINUTES",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        val newMinutes = (minutes - 15).let { if (it < 0) 45 else it }
                                        val newHours = if (minutes - 15 < 0) maxOf(0, hours - 1) else hours
                                        val total = newHours * 60 + newMinutes
                                        onDurationChange(maxOf(5, total))
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease minutes",
                                        tint = TextPrimary
                                    )
                                }

                                Text(
                                    text = "$minutes min",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        val newMinutes = (minutes + 15) % 60
                                        val newHours = if (minutes + 15 >= 60) hours + 1 else hours
                                        val total = newHours * 60 + newMinutes
                                        onDurationChange(minOf(720, maxOf(5, total)))
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase minutes",
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Quick Add Adjusters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "+15m" to 15,
                            "+30m" to 30,
                            "+1 hr" to 60,
                            "+2 hrs" to 120
                        ).forEach { (label, delta) ->
                            SuggestionChip(
                                onClick = {
                                    val newTotal = minOf(720, state.durationMinutes + delta)
                                    onDurationChange(newTotal)
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(12.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = Emerald400
                                ),
                                border = null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Strictness Selector
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "RESTRICTION STRICTNESS",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )

            StrictnessLevel.entries.forEach { level ->
                val isSelected = state.strictnessLevel == level
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStrictnessChange(level) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) Emerald500 else DarkBorder
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onStrictnessChange(level) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Emerald500,
                                    unselectedColor = TextMuted
                                )
                            )
                            Text(
                                text = level.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Emerald400 else TextPrimary
                                )
                            )
                        }
                        Text(
                            text = level.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            modifier = Modifier.padding(start = 40.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Start Session CTA
        Button(
            onClick = onStartSession,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald500,
                contentColor = DarkBackground
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Lock & Begin ${TimeUtils.formatMinutes(state.durationMinutes)} Session",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ActiveFocusSessionView(
    session: FocusSession,
    remainingMs: Long,
    progress: Float,
    onEmergencyExit: () -> Unit
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Strictness Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Emerald500.copy(alpha = 0.15f))
                    .border(1.dp, Emerald500, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${session.strictnessLevel.title.uppercase()} MODE ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Emerald400,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Session Title
            Text(
                text = session.sessionName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            )

            // Circular Progress Indicator & Countdown
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Emerald500,
                    strokeWidth = 14.dp,
                    trackColor = DarkSurfaceVariant
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = TimeUtils.formatCountdown(remainingMs),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 42.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "REMAINING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Distraction Deflected Counter
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = Emerald400
                    )
                    Text(
                        text = "${session.distractionsBlockedCount} Distractions Deflected",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Exit Trigger
            OutlinedButton(
                onClick = { showEmergencyDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Crimson400),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Crimson500.copy(alpha = 0.5f)))
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Emergency Exit Session")
            }
        }
    }

    if (showEmergencyDialog) {
        EmergencyExitDialog(
            strictness = session.strictnessLevel,
            onDismiss = { showEmergencyDialog = false },
            onConfirmedExit = {
                showEmergencyDialog = false
                onEmergencyExit()
            }
        )
    }
}

@Composable
fun EmergencyExitDialog(
    strictness: StrictnessLevel,
    onDismiss: () -> Unit,
    onConfirmedExit: () -> Unit
) {
    var countdown by remember { mutableStateOf(if (strictness == StrictnessLevel.MAXIMUM) 60 else 10) }
    var userPledge by remember { mutableStateOf("") }
    val expectedPledge = "I am interrupting my interview prep"

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Emergency Exit", color = Crimson400, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Are you sure you want to break this session? Consistent discipline is key to passing competitive technical interviews.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                if (strictness == StrictnessLevel.MAXIMUM) {
                    Text(
                        text = "Maximum Strictness Friction: Type the sentence below to proceed:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Amber500, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "\"$expectedPledge\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    OutlinedTextField(
                        value = userPledge,
                        onValueChange = { userPledge = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type here...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson500,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                if (countdown > 0) {
                    Text(
                        text = "Unlocking exit in $countdown seconds...",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }
        },
        confirmButton = {
            val canExit = countdown == 0 && (strictness != StrictnessLevel.MAXIMUM || userPledge.trim() == expectedPledge)
            Button(
                onClick = onConfirmedExit,
                enabled = canExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Crimson500,
                    disabledContainerColor = DarkSurfaceVariant
                )
            ) {
                Text("Confirm Break", color = if (canExit) TextPrimary else TextMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Focusing", color = Emerald400)
            }
        },
        containerColor = DarkSurface
    )
}

