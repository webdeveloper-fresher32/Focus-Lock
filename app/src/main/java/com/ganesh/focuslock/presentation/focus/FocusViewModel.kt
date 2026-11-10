package com.ganesh.focuslock.presentation.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.domain.model.FocusSession
import com.ganesh.focuslock.domain.model.SessionStatus
import com.ganesh.focuslock.domain.model.StrictnessLevel
import com.ganesh.focuslock.service.FocusSessionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class FocusSetupUiState(
    val sessionName: String = "Interview Preparation",
    val durationMinutes: Int = 45,
    val strictnessLevel: StrictnessLevel = StrictnessLevel.STRICT,
    val remainingMs: Long = 0L,
    val progressFraction: Float = 0f
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val sessionRepo = app.sessionRepository

    private val _uiState = MutableStateFlow(FocusSetupUiState())
    val uiState: StateFlow<FocusSetupUiState> = _uiState.asStateFlow()

    val activeSession: StateFlow<FocusSession?> = sessionRepo.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        startLiveTimer()
    }

    private fun startLiveTimer() {
        viewModelScope.launch {
            while (isActive) {
                val current = activeSession.value
                if (current != null && current.sessionStatus == SessionStatus.RUNNING) {
                    val remaining = current.remainingTimeMs
                    _uiState.value = _uiState.value.copy(
                        remainingMs = remaining,
                        progressFraction = current.progressFraction
                    )
                    if (remaining <= 0L) {
                        sessionRepo.endSession(current.id, SessionStatus.COMPLETED)
                        FocusSessionService.stopService(getApplication())
                    }
                }
                delay(1000L)
            }
        }
    }

    fun updateSessionName(name: String) {
        _uiState.value = _uiState.value.copy(sessionName = name)
    }

    fun updateDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = minutes)
    }

    fun updateStrictness(level: StrictnessLevel) {
        _uiState.value = _uiState.value.copy(strictnessLevel = level)
    }

    fun startFocusSession() {
        val currentState = _uiState.value
        viewModelScope.launch {
            val sessionId = sessionRepo.startSession(
                name = currentState.sessionName.ifBlank { "Deep Focus" },
                durationMinutes = currentState.durationMinutes,
                strictness = currentState.strictnessLevel
            )
            if (sessionId > 0) {
                FocusSessionService.startService(getApplication())
            }
        }
    }

    fun cancelSessionEmergency(sessionId: Long) {
        viewModelScope.launch {
            sessionRepo.endSession(sessionId, SessionStatus.CANCELLED_EMERGENCY)
            FocusSessionService.stopService(getApplication())
        }
    }
}

