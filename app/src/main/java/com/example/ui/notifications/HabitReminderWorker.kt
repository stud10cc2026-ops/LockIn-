package com.example.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.ui.habits.HabitPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration
import com.example.R

class HabitReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "habit_reminders_channel"
        private const val CHANNEL_NAME = "Habit Reminders"
    }

    override suspend fun doWork(): Result {
        val habitId = inputData.getString("habit_id") ?: return Result.failure()
        val prefs = HabitPreferences(applicationContext)

        if (!prefs.isHabitRemindersGlobalEnabled()) return Result.success()

        val habits = prefs.getHabits()
        val triggerHabit = habits.find { it.id == habitId } ?: return Result.success()
        if (!triggerHabit.remindersEnabled) return Result.success()

        val habitLogs = prefs.getHabitLogs()
        val todayStr = LocalDate.now().toString()
        
        // If already completed today, don't notify
        if (habitLogs[todayStr]?.get(habitId) == true) return Result.success()

        // Batching Logic: Check for other habits due within 30 minutes of "now"
        val nowMillis = System.currentTimeMillis()
        val thirtyMinsMillis = 30 * 60 * 1000L
        
        val dueHabits = mutableListOf<String>()
        val idsToMarkNotified = mutableListOf<String>()

        // 1. Current trigger habit
        dueHabits.add(triggerHabit.name)
        idsToMarkNotified.add(triggerHabit.id)

        // 2. Look for others
        habits.filter { it.id != habitId && !it.isArchived && it.remindersEnabled }.forEach { otherHabit ->
            val isDoneToday = habitLogs[todayStr]?.get(otherHabit.id) == true
            if (!isDoneToday) {
                val scheduledTime = prefs.getHabitReminderScheduledTime(otherHabit.id)
                val lastNotified = prefs.getHabitLastNotifiedAt(otherHabit.id)
                
                // If scheduled within 30 minutes of now AND not already notified today
                val isScheduledSoon = Math.abs(scheduledTime - nowMillis) <= thirtyMinsMillis
                val notNotifiedRecently = (nowMillis - lastNotified) > (12 * 60 * 60 * 1000L) // 12 hours buffer

                if (isScheduledSoon && notNotifiedRecently) {
                    dueHabits.add(otherHabit.name)
                    idsToMarkNotified.add(otherHabit.id)
                }
            }
        }

        // Mark all as notified to prevent immediate re-notification
        idsToMarkNotified.forEach { id ->
            prefs.saveHabitLastNotifiedAt(id, nowMillis)
        }

        sendNotification(dueHabits)

        return Result.success()
    }

    private fun sendNotification(habitNames: List<String>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // We can add extras to navigate to Habits screen if needed
            putExtra("navigate_to", "habits")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title: String
        val message: String

        if (habitNames.size >= 2) {
            title = "${habitNames.size} habits waiting"
            message = habitNames.joinToString(", ")
        } else if (habitNames.size == 1) {
            title = "Habit Reminder"
            message = "Ready to complete '${habitNames[0]}'?"
        } else {
            return
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use default for now
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}
