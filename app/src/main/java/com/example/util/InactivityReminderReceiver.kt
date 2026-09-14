package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppPreferences
import com.example.ui.notifications.AppNotificationItem
import com.example.ui.notifications.formatRelativeNotificationTime

class InactivityReminderReceiver : BroadcastReceiver() {

  companion object {
    const val REMINDER_TITLE = "Ready to Lock In?"
    const val REMINDER_BODY = "Your next focus session is waiting."
  }

  override fun onReceive(context: Context, intent: Intent?) {
    val prefs = AppPreferences(context)

    // Handle device boot: reschedule based on last activity
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
      if (!prefs.isFirstLaunch()) {
        val lastActivity = prefs.getLastActivityTimestamp()
        if (lastActivity > 0L) {
          InactivityReminderManager.scheduleNextReminder(
            context,
            lastActivity + InactivityReminderManager.INACTIVITY_INTERVAL_MS
          )
        }
      }
      return
    }

    // Requirement 9: If the user has never completed onboarding, do not send this reminder.
    if (prefs.isFirstLaunch()) {
      return
    }

    // Requirement 8: If Android notifications are disabled/not permitted, do not attempt to bypass the user's setting.
    if (!NotificationHelper.isSystemPermissionGranted(context)) {
      return
    }
    if (!prefs.areNotificationsEnabledLocally()) {
      return
    }

    val now = System.currentTimeMillis()
    val lastActivity = prefs.getLastActivityTimestamp()

    // If there is no activity recorded yet, record now and schedule
    if (lastActivity <= 0L) {
      prefs.setLastActivityTimestamp(now)
      InactivityReminderManager.scheduleNextReminder(
        context,
        now + InactivityReminderManager.INACTIVITY_INTERVAL_MS
      )
      return
    }

    val elapsedSinceActivity = now - lastActivity

    // Requirement 3: If there has been no meaningful activity for 24 hours (with a small margin)
    if (elapsedSinceActivity < InactivityReminderManager.INACTIVITY_INTERVAL_MS - 60_000L) {
      // User was active less than 24 hours ago; reschedule for 24h after their last activity
      InactivityReminderManager.scheduleNextReminder(
        context,
        lastActivity + InactivityReminderManager.INACTIVITY_INTERVAL_MS
      )
      return
    }

    // Requirement 4 & 6: If the user remains inactive, allow at most ONE inactivity reminder per 24-hour inactivity period.
    // Do NOT repeatedly send the same reminder every few hours.
    val lastReminderSent = prefs.getLastInactivityReminderSentTimestamp()
    if (lastReminderSent > lastActivity && (now - lastReminderSent) < InactivityReminderManager.INACTIVITY_INTERVAL_MS - 60_000L) {
      // Already sent within the last 24h period of inactivity
      InactivityReminderManager.scheduleNextReminder(
        context,
        lastReminderSent + InactivityReminderManager.INACTIVITY_INTERVAL_MS
      )
      return
    }

    // Requirement 7: Do not send the reminder during unreasonable nighttime hours.
    val daytimeTarget = InactivityReminderManager.calculateDaytimeTriggerTime(now)
    if (daytimeTarget > now + 60_000L) {
      // It's currently nighttime! Postpone until the daytime window
      InactivityReminderManager.scheduleNextReminder(context, daytimeTarget)
      return
    }

    // Send the reminder notification using the existing NotificationHelper
    NotificationHelper.showInactivityReminderNotification(
      context = context,
      title = REMINDER_TITLE,
      message = REMINDER_BODY
    )

    // Record that the reminder was sent
    prefs.setLastInactivityReminderSentTimestamp(now)

    // Persist into app notifications so it is visible in the in-app notification center
    val notifItem = AppNotificationItem(
      title = REMINDER_TITLE,
      message = REMINDER_BODY,
      timestamp = now,
      timestampFormatted = formatRelativeNotificationTime(now, now)
    )
    prefs.appendAppNotification(notifItem)

    // Requirement 6: If the user remains inactive, allow at most ONE inactivity reminder per 24-hour inactivity period.
    InactivityReminderManager.scheduleNextReminder(
      context,
      now + InactivityReminderManager.INACTIVITY_INTERVAL_MS
    )
  }
}
