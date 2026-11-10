package com.ganesh.focuslock.presentation.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganesh.focuslock.core.theme.*
import com.ganesh.focuslock.domain.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val apps by viewModel.filteredApps.collectAsState()
    var appForLimitDialog by remember { mutableStateOf<AppInfo?>(null) }

    val categories = listOf("All", "Social", "Entertainment", "Games", "Shopping", "Productivity", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Restricted Apps",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "${apps.count { it.isRestrictedInFocus }} apps blocked during focus",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            IconButton(
                onClick = { viewModel.refreshInstalledApps() },
                enabled = !state.isSyncing
            ) {
                if (state.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Emerald500, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync Apps", tint = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search installed applications...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = state.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurface,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Application List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppRestrictionCard(
                    app = app,
                    onToggleRestriction = { viewModel.toggleAppRestriction(app.packageName, app.isRestrictedInFocus) },
                    onSetLimitClick = { appForLimitDialog = app }
                )
            }
        }
    }

    // Daily Limit Dialog
    appForLimitDialog?.let { targetApp ->
        DailyLimitPickerBottomSheet(
            app = targetApp,
            onDismiss = { appForLimitDialog = null },
            onSelectLimit = { minutes ->
                viewModel.updateDailyLimit(targetApp.packageName, minutes)
                appForLimitDialog = null
            }
        )
    }
}

@Composable
fun AppRestrictionCard(
    app: AppInfo,
    onToggleRestriction: () -> Unit,
    onSetLimitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (app.isRestrictedInFocus) DarkSurface else DarkSurface.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (app.isRestrictedInFocus) Emerald500.copy(alpha = 0.4f) else DarkBorder
            )
        )
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
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "${app.category} • ${app.packageName}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Daily Limit Tag (clickable)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (app.dailyLimitMinutes != null) Amber500.copy(alpha = 0.2f) else DarkSurfaceVariant
                        )
                        .clickable { onSetLimitClick() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (app.dailyLimitMinutes != null) "Daily Limit: ${app.dailyLimitMinutes}m" else "Set Daily Limit",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (app.dailyLimitMinutes != null) Amber500 else TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Switch to restrict during focus
            Switch(
                checked = app.isRestrictedInFocus,
                onCheckedChange = { onToggleRestriction() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = Emerald500,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun DailyLimitPickerBottomSheet(
    app: AppInfo,
    onDismiss: () -> Unit,
    onSelectLimit: (Int?) -> Unit
) {
    val limits = listOf(15, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Daily Limit for ${app.appName}", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FocusLock will block this application once you exceed this time per calendar day.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                limits.forEach { minutes ->
                    TextButton(
                        onClick = { onSelectLimit(minutes) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Emerald400)
                    ) {
                        Text(text = "$minutes minutes / day", fontSize = 16.sp)
                    }
                }
                TextButton(
                    onClick = { onSelectLimit(null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Crimson400)
                ) {
                    Text(text = "No Daily Limit (Disabled)", fontSize = 16.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextPrimary
    )
}

