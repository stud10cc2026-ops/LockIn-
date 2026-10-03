package com.example.ui.habits

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class HabitPreferences(private val context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("lockin_habits_prefs", Context.MODE_PRIVATE)

  fun getHabits(): List<HabitItem> {
    val jsonStr = prefs.getString("habits_list_json", null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonStr)
      val list = mutableListOf<HabitItem>()
      for (i in 0 until array.length()) {
        list.add(HabitItem.fromJson(array.getJSONObject(i)))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveHabits(habits: List<HabitItem>) {
    try {
      val array = JSONArray()
      habits.forEach { array.put(it.toJson()) }
      prefs.edit().putString("habits_list_json", array.toString()).apply()
    } catch (_: Exception) {}
  }

  // Returns Map<dateStr ("yyyy-MM-dd"), Map<habitId, Boolean>>
  fun getHabitLogs(): Map<String, Map<String, Boolean>> {
    val jsonStr = prefs.getString("habit_logs_json", null) ?: return emptyMap()
    return try {
      val obj = JSONObject(jsonStr)
      val result = mutableMapOf<String, MutableMap<String, Boolean>>()
      val dates = obj.keys()
      while (dates.hasNext()) {
        val dateStr = dates.next()
        val dateObj = obj.getJSONObject(dateStr)
        val innerMap = mutableMapOf<String, Boolean>()
        val habitIds = dateObj.keys()
        while (habitIds.hasNext()) {
          val habitId = habitIds.next()
          val valObj = dateObj.get(habitId)
          val isDone = when (valObj) {
            is Boolean -> valObj
            is String -> valObj.isNotEmpty() && !valObj.equals("false", ignoreCase = true)
            else -> false
          }
          if (isDone) {
            innerMap[habitId] = true
          }
        }
        if (innerMap.isNotEmpty()) {
          result[dateStr] = innerMap
        }
      }
      result
    } catch (_: Exception) {
      emptyMap()
    }
  }

  fun saveHabitLog(dateStr: String, habitId: String, isDone: Boolean) {
    try {
      val currentLogs = getHabitLogs().toMutableMap()
      val dateMap = (currentLogs[dateStr] ?: emptyMap()).toMutableMap()
      if (!isDone) {
        dateMap.remove(habitId)
      } else {
        dateMap[habitId] = true
      }

      if (dateMap.isEmpty()) {
        currentLogs.remove(dateStr)
      } else {
        currentLogs[dateStr] = dateMap
      }

      val rootObj = JSONObject()
      currentLogs.forEach { (dStr, map) ->
        val dateObj = JSONObject()
        map.forEach { (hId, done) ->
          dateObj.put(hId, done)
        }
        rootObj.put(dStr, dateObj)
      }

      prefs.edit().putString("habit_logs_json", rootObj.toString()).apply()
    } catch (_: Exception) {}
  }

  fun getLastSelectedTab(): Int = prefs.getInt("last_selected_tab", 0)
  fun saveLastSelectedTab(tabIndex: Int) {
    prefs.edit().putInt("last_selected_tab", tabIndex).apply()
  }

  fun getLastFilter(): String = prefs.getString("last_filter", "ALL") ?: "ALL"
  fun saveLastFilter(filter: String) {
    prefs.edit().putString("last_filter", filter).apply()
  }

  fun getLastViewMode(): String = prefs.getString("last_view_mode", "DETAILED") ?: "DETAILED"
  fun saveLastViewMode(mode: String) {
    prefs.edit().putString("last_view_mode", mode).apply()
  }

  fun saveWidgetBinding(widgetId: Int, habitId: String) {
    val bindings = getWidgetBindings().toMutableMap()
    bindings[widgetId.toString()] = habitId
    val obj = JSONObject(bindings as Map<*, *>)
    prefs.edit().putString("widget_bindings_json", obj.toString()).apply()
  }

  fun getWidgetBindings(): Map<String, String> {
    val jsonStr = prefs.getString("widget_bindings_json", null) ?: return emptyMap()
    return try {
      val obj = JSONObject(jsonStr)
      val result = mutableMapOf<String, String>()
      obj.keys().forEach { key ->
        result[key] = obj.getString(key)
      }
      result
    } catch (_: Exception) { emptyMap() }
  }

  fun removeWidgetBinding(widgetId: Int) {
    val bindings = getWidgetBindings().toMutableMap()
    bindings.remove(widgetId.toString())
    val obj = JSONObject(bindings as Map<*, *>)
    prefs.edit().putString("widget_bindings_json", obj.toString()).apply()
  }

  fun isHabitRemindersGlobalEnabled(): Boolean = prefs.getBoolean("habit_reminders_global_enabled", true)
  fun setHabitRemindersGlobalEnabled(enabled: Boolean) {
    prefs.edit().putBoolean("habit_reminders_global_enabled", enabled).apply()
  }

  fun saveHabitReminderScheduledTime(habitId: String, timeMillis: Long) {
    prefs.edit().putLong("reminder_scheduled_$habitId", timeMillis).apply()
  }

  fun getHabitReminderScheduledTime(habitId: String): Long {
    return prefs.getLong("reminder_scheduled_$habitId", 0L)
  }

  fun removeHabitReminderScheduledTime(habitId: String) {
    prefs.edit().remove("reminder_scheduled_$habitId").apply()
  }

  fun saveHabitLastNotifiedAt(habitId: String, timeMillis: Long) {
    prefs.edit().putLong("last_notified_$habitId", timeMillis).apply()
  }

  fun getHabitLastNotifiedAt(habitId: String): Long {
    return prefs.getLong("last_notified_$habitId", 0L)
  }

  fun clearAll() {
    prefs.edit().clear().apply()
  }

  fun getGuestHabits(): List<HabitItem> {
    val guestPrefs = context.getSharedPreferences("lockin_guest_prefs_habits", Context.MODE_PRIVATE)
    val jsonStr = guestPrefs.getString("habits_list_json", null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonStr as String)
      val list = mutableListOf<HabitItem>()
      for (i in 0 until array.length()) {
        list.add(HabitItem.fromJson(array.getJSONObject(i)))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun getGuestHabitLogs(): Map<String, Map<String, Boolean>> {
    val guestPrefs = context.getSharedPreferences("lockin_guest_prefs_habits", Context.MODE_PRIVATE)
    val jsonStr = guestPrefs.getString("habit_logs_json", null) ?: return emptyMap()
    // Logic same as getHabitLogs but using guestPrefs
    return try {
      val obj = JSONObject(jsonStr as String)
      val result = mutableMapOf<String, MutableMap<String, Boolean>>()
      val dates = obj.keys()
      while (dates.hasNext()) {
        val dateStr = dates.next()
        val dateObj = obj.getJSONObject(dateStr)
        val innerMap = mutableMapOf<String, Boolean>()
        val habitIds = dateObj.keys()
        while (habitIds.hasNext()) {
          val habitId = habitIds.next()
          val valObj = dateObj.get(habitId)
          val isDone = when (valObj) {
            is Boolean -> valObj
            is String -> valObj.isNotEmpty() && !valObj.equals("false", ignoreCase = true)
            else -> false
          }
          if (isDone) {
            innerMap[habitId] = true
          }
        }
        if (innerMap.isNotEmpty()) {
          result[dateStr] = innerMap
        }
      }
      result
    } catch (_: Exception) {
      emptyMap()
    }
  }

  fun clearGuestData() {
    context.getSharedPreferences("lockin_guest_prefs_habits", Context.MODE_PRIVATE).edit().clear().apply()
  }
}
