package com.example.ui.habits

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import android.appwidget.AppWidgetManager
import android.content.Intent
import java.time.LocalDate

class ConfigActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val appWidgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: -1
        val intent = Intent(context, HabitWidgetConfigActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            HabitWidgetContent()
        }
    }

    @Composable
    private fun HabitWidgetContent() {
        val context = LocalContext.current
        val glanceId = LocalGlanceId.current
        
        val appWidgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: -1
        val prefs = HabitPreferences(context)
        val bindings = prefs.getWidgetBindings()
        val habitId = bindings[appWidgetId.toString()]
        
        if (habitId == null) {
            val habits = prefs.getHabits()
            if (habits.isEmpty()) {
                WidgetErrorState("No habits yet")
            } else {
                WidgetErrorState("Tap to select habit")
            }
            return
        }
        
        val habits = prefs.getHabits()
        val habit = habits.find { it.id == habitId }
        
        if (habit == null) {
            WidgetErrorState("Habit missing")
            return
        }
        
        val logs = prefs.getHabitLogs()
        val today = LocalDate.now()
        val streak = calculateCurrentStreak(habit.id, logs, today)
        
        WidgetMainLayout(context, habit.name, streak, appWidgetId)
    }

    @Composable
    private fun WidgetMainLayout(context: Context, habitName: String, streak: Int, appWidgetId: Int) {
        val rv = RemoteViews(context.packageName, com.example.R.layout.widget_habit_card)
        rv.setTextViewText(com.example.R.id.tv_habit_name, habitName)
        rv.setTextViewText(com.example.R.id.tv_streak_number, streak.toString())

        for (i in 0 until 30) {
            val dotId = context.resources.getIdentifier("dot_$i", "id", context.packageName)
            if (dotId != 0) {
                val drawableId = if (i < streak) com.example.R.drawable.ic_dot_active_white else com.example.R.drawable.ic_dot_inactive_grey
                rv.setImageViewResource(dotId, drawableId)
            }
        }

        if (appWidgetId != -1) {
            val configIntent = Intent(context, HabitWidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            rv.setOnClickPendingIntent(com.example.R.id.widget_root, pendingIntent)
        }

        AndroidRemoteViews(rv)
    }

    @Composable
    private fun WidgetErrorState(message: String) {
        val glanceId = LocalGlanceId.current
        val appWidgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: -1

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
                .clickable(
                    actionRunCallback<ConfigActionCallback>()
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = message, style = TextStyle(color = ColorProvider(Color.White)))
        }
    }
}
