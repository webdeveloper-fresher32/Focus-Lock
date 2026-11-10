package com.ganesh.focuslock.presentation.blocker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.focuslock.R
import com.ganesh.focuslock.core.theme.*
import com.ganesh.focuslock.core.utils.TimeUtils
import com.ganesh.focuslock.domain.model.StrictnessLevel
import kotlinx.coroutines.delay

@Composable
fun BlockScreen(
    restrictedPackage: String,
    reason: String,
    sessionName: String,
    targetEndMs: Long,
    strictness: StrictnessLevel,
    onReturnHome: () -> Unit,
    onOpenFocusLock: () -> Unit,
    onEmergencyCancel: () -> Unit
) {
    var remainingMs by remember { mutableStateOf(maxOf(0L, targetEndMs - System.currentTimeMillis())) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(targetEndMs) {
        while (remainingMs > 0L) {
            delay(1000L)
            remainingMs = maxOf(0L, targetEndMs - System.currentTimeMillis())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo Shield
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FocusLock Shield",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            // Title & App Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "FOCUSLOCK BLOCKED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Crimson400,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                )

                Text(
                    text = "Distraction Prevented",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Text(
                    text = "App: $restrictedPackage",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    maxLines = 1
                )
            }

            // Reason & Session Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    )

                    if (targetEndMs > 0L) {
                        Text(
                            text = "${TimeUtils.formatCountdown(remainingMs)} remaining",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald400
                            )
                        )
                    }

                    Text(
                        text = "“Consistency is what transforms average into excellence. Stay focused on your interview preparation.”",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Return Action
            Button(
                onClick = onReturnHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = DarkBackground
                )
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Return to Home / Study",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // View FocusLock Dashboard
            OutlinedButton(
                onClick = onOpenFocusLock,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Text("Open FocusLock")
            }

            // Emergency Exit (only if mode allows)
            if (strictness == StrictnessLevel.NORMAL) {
                TextButton(onClick = onEmergencyCancel) {
                    Text("Cancel Focus Session", color = Crimson400)
                }
            } else if (strictness == StrictnessLevel.MAXIMUM) {
                TextButton(onClick = { showEmergencyDialog = true }) {
                    Text("Emergency Exit (Maximum Friction)", color = TextMuted)
                }
            }
        }
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Emergency Exit", color = Crimson400, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Maximum strictness requires exiting from the FocusLock app dashboard after solving an intentional friction delay.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = onOpenFocusLock) {
                    Text("Go to Dashboard", color = Emerald400)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Back", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

