package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import com.example.ui.home.HomeUiState
import com.example.util.AppBlockerHelper
import com.example.util.SessionAlarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class FocusBlockerService : Service() {

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
  private var isRunning = false
  private var timerJob: Job? = null

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

    if (action == ACTION_TOGGLE_PAUSE) {
      handleTogglePause()
      return START_STICKY
    }

    val prefs = AppPreferences(applicationContext)
    val state = prefs.loadUiState(HomeUiState())

    startForegroundNotification(state.remainingSeconds, state.isSessionPaused)

    if (!isRunning) {
      isRunning = true
      startMonitoringForegroundApp()
      startTimerLoop()
    }

    return START_STICKY
  }

  private fun handleTogglePause() {
    val prefs = AppPreferences(applicationContext)
    val newPauseState = prefs.toggleSessionPaused()
    val state = prefs.loadUiState(HomeUiState())

    if (newPauseState) {
      SessionAlarmManager.cancelSessionCompletion(applicationContext)
    } else {
      SessionAlarmManager.scheduleSessionCompletion(applicationContext, state.remainingSeconds)
    }

    // Broadcast change so ViewModel updates
    val broadcastIntent = Intent(ACTION_SESSION_STATE_CHANGED).apply {
      setPackage(packageName)
    }
    applicationContext.sendBroadcast(broadcastIntent)

    updateNotification(state.remainingSeconds, newPauseState)
  }

  private fun startForegroundNotification(remainingSeconds: Int, isPaused: Boolean) {
    createNotificationChannel()

    val notification = buildLockscreenNotification(remainingSeconds, isPaused)

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

  private fun updateNotification(remainingSeconds: Int, isPaused: Boolean) {
    val notificationManager = getSystemService(NotificationManager::class.java)
    val notification = buildLockscreenNotification(remainingSeconds, isPaused)
    notificationManager?.notify(NOTIFICATION_ID, notification)
  }

  private fun buildLockscreenNotification(remainingSeconds: Int, isPaused: Boolean): android.app.Notification {
    val intent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    val statusText = if (isPaused) "Session paused • $formattedTime remaining" else "Focus session active • $formattedTime remaining"
    val notificationIcon = R.drawable.ic_stat_notification

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(notificationIcon)
      .setContentTitle("Lock In")
      .setContentText(statusText)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setContentIntent(contentPendingIntent)
      .build()
  }

  private fun startTimerLoop() {
    timerJob?.cancel()
    timerJob = serviceScope.launch {
      while (isActive && isRunning) {
        delay(1000L)
        val prefs = AppPreferences(applicationContext)
        val state = prefs.loadUiState(HomeUiState())

        if (!state.isSessionActive) {
          stopForeground(STOP_FOREGROUND_REMOVE)
          stopSelf()
          break
        }

        if (!state.isSessionPaused) {
          if (state.remainingSeconds > 0) {
            val newRem = maxOf(0, state.remainingSeconds - 1)
            prefs.updateRemainingSeconds(newRem)
            updateNotification(newRem, false)

            if (newRem <= 0) {
              prefs.completeSessionInPrefs(state.effectiveDurationMinutes)
              stopForeground(STOP_FOREGROUND_REMOVE)
              stopSelf()
              break
            }
          }
        } else {
          updateNotification(state.remainingSeconds, true)
        }
      }
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
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Active Focus Session background service"
        setShowBadge(false)
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
    const val ACTION_TOGGLE_PAUSE = "ACTION_TOGGLE_PAUSE_FOCUS_SESSION"
    const val ACTION_SESSION_STATE_CHANGED = "com.example.action.SESSION_STATE_CHANGED"

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

    fun notifyStateChanged(context: Context) {
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
  }
}
