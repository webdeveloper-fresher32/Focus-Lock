package com.ganesh.focuslock.presentation.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.domain.model.FocusSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalFocusMinutes: Int = 0,
    val totalDistractionsBlocked: Int = 0,
    val recentSessions: List<FocusSession> = emptyList(),
    val completionRate: Int = 100
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val sessionRepo = app.sessionRepository

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            sessionRepo.getTotalFocusMinutes().collect { minutes ->
                _uiState.value = _uiState.value.copy(totalFocusMinutes = minutes)
            }
        }

        viewModelScope.launch {
            sessionRepo.getTotalDistractionsBlocked().collect { blocked ->
                _uiState.value = _uiState.value.copy(totalDistractionsBlocked = blocked)
            }
        }

        viewModelScope.launch {
            sessionRepo.getAllSessions().collect { sessions ->
                val total = sessions.size
                val completed = sessions.count { it.sessionStatus.name == "COMPLETED" }
                val rate = if (total > 0) (completed * 100) / total else 100
                _uiState.value = _uiState.value.copy(
                    recentSessions = sessions.take(15),
                    completionRate = rate
                )
            }
        }
    }
}

