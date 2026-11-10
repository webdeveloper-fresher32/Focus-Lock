package com.ganesh.focuslock.domain.repository

import com.ganesh.focuslock.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getAllApps(): Flow<List<AppInfo>>
    fun getRestrictedPackageNames(): Flow<List<String>>
    suspend fun getRestrictedPackageNamesSync(): List<String>
    suspend fun updateAppRestriction(packageName: String, isRestricted: Boolean)
    suspend fun updateDailyLimit(packageName: String, limitMinutes: Int?)
    suspend fun updateHardBlock(packageName: String, isHardBlocked: Boolean)
    suspend fun syncInstalledApps()
}

