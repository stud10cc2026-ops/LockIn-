package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.AppBlockerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusBlockerService : Service() {

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
  private var isRunning = false

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action
    if (action == ACTION_STOP) {
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    startForegroundNotification()

    if (!isRunning) {
      isRunning = true
      startMonitoringForegroundApp()
    }

    return START_STICKY
  }

  private fun startForegroundNotification() {
    createNotificationChannel()

    val intent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationIcon = com.example.R.drawable.ic_stat_notification

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Lock In Focus Active")
      .setContentText("Social media & distraction apps are restricted. Stay focused.")
      .setSmallIcon(notificationIcon)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setContentIntent(pendingIntent)
      .build()

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      try {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      } catch (_: Exception) {}
    }
  }

  private var lastForegroundBringTime = 0L

  private fun startMonitoringForegroundApp() {
    serviceScope.launch {
      while (isActive && isRunning) {
        delay(500L)

        val hasUsage = AppBlockerHelper.isUsageStatsPermissionGranted(applicationContext)
        val hasOverlay = AppBlockerHelper.isOverlayPermissionGranted(applicationContext)

        if (!hasUsage || !hasOverlay) {
          continue
        }

        // If Lock In is ALREADY in the foreground, do NOT re-trigger intent to prevent touch input drops
        if (AppBlockerHelper.isLockInInForeground(applicationContext)) {
          continue
        }

        val currentPackage = AppBlockerHelper.getForegroundPackageName(applicationContext)

        if (currentPackage != null) {
          val blockedAppName = AppBlockerHelper.getBlockedAppName(applicationContext, currentPackage)
          if (blockedAppName != null) {
            val now = System.currentTimeMillis()
            if (now - lastForegroundBringTime > 1500L) {
              lastForegroundBringTime = now
              bringLockInToForeground(currentPackage, blockedAppName)
            }
          }
        }
      }
    }
  }

  private fun bringLockInToForeground(packageName: String, blockedAppName: String) {
    try {
      val intent = Intent(applicationContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        putExtra("BLOCKED_PACKAGE_NAME", packageName)
        putExtra("BLOCKED_APP_NAME", blockedAppName)
      }

      val pendingIntent = PendingIntent.getActivity(
        applicationContext,
        (System.currentTimeMillis() % 10000).toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      try {
        pendingIntent.send()
      } catch (_: Exception) {
        applicationContext.startActivity(intent)
      }
      applicationContext.startActivity(intent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Focus Session App Blocker",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Active Focus Session app blocking background service"
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    isRunning = false
    serviceJob.cancel()
    super.onDestroy()
  }

  companion object {
    const val CHANNEL_ID = "focus_blocker_service_channel"
    const val NOTIFICATION_ID = 8891
    const val ACTION_START = "ACTION_START_FOCUS_BLOCKER"
    const val ACTION_STOP = "ACTION_STOP_FOCUS_BLOCKER"

    fun startService(context: Context) {
      val intent = Intent(context, FocusBlockerService::class.java).apply {
        action = ACTION_START
      }
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, FocusBlockerService::class.java).apply {
        action = ACTION_STOP
      }
      try {
        context.stopService(intent)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
