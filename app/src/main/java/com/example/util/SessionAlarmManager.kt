package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object SessionAlarmManager {
    private const val REQUEST_CODE = 9921

    fun scheduleSessionCompletion(context: Context, durationSeconds: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMillis = System.currentTimeMillis() + (durationSeconds * 1000L)

        val intent = Intent(context, SessionCompletionReceiver::class.java)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelSessionCompletion(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SessionCompletionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
