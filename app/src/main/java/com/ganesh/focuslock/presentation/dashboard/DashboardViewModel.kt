package com.ganesh.focuslock.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.core.utils.PermissionHelper
import com.ganesh.focuslock.domain.model.FocusSession
import com.ganesh.focuslock.domain.model.StrictnessLevel
import com.ganesh.focuslock.domain.repository.DailyUsageSummary
import com.ganesh.focuslock.service.FocusSessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val usageSummary: DailyUsageSummary = DailyUsageSummary(0L, emptyList(), emptyList()),
    val activeSession: FocusSession? = null,
    val hasUsageAccess: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasOverlay: Boolean = false,
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val sessionRepo = app.sessionRepository
    private val usageRepo = app.usageRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val activeSession = sessionRepo.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        checkPermissions()
        observeUsage()
        observeSession()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            hasUsageAccess = PermissionHelper.hasUsageStatsPermission(context),
            hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context),
            hasOverlay = PermissionHelper.canDrawOverlays(context)
        )
    }

    private fun observeUsage() {
        viewModelScope.launch {
            usageRepo.getTodayUsageSummary().collect { summary ->
                _uiState.value = _uiState.value.copy(
                    usageSummary = summary,
                    isLoading = false
                )
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            activeSession.collect { session ->
                _uiState.value = _uiState.value.copy(activeSession = session)
            }
        }
    }

    fun quickStartInterviewPrep(durationMinutes: Int = 45) {
        viewModelScope.launch {
            val sessionId = sessionRepo.startSession(
                name = "Interview Preparation",
                durationMinutes = durationMinutes,
                strictness = StrictnessLevel.STRICT
            )
            if (sessionId > 0) {
                FocusSessionService.startService(getApplication())
            }
        }
    }
}

