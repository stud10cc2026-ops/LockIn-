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
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class ProgressIndicatorWidgetProvider : AppWidgetProvider() {

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

    fun updateAllWidgets(context: Context) {
      try {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, ProgressIndicatorWidgetProvider::class.java)
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
        val intent = Intent(context, ProgressIndicatorWidgetProvider::class.java).apply {
          action = ACTION_UPDATE_DAILY_GOAL
        }
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          9004,
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

      val views = RemoteViews(context.packageName, R.layout.widget_progress_indicator)

      // Tap widget opens MainActivity (Goal Overview)
      val tapIntent = Intent(context, MainActivity::class.java).apply {
        action = "com.example.action.OPEN_DAILY_GOAL"
        putExtra("OPEN_TAB", "DAILY")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        widgetId,
        tapIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      views.setOnClickPendingIntent(R.id.widget_progress_indicator_container, pendingIntent)

      if (goal != null) {
        val timezoneId = prefs.getSelectedTimezoneId()
        val today = try {
          if (timezoneId.isNullOrEmpty() || timezoneId == "DEVICE_DEFAULT") {
            LocalDate.now()
          } else {
            LocalDate.now(ZoneId.of(timezoneId))
          }
        } catch (_: Exception) {
          LocalDate.now()
        }

        val totalDays = goal.totalDays
        val currentDayIndex = goal.getCurrentDay(today)
        val passedDays = if (today.isBefore(goal.getStartDate())) 0 else currentDayIndex.coerceIn(0, totalDays)

        val currentDay = goal.getCurrentDay(today)
        val remainingDays = goal.getRemainingDays(today)
        val endDate = goal.getEndDate()

        val statusText = when {
          today.isAfter(endDate) -> "Goal complete"
          currentDay == totalDays -> "Final day"
          else -> "Day $currentDay of $totalDays • $remainingDays days to go"
        }
        val subtitle = "${goal.title} • $statusText"

        val progressPercent = if (totalDays > 0) (currentDay.toFloat() / totalDays.toFloat() * 100f).toInt() else 0

        // Comparison Logic
        var comparisonText: String? = null
        if (passedDays >= 14) {
          fun getConsistentCount(start: Int, end: Int): Int {
            var count = 0
            for (d in start..end) {
              val r = goal.ratings[d]
              if (r == null || r == 1 || r == 2) count++
            }
            return count
          }
          val currentPeriodCount = getConsistentCount(passedDays - 6, passedDays)
          val prevPeriodCount = getConsistentCount(passedDays - 13, passedDays - 7)
          if (currentPeriodCount != prevPeriodCount) {
            val diff = currentPeriodCount - prevPeriodCount
            val percentChange = if (prevPeriodCount > 0) {
              (kotlin.math.abs(diff).toFloat() / prevPeriodCount.toFloat() * 100f).toInt()
            } else {
              kotlin.math.abs(diff) * 100
            }
            val arrow = if (diff > 0) "↗" else "↘"
            comparisonText = "$arrow $percentChange%"
          }
        }

        views.setViewVisibility(R.id.widget_progress_indicator_active_layout, View.VISIBLE)
        views.setViewVisibility(R.id.widget_progress_indicator_empty_layout, View.GONE)

        views.setTextViewText(R.id.widget_progress_indicator_subtitle, subtitle)
        views.setTextViewText(R.id.widget_progress_indicator_percent, "$progressPercent%")

        if (comparisonText != null) {
          views.setTextViewText(R.id.widget_progress_indicator_badge, comparisonText)
          views.setViewVisibility(R.id.widget_progress_indicator_badge_layout, View.VISIBLE)
        } else {
          views.setViewVisibility(R.id.widget_progress_indicator_badge_layout, View.GONE)
        }

        val barBitmap = createSegmentedBarBitmap(currentDay.toFloat() / totalDays.toFloat())
        views.setImageViewBitmap(R.id.widget_progress_indicator_bar, barBitmap)
      } else {
        views.setViewVisibility(R.id.widget_progress_indicator_active_layout, View.GONE)
        views.setViewVisibility(R.id.widget_progress_indicator_empty_layout, View.VISIBLE)
      }

      appWidgetManager.updateAppWidget(widgetId, views)
    }

    fun requestPinAppWidget(context: Context): Boolean {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
        val provider = ComponentName(context, ProgressIndicatorWidgetProvider::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
          val successIntent = Intent(context, ProgressIndicatorWidgetProvider::class.java).apply {
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

    private fun createSegmentedBarBitmap(progress: Float): Bitmap {
      val totalBars = 10
      val widthPx = 800
      val heightPx = 60
      val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)

      val gap = 12f
      val barWidth = (widthPx.toFloat() - (totalBars - 1) * gap) / totalBars
      
      val paintFilled = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4A9FE0") }
      val paintUnfilled = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A2A2A") }

      val filledCount = (progress.coerceIn(0f, 1f) * totalBars).toInt()

      for (i in 0 until totalBars) {
        val left = i * (barWidth + gap)
        val right = left + barWidth
        val top = 0f
        val bottom = heightPx.toFloat()

        val paint = if (i < filledCount) paintFilled else paintUnfilled
        canvas.drawRoundRect(left, top, right, bottom, 6f, 6f, paint)
      }

      return bitmap
    }
  }
}
