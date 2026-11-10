package com.ganesh.focuslock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ganesh.focuslock.FocusLockApp
import com.ganesh.focuslock.MainActivity
import com.ganesh.focuslock.core.utils.TimeUtils
import com.ganesh.focuslock.domain.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusSessionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var tickerJob: Job? = null

    companion object {
        const val CHANNEL_ID = "focus_lock_session_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_FOCUS_SESSION"
        const val ACTION_STOP = "ACTION_STOP_FOCUS_SESSION"

        fun startService(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundSession()
                stopSelf()
            }
            else -> {
                startForegroundSession()
            }
        }
        return START_STICKY
    }

    private fun startForegroundSession() {
        val initialNotification = buildNotification("Focus Session Active", "Protecting your interview preparation block...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val app = application as? FocusLockApp ?: return@launch
            val sessionRepo = app.sessionRepository

            while (isActive) {
                val activeSession = sessionRepo.getActiveSessionSync()
                if (activeSession == null || activeSession.sessionStatus != SessionStatus.RUNNING) {
                    stopForegroundSession()
                    stopSelf()
                    break
                }

                val remainingMs = activeSession.remainingTimeMs
                if (remainingMs <= 0L) {
                    sessionRepo.endSession(activeSession.id, SessionStatus.COMPLETED)
                    showCompletionNotification(activeSession.sessionName)
                    stopForegroundSession()
                    stopSelf()
                    break
                }

                val formattedTime = TimeUtils.formatCountdown(remainingMs)
                val notification = buildNotification(
                    title = "Focus: ${activeSession.sessionName}",
                    content = "$formattedTime remaining • Distractions blocked"
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)

                delay(1000L)
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showCompletionNotification(sessionName: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session Complete! 🎉")
            .setContentText("Great job! You stayed focused through $sessionName.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(1002, notification)
    }

    private fun stopForegroundSession() {
        tickerJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FocusLock Active Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live countdown and status of your active focus session."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        super.onDestroy()
    }
}

