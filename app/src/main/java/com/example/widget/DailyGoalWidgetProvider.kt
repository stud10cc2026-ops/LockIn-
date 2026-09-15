package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import java.time.LocalDate
import java.util.Calendar

class DailyGoalWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    for (widgetId in appWidgetIds) {
      updateWidget(context, appWidgetManager, widgetId)
    }
    scheduleMidnightUpdate(context)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action
    if (action == Intent.ACTION_DATE_CHANGED ||
        action == Intent.ACTION_TIME_CHANGED ||
        action == "android.intent.action.TIME_SET" ||
        action == Intent.ACTION_TIMEZONE_CHANGED ||
        action == Intent.ACTION_BOOT_COMPLETED ||
        action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
        action == ACTION_UPDATE_DAILY_GOAL) {
      updateAllWidgets(context)
      scheduleMidnightUpdate(context)
    }
  }

  companion object {
    const val ACTION_UPDATE_DAILY_GOAL = "com.example.action.UPDATE_DAILY_GOAL"
    const val ACTION_OPEN_DAILY_PAGE = "com.example.action.OPEN_DAILY_GOAL"
    const val EXTRA_OPEN_TAB = "OPEN_TAB"
    const val TAB_DAILY = "DAILY"

    fun updateAllWidgets(context: Context) {
      try {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, DailyGoalWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(componentName) ?: return
        for (id in ids) {
          updateWidget(context, appWidgetManager, id)
        }
        scheduleMidnightUpdate(context)
      } catch (_: Exception) {}
    }

    fun scheduleMidnightUpdate(context: Context) {
      try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyGoalWidgetProvider::class.java).apply {
          action = ACTION_UPDATE_DAILY_GOAL
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          9001,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
          add(Calendar.DAY_OF_YEAR, 1)
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 5)
          set(Calendar.MILLISECOND, 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
        } else {
          alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
        }
      } catch (_: Exception) {}
    }

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
      val prefs = AppPreferences(context)
      val goal = prefs.getDailyGoal()

      val views = RemoteViews(context.packageName, R.layout.widget_daily_goal)

      // Tap widget opens Home page (MainActivity)
      val tapIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        widgetId,
        tapIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

      if (goal != null) {
        val today = LocalDate.now()
        val currentDay = goal.getCurrentDay(today)
        val remainingDays = goal.getRemainingDays(today)
        val isCompleted = goal.isCompleted(today)

        views.setViewVisibility(R.id.widget_active_layout, View.VISIBLE)
        views.setViewVisibility(R.id.widget_empty_layout, View.GONE)

        // Goal Name
        views.setTextViewText(R.id.widget_goal_name, goal.title)

        // Dynamic Dot Grid Bitmap
        val dotBitmap = createDotGridBitmap(
          totalDays = goal.totalDays,
          currentDay = currentDay,
          isCompleted = isCompleted
        )
        views.setImageViewBitmap(R.id.widget_dot_grid, dotBitmap)

        // Bottom row: Day on left, remaining / completed status on right
        val dayFormatted = String.format("%02d", currentDay)
        views.setTextViewText(R.id.widget_day_text, "DAY $dayFormatted")

        if (isCompleted) {
          views.setTextViewText(R.id.widget_remaining_text, "GOAL COMPLETE")
          views.setTextColor(R.id.widget_remaining_text, Color.parseColor("#CCFF00"))
        } else {
          val remainingText = if (remainingDays == 1) "1 DAY LEFT" else "$remainingDays DAYS LEFT"
          views.setTextViewText(R.id.widget_remaining_text, remainingText)
          views.setTextColor(R.id.widget_remaining_text, Color.parseColor("#8A92A0"))
        }
      } else {
        views.setViewVisibility(R.id.widget_active_layout, View.GONE)
        views.setViewVisibility(R.id.widget_empty_layout, View.VISIBLE)
        views.setTextViewText(R.id.widget_empty_text, "SET A DAILY GOAL")
        views.setTextViewText(R.id.widget_empty_subtext, "Tap to choose your duration")
      }

      appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun createDotGridBitmap(
      totalDays: Int,
      currentDay: Int,
      isCompleted: Boolean
    ): Bitmap {
      val cols = when {
        totalDays <= 7 -> 7
        totalDays <= 31 -> 10
        else -> 28
      }
      val rows = (totalDays + cols - 1) / cols

      val widthPx = 640
      val heightPx = when {
        totalDays <= 7 -> 36
        totalDays <= 31 -> 72
        else -> 120
      }

      val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)

      val spacingX = widthPx.toFloat() / cols
      val spacingY = heightPx.toFloat() / rows

      val baseRadius = when {
        totalDays <= 7 -> (minOf(spacingX, spacingY) * 0.32f).coerceIn(10f, 18f)
        totalDays <= 31 -> (minOf(spacingX, spacingY) * 0.34f).coerceIn(7f, 13f)
        else -> (minOf(spacingX, spacingY) * 0.35f).coerceIn(4f, 7f)
      }

      val paintPassed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
      }
      val paintCurrent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCFF00") // Signature neon lime
        style = Paint.Style.FILL
      }
      val paintRemaining = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#282B33") // Subtle dark charcoal dot
        style = Paint.Style.FILL
      }

      for (day in 1..totalDays) {
        val col = (day - 1) % cols
        val row = (day - 1) / cols

        val cx = col * spacingX + spacingX / 2f
        val cy = row * spacingY + spacingY / 2f

        val isPassed = day < currentDay || isCompleted
        val isCurrent = day == currentDay && !isCompleted

        val paint = when {
          isCurrent -> paintCurrent
          isPassed -> paintPassed
          else -> paintRemaining
        }
        val radius = if (isCurrent) baseRadius * 1.25f else baseRadius
        canvas.drawCircle(cx, cy, radius, paint)
      }

      return bitmap
    }

    fun requestPinAppWidget(context: Context): Boolean {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
        val provider = ComponentName(context, DailyGoalWidgetProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
          val successIntent = Intent(context, DailyGoalWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_DAILY_GOAL
          }
          val successPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            successIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          return appWidgetManager.requestPinAppWidget(provider, null, successPendingIntent)
        }
      }
      return false
    }
  }
}
