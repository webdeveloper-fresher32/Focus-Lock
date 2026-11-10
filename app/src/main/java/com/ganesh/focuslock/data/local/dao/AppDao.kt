package com.ganesh.focuslock.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ganesh.focuslock.data.local.entity.RestrictedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM restricted_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<RestrictedAppEntity>>

    @Query("SELECT packageName FROM restricted_apps WHERE isRestrictedInFocus = 1")
    fun getRestrictedPackageNames(): Flow<List<String>>

    @Query("SELECT packageName FROM restricted_apps WHERE isRestrictedInFocus = 1")
    suspend fun getRestrictedPackageNamesSync(): List<String>

    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): RestrictedAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: RestrictedAppEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(apps: List<RestrictedAppEntity>)

    @Query("UPDATE restricted_apps SET isRestrictedInFocus = :isRestricted WHERE packageName = :packageName")
    suspend fun updateRestriction(packageName: String, isRestricted: Boolean)

    @Query("UPDATE restricted_apps SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun updateDailyLimit(packageName: String, limitMinutes: Int?)

    @Query("UPDATE restricted_apps SET isHardBlocked = :isHardBlocked WHERE packageName = :packageName")
    suspend fun updateHardBlock(packageName: String, isHardBlocked: Boolean)
}

