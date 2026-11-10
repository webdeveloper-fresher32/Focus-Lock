package com.ganesh.focuslock

import android.app.Application
import com.ganesh.focuslock.data.local.FocusLockDatabase
import com.ganesh.focuslock.data.preferences.FocusPreferences
import com.ganesh.focuslock.data.repository.AppRepositoryImpl
import com.ganesh.focuslock.data.repository.SessionRepositoryImpl
import com.ganesh.focuslock.data.repository.UsageRepositoryImpl
import com.ganesh.focuslock.domain.repository.AppRepository
import com.ganesh.focuslock.domain.repository.SessionRepository
import com.ganesh.focuslock.domain.repository.UsageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FocusLockApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { FocusLockDatabase.getInstance(this) }
    val preferences by lazy { FocusPreferences(this) }

    val appRepository: AppRepository by lazy {
        AppRepositoryImpl(this, database.appDao())
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(database.focusSessionDao())
    }

    val usageRepository: UsageRepository by lazy {
        UsageRepositoryImpl(this, database.appDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Synchronize installed apps in the background
        applicationScope.launch {
            appRepository.syncInstalledApps()
        }
    }
}

