package com.example.ui.notifications

import android.content.Context
import androidx.work.*
import com.example.ui.habits.HabitPreferences
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object HabitReminderScheduler {
    private const val REMINDER_DELAY_HOURS = 18L
    private const val QUIET_HOUR_START = 22 // 10 PM
    private const val QUIET_HOUR_END = 8 // 8 AM

    fun scheduleReminder(context: Context, habitId: String) {
        val workManager = WorkManager.getInstance(context)
        
        // 1. Cancel existing work for this habit
        workManager.cancelAllWorkByTag(habitId)

        // 2. Check if global reminders are enabled
        val prefs = HabitPreferences(context)
        if (!prefs.isHabitRemindersGlobalEnabled()) return

        // 3. Check if this specific habit has reminders enabled
        val habits = prefs.getHabits()
        val habit = habits.find { it.id == habitId } ?: return
        if (!habit.remindersEnabled) return

        // 4. Calculate delay (18 hours from now)
        val now = LocalDateTime.now()
        var scheduledTime = now.plusHours(REMINDER_DELAY_HOURS)

        // 5. Handle Quiet Hours (10 PM - 8 AM)
        val timeOfDay = scheduledTime.toLocalTime()
        if (timeOfDay.isAfter(LocalTime.of(QUIET_HOUR_START - 1, 59)) || timeOfDay.isBefore(LocalTime.of(QUIET_HOUR_END, 0))) {
            // Move to 8:00 AM next available time
            if (timeOfDay.isAfter(LocalTime.of(QUIET_HOUR_START - 1, 59))) {
                scheduledTime = scheduledTime.plusDays(1).with(LocalTime.of(QUIET_HOUR_END, 0))
            } else {
                scheduledTime = scheduledTime.with(LocalTime.of(QUIET_HOUR_END, 0))
            }
        }

        val delay = Duration.between(now, scheduledTime).toMillis()
        
        // Save scheduled time for batching logic
        prefs.saveHabitReminderScheduledTime(habitId, scheduledTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())

        // 6. Schedule unique work
        val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(habitId)
            .setInputData(workDataOf("habit_id" to habitId))
            .build()

        workManager.enqueueUniqueWork(
            "reminder_$habitId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context, habitId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(habitId)
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$habitId")
    }
}
