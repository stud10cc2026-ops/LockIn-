package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppPreferences
import com.example.service.FocusBlockerService

class SessionCompletionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appPreferences = AppPreferences(context)
        
        // Stop the background blocker service immediately
        FocusBlockerService.stopService(context)
        
        // Finalize the session in preferences (updates history, stats, and adds app notification)
        if (appPreferences.isSessionActive()) {
            val durationMinutes = appPreferences.getEffectiveDurationMinutes()
            appPreferences.completeSessionInPrefs(durationMinutes)
            
            // Post a system notification to inform the user
            NotificationHelper.postSystemNotification(
                context,
                "Focus Complete",
                "Your session is finished. All apps are unblocked."
            )
            
            // Reschedule inactivity reminders as the user is now "active" (finished a session)
            InactivityReminderManager.recordActivityAndReschedule(context)
        }
    }
}
