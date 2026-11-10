package com.ganesh.focuslock.domain.repository

import com.ganesh.focuslock.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

data class DailyUsageSummary(
    val totalScreenTimeMs: Long,
    val topApps: List<AppInfo>,
    val appsExceedingLimit: List<AppInfo>
)

interface UsageRepository {
    fun getTodayUsageSummary(): Flow<DailyUsageSummary>
    suspend fun getAppUsageToday(packageName: String): Long
    suspend fun isAppExceedingDailyLimit(packageName: String): Boolean
}

