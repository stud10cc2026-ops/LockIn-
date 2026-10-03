package com.example.ui.habits

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        val actionsToRefresh = listOf(
            "BIND_HABIT_TO_WIDGET",
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        if (intent.action in actionsToRefresh) {
            val pendingResult = goAsync()
            MainScope().launch {
                try {
                    if (intent.action == "BIND_HABIT_TO_WIDGET") {
                        val habitId = intent.getStringExtra("habit_id")
                        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        
                        if (habitId != null && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            val prefs = HabitPreferences(context)
                            prefs.saveWidgetBinding(widgetId, habitId)
                        }
                    }
                    
                    // Refresh all widgets
                    HabitWidget().updateAll(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val prefs = HabitPreferences(context)
        appWidgetIds.forEach { id ->
            prefs.removeWidgetBinding(id)
        }
    }
}
