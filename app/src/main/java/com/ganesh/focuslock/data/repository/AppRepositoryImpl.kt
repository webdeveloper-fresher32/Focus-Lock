package com.ganesh.focuslock.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.ganesh.focuslock.data.local.dao.AppDao
import com.ganesh.focuslock.data.local.entity.RestrictedAppEntity
import com.ganesh.focuslock.domain.model.AppInfo
import com.ganesh.focuslock.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val context: Context,
    private val appDao: AppDao
) : AppRepository {

    override fun getAllApps(): Flow<List<AppInfo>> {
        return appDao.getAllApps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRestrictedPackageNames(): Flow<List<String>> {
        return appDao.getRestrictedPackageNames()
    }

    override suspend fun getRestrictedPackageNamesSync(): List<String> {
        return withContext(Dispatchers.IO) {
            appDao.getRestrictedPackageNamesSync()
        }
    }

    override suspend fun updateAppRestriction(packageName: String, isRestricted: Boolean) {
        withContext(Dispatchers.IO) {
            appDao.updateRestriction(packageName, isRestricted)
        }
    }

    override suspend fun updateDailyLimit(packageName: String, limitMinutes: Int?) {
        withContext(Dispatchers.IO) {
            appDao.updateDailyLimit(packageName, limitMinutes)
        }
    }

    override suspend fun updateHardBlock(packageName: String, isHardBlocked: Boolean) {
        withContext(Dispatchers.IO) {
            appDao.updateHardBlock(packageName, isHardBlocked)
        }
    }

    override suspend fun syncInstalledApps() {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val myPackage = context.packageName

            val entities = resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                // Skip FocusLock and system essential dialers
                if (pkgName == myPackage || isSystemEssential(pkgName)) {
                    null
                } else {
                    val appName = resolveInfo.loadLabel(pm).toString()
                    val category = categorizeApp(pkgName, resolveInfo.activityInfo.applicationInfo)
                    val isDistractingByDefault = isDistractingApp(pkgName, category)

                    RestrictedAppEntity(
                        packageName = pkgName,
                        appName = appName,
                        category = category,
                        isRestrictedInFocus = isDistractingByDefault,
                        dailyLimitMinutes = null, // IMPORTANT: Never set daily limits automatically
                        isHardBlocked = false
                    )
                }
            }

            if (entities.isNotEmpty()) {
                appDao.insertAllIgnore(entities)
            }

            // Clean up any stale 45-minute daily limits from previous versions
            appDao.resetDefaultDailyLimits()
        }
    }

    private fun isSystemEssential(pkg: String): Boolean {
        return pkg.contains("dialer") ||
                pkg.contains("telecom") ||
                pkg.contains("emergency") ||
                pkg.contains("settings") ||
                pkg == "android" ||
                pkg == "com.android.systemui"
    }

    private fun categorizeApp(pkg: String, appInfo: ApplicationInfo): String {
        val lowerPkg = pkg.lowercase()
        return when {
            lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
            lowerPkg.contains("twitter") || lowerPkg.contains("reddit") ||
            lowerPkg.contains("snapchat") || lowerPkg.contains("tiktok") ||
            lowerPkg.contains("threads") || lowerPkg.contains("linkedin") -> "Social"

            lowerPkg.contains("youtube") || lowerPkg.contains("netflix") ||
            lowerPkg.contains("spotify") || lowerPkg.contains("primevideo") ||
            lowerPkg.contains("twitch") || lowerPkg.contains("disney") -> "Entertainment"

            lowerPkg.contains("game") || lowerPkg.contains("pubg") ||
            lowerPkg.contains("freefire") || lowerPkg.contains("clash") ||
            lowerPkg.contains("candycrush") || (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0 -> "Games"

            lowerPkg.contains("amazon") || lowerPkg.contains("flipkart") ||
            lowerPkg.contains("ebay") || lowerPkg.contains("myntra") -> "Shopping"

            lowerPkg.contains("leetcode") || lowerPkg.contains("hackerrank") ||
            lowerPkg.contains("duolingo") || lowerPkg.contains("udemy") ||
            lowerPkg.contains("coursera") || lowerPkg.contains("github") -> "Productivity"

            else -> "Other"
        }
    }

    private fun isDistractingApp(pkg: String, category: String): Boolean {
        return category in listOf("Social", "Entertainment", "Games", "Shopping")
    }

    private fun RestrictedAppEntity.toDomain() = AppInfo(
        packageName = packageName,
        appName = appName,
        category = category,
        isRestrictedInFocus = isRestrictedInFocus,
        dailyLimitMinutes = dailyLimitMinutes,
        isHardBlocked = isHardBlocked
    )
}
