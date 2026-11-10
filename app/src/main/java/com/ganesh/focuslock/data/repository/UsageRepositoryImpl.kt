package com.ganesh.focuslock.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import com.ganesh.focuslock.core.utils.TimeUtils
import com.ganesh.focuslock.data.local.dao.AppDao
import com.ganesh.focuslock.domain.model.AppInfo
import com.ganesh.focuslock.domain.repository.DailyUsageSummary
import com.ganesh.focuslock.domain.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UsageRepositoryImpl(
    private val context: Context,
    private val appDao: AppDao
) : UsageRepository {

    override fun getTodayUsageSummary(): Flow<DailyUsageSummary> = flow {
        while (true) {
            val summary = calculateTodaySummary()
            emit(summary)
            // Periodic refresh every 30 seconds
            delay(30_000L)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAppUsageToday(packageName: String): Long {
        return withContext(Dispatchers.IO) {
            val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@withContext 0L
            val startOfDay = TimeUtils.getStartOfDayEpochMs()
            val now = System.currentTimeMillis()

            val stats = usageManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                now
            ) ?: return@withContext 0L

            stats.filter { it.packageName == packageName }
                .sumOf { it.totalTimeInForeground }
        }
    }

    override suspend fun isAppExceedingDailyLimit(packageName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val app = appDao.getApp(packageName) ?: return@withContext false
            val limitMinutes = app.dailyLimitMinutes ?: return@withContext false
            if (limitMinutes <= 0) return@withContext false

            val usedMillis = getAppUsageToday(packageName)
            val limitMillis = limitMinutes * 60 * 1000L
            usedMillis >= limitMillis
        }
    }

    private suspend fun calculateTodaySummary(): DailyUsageSummary {
        val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return DailyUsageSummary(0L, emptyList(), emptyList())

        val startOfDay = TimeUtils.getStartOfDayEpochMs()
        val now = System.currentTimeMillis()

        val rawStats = usageManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: emptyList()

        val pm = context.packageManager
        val myPackage = context.packageName

        // Aggregate by package
        val packageTimeMap = mutableMapOf<String, Long>()
        for (stat in rawStats) {
            val current = packageTimeMap.getOrDefault(stat.packageName, 0L)
            packageTimeMap[stat.packageName] = current + stat.totalTimeInForeground
        }

        val totalScreenTime = packageTimeMap.values.sum()

        // Filter meaningful apps (used > 0 and has launcher intent)
        val appList = mutableListOf<AppInfo>()
        val exceedingList = mutableListOf<AppInfo>()

        for ((pkg, timeMs) in packageTimeMap) {
            if (pkg == myPackage || timeMs < 60_000L) continue // ignore under 1 minute or self

            val appEntity = appDao.getApp(pkg)
            val appName = try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                appEntity?.appName ?: pkg
            }

            val app = AppInfo(
                packageName = pkg,
                appName = appName,
                category = appEntity?.category ?: "Other",
                isRestrictedInFocus = appEntity?.isRestrictedInFocus ?: true,
                dailyLimitMinutes = appEntity?.dailyLimitMinutes,
                isHardBlocked = appEntity?.isHardBlocked ?: false,
                screenTimeTodayMs = timeMs
            )

            appList.add(app)

            val limitMinutes = app.dailyLimitMinutes
            if (limitMinutes != null && limitMinutes > 0) {
                val limitMs = limitMinutes * 60 * 1000L
                if (timeMs >= limitMs) {
                    exceedingList.add(app)
                }
            }
        }

        val sortedTopApps = appList.sortedByDescending { it.screenTimeTodayMs }.take(10)

        return DailyUsageSummary(
            totalScreenTimeMs = totalScreenTime,
            topApps = sortedTopApps,
            appsExceedingLimit = exceedingList
        )
    }
}

