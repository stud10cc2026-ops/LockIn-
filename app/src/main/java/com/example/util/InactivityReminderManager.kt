package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppPreferences
import java.util.Calendar

object InactivityReminderManager {

  const val INACTIVITY_INTERVAL_MS = 12L * 60 * 60 * 1000L // 12 hours
  private const val REQUEST_CODE = 2001
  const val ACTION_INACTIVITY_CHECK = "com.example.action.INACTIVITY_REMINDER"

  /**
   * Records a meaningful user activity, resets the inactivity reminder tracker,
   * and schedules the next inactivity check for 12 hours later (daytime adjusted).
   */
  fun recordActivityAndReschedule(context: Context) {
    val prefs = AppPreferences(context)
    val now = System.currentTimeMillis()
    prefs.setLastActivityTimestamp(now)
    // Reset inactivity reminder sent timestamp so a new 12-hour cycle begins
    prefs.setLastInactivityReminderSentTimestamp(0L)

    // Schedule 12 hours from now, adjusted to a reasonable daytime window
    scheduleNextReminder(context, now + INACTIVITY_INTERVAL_MS)
  }

  /**
   * Adjusts the base target time so it falls within a civilized daytime window (9:00 AM - 9:00 PM).
   * If the time falls between 9:00 PM and 9:00 AM, it is pushed forward to 9:00 AM.
   */
  fun calculateDaytimeTriggerTime(baseTime: Long): Long {
    val calendar = Calendar.getInstance().apply {
      timeInMillis = baseTime
    }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)

    when {
      // Early morning before 9:00 AM (0..8) -> advance to 9:00 AM today
      hour < 9 -> {
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
      }
      // Nighttime 9:00 PM or later (21..23) -> advance to 9:00 AM the next day
      hour >= 21 -> {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
      }
      // Daytime 9:00 AM to 8:59 PM (9..20) -> keep as-is
    }
    return calendar.timeInMillis
  }

  fun scheduleNextReminder(context: Context, targetEpochMs: Long) {
    val prefs = AppPreferences(context)
    // Requirement 9: If user has never completed onboarding, do not send this reminder
    if (prefs.isFirstLaunch()) return

    val triggerAtMillis = calculateDaytimeTriggerTime(targetEpochMs)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val intent = Intent(context, InactivityReminderReceiver::class.java).apply {
      action = ACTION_INACTIVITY_CHECK
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      }
    } catch (_: Exception) {
      try {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      } catch (_: Exception) {}
    }
  }

  fun cancelReminder(context: Context) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, InactivityReminderReceiver::class.java).apply {
        action = ACTION_INACTIVITY_CHECK
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
      }
    } catch (_: Exception) {}
  }
}
