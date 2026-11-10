package com.ganesh.focuslock.presentation.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.focuslock.core.theme.*
import com.ganesh.focuslock.core.utils.PermissionHelper

@Composable
fun PermissionGuideScreen(
    onAllGranted: () -> Unit
) {
    val context = LocalContext.current

    var hasUsage by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.canDrawOverlays(context)) }
    var hasBattery by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }

    // Re-check on resume / button clicks
    fun refresh() {
        hasUsage = PermissionHelper.hasUsageStatsPermission(context)
        hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
        hasOverlay = PermissionHelper.canDrawOverlays(context)
        hasBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "Setup Permissions",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "FocusLock needs these standard Android permissions to enforce distraction blocking and monitor screen time.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        // 1. Usage Stats Permission
        PermissionItemCard(
            title = "Usage Access",
            description = "Calculates your daily screen time and checks if distracting apps exceeded daily limits.",
            icon = Icons.Default.QueryStats,
            isGranted = hasUsage,
            onGrantClick = {
                PermissionHelper.openUsageStatsSettings(context)
            }
        )

        // 2. Accessibility Service
        PermissionItemCard(
            title = "Accessibility Protection",
            description = "Detects when a blocked application is opened during an active focus block so FocusLock can intercept it.",
            icon = Icons.Default.Accessibility,
            isGranted = hasAccessibility,
            onGrantClick = {
                PermissionHelper.openAccessibilitySettings(context)
            }
        )

        // 3. Draw Over Other Apps
        PermissionItemCard(
            title = "Display Over Other Apps",
            description = "Allows FocusLock to display the blocking screen immediately over restricted applications.",
            icon = Icons.Default.Layers,
            isGranted = hasOverlay,
            onGrantClick = {
                PermissionHelper.openOverlaySettings(context)
            }
        )

        // 4. Battery Optimization Exemption
        PermissionItemCard(
            title = "Ignore Battery Optimizations",
            description = "Prevents aggressive Android OEM battery savers from killing the active focus countdown.",
            icon = Icons.Default.BatteryAlert,
            isGranted = hasBattery,
            onGrantClick = {
                PermissionHelper.requestIgnoreBatteryOptimizations(context)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                refresh()
                if (hasUsage && hasAccessibility && hasOverlay) {
                    onAllGranted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasUsage && hasAccessibility && hasOverlay) Emerald500 else DarkSurfaceVariant,
                contentColor = if (hasUsage && hasAccessibility && hasOverlay) DarkBackground else TextMuted
            )
        ) {
            Text(
                text = if (hasUsage && hasAccessibility && hasOverlay) "All Set — Return to Dashboard" else "Re-check Granted Permissions",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isGranted) Emerald500.copy(alpha = 0.5f) else DarkBorder
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isGranted) Emerald500.copy(alpha = 0.2f) else DarkSurfaceVariant, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Emerald400 else TextSecondary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isGranted) "Granted ✓" else "Not Granted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isGranted) Emerald400 else Amber500,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (!isGranted) {
                    FilledTonalButton(
                        onClick = onGrantClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Emerald500,
                            contentColor = DarkBackground
                        )
                    ) {
                        Text("Grant", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Granted",
                        tint = Emerald400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

