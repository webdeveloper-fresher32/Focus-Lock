package com.ganesh.focuslock.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ganesh.focuslock.presentation.apps.AppsScreen
import com.ganesh.focuslock.presentation.apps.AppsViewModel
import com.ganesh.focuslock.presentation.dashboard.DashboardScreen
import com.ganesh.focuslock.presentation.dashboard.DashboardViewModel
import com.ganesh.focuslock.presentation.focus.FocusScreen
import com.ganesh.focuslock.presentation.focus.FocusViewModel
import com.ganesh.focuslock.presentation.permissions.PermissionGuideScreen
import com.ganesh.focuslock.presentation.stats.StatsScreen
import com.ganesh.focuslock.presentation.stats.StatsViewModel

@Composable
fun FocusLockNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Apps.route) {
            val viewModel: AppsViewModel = viewModel()
            AppsScreen(viewModel = viewModel)
        }

        composable(Screen.Focus.route) {
            val viewModel: FocusViewModel = viewModel()
            FocusScreen(viewModel = viewModel)
        }

        composable(Screen.Stats.route) {
            val viewModel: StatsViewModel = viewModel()
            StatsScreen(viewModel = viewModel)
        }

        composable(Screen.Permissions.route) {
            PermissionGuideScreen(
                onAllGranted = {
                    navController.popBackStack()
                }
            )
        }
    }
}

