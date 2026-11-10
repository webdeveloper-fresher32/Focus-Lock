package com.ganesh.focuslock.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Apps : Screen("apps", "App Limits", Icons.Default.Apps)
    object Focus : Screen("focus", "Focus Mode", Icons.Default.Timer)
    object Stats : Screen("stats", "Analytics", Icons.Default.BarChart)
    object Permissions : Screen("permissions", "Permissions")
}

