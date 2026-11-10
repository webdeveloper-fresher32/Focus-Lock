package com.ganesh.focuslock.presentation.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.domain.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppsUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isSyncing: Boolean = false
)

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FocusLockApp
    private val appRepo = app.appRepository

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private val allApps = appRepo.getAllApps()

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        allApps,
        _uiState
    ) { apps, state ->
        apps.filter { app ->
            val matchesQuery = state.searchQuery.isEmpty() ||
                    app.appName.contains(state.searchQuery, ignoreCase = true) ||
                    app.packageName.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.selectedCategory == "All" ||
                    app.category.equals(state.selectedCategory, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleAppRestriction(packageName: String, currentRestricted: Boolean) {
        viewModelScope.launch {
            appRepo.updateAppRestriction(packageName, !currentRestricted)
        }
    }

    fun updateDailyLimit(packageName: String, limitMinutes: Int?) {
        viewModelScope.launch {
            appRepo.updateDailyLimit(packageName, limitMinutes)
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            appRepo.syncInstalledApps()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }
}

