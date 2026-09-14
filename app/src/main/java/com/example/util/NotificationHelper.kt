package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.AppPreferences

object NotificationHelper {

  private const val CHANNEL_ID = "lock_in_channel"
  private const val CHANNEL_NAME = "Lock In Notifications"
  private const val CHANNEL_DESC = "Notifications for Lock In app"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = CHANNEL_DESC
      }
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun isSystemPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    }
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
  }

  fun openNotificationSettings(context: Context) {
    try {
      val intent = android.content.Intent().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
          putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
          action = "android.settings.APP_NOTIFICATION_SETTINGS"
          putExtra("app_package", context.packageName)
          putExtra("app_uid", context.applicationInfo.uid)
        }
        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun postSystemNotification(
    context: Context,
    title: String,
    message: String,
    notificationId: Int = System.currentTimeMillis().toInt()
  ) {
    createNotificationChannel(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
      flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = android.app.PendingIntent.getActivity(
      context,
      0,
      intent,
      android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val iconRes = com.example.R.drawable.ic_stat_notification

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(iconRes)
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)

    try {
      NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun showFirstLaunchNotification(context: Context, prefs: AppPreferences): Boolean {
    postSystemNotification(context, "Welcome to Lock In", "Take back control of your time.", 1001)
    prefs.setFirstLaunchCompleted()
    return true
  }

  fun showAccountCreationNotification(context: Context) {
    postSystemNotification(context, "You're all set", "Your Lock In account is ready.", 1002)
  }

  fun showLoginNotification(context: Context) {
    postSystemNotification(context, "Welcome back", "Ready to lock in?", 1003)
  }

  const val NOTIFICATION_ID_INACTIVITY = 1004

  fun showInactivityReminderNotification(
    context: Context,
    title: String = "Ready to Lock In?",
    message: String = "Your next focus session is waiting."
  ) {
    postSystemNotification(context, title, message, NOTIFICATION_ID_INACTIVITY)
  }
}
