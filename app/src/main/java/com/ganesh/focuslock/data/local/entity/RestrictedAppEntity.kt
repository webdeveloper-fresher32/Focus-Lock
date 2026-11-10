package com.ganesh.focuslock.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restricted_apps")
data class RestrictedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String,
    val isRestrictedInFocus: Boolean,
    val dailyLimitMinutes: Int?,
    val isHardBlocked: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

