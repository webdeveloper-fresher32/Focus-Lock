package com.ganesh.focuslock.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val category: String = "Other",
    val isRestrictedInFocus: Boolean = true,
    val dailyLimitMinutes: Int? = null,
    val isHardBlocked: Boolean = false,
    val screenTimeTodayMs: Long = 0L
)

