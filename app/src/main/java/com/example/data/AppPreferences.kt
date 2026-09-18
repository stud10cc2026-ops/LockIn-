package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.home.AppBlockItem
import com.example.ui.home.FocusSessionHistoryItem
import com.example.ui.home.HomeUiState
import com.example.ui.notifications.AppNotificationItem
import com.example.ui.notifications.formatRelativeNotificationTime
import org.json.JSONArray
import org.json.JSONObject

class AppPreferences(private val context: Context) {
  private val globalPrefs: SharedPreferences = context.getSharedPreferences("lockin_global_prefs", Context.MODE_PRIVATE)

  private fun getPrefsForUser(userId: String?): SharedPreferences {
    val prefsName = if (!userId.isNullOrEmpty()) {
      "lockin_user_prefs_$userId"
    } else {
      "lockin_guest_prefs"
    }
    return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
  }

  fun getActiveUserId(): String? {
    return globalPrefs.getString("active_user_id", null)
  }

  fun setActiveUserId(userId: String?) {
    try {
      if (userId.isNullOrEmpty()) {
        globalPrefs.edit().remove("active_user_id").apply()
      } else {
        globalPrefs.edit().putString("active_user_id", userId).apply()
      }
    } catch (_: Exception) {}
  }

  fun clearActiveUserSession() {
    setActiveUserId(null)
  }

  fun saveUiStateForUser(userId: String, state: HomeUiState) {
    try {
      val prefs = getPrefsForUser(userId)
      val editor = prefs.edit()
      editor.putString("userName", state.userName)
      editor.putBoolean("isLoggedIn", state.isLoggedIn)
      editor.putString("userEmail", state.userEmail)
      editor.putString("userId", userId)
      editor.putBoolean("has_saved_data", true)
      editor.apply()
    } catch (_: Exception) {}
  }

  fun saveUiState(state: HomeUiState) {
    try {
      val targetUserId = if (state.isLoggedIn && !state.userId.isNullOrEmpty()) state.userId else null
      setActiveUserId(targetUserId)

      val prefs = getPrefsForUser(targetUserId)
      val editor = prefs.edit()
      editor.putString("userName", state.userName)
      editor.putBoolean("isLoggedIn", state.isLoggedIn)
      editor.putString("userEmail", state.userEmail)
      editor.putString("userId", state.userId)
      editor.putString("accessToken", state.accessToken)
      editor.putString("refreshToken", state.refreshToken)
      editor.putInt("selectedDurationMinutes", state.selectedDurationMinutes)
      editor.putBoolean("isCustomDuration", state.isCustomDuration)
      editor.putInt("customMinutes", state.customMinutes)
      editor.putBoolean("isNightMode", state.isNightMode)
      editor.putInt("defaultDurationMinutes", state.defaultDurationMinutes)
      editor.putBoolean("allowPause", state.allowPause)
      editor.putBoolean("focusReminders", state.focusReminders)
      editor.putBoolean("notificationsEnabled", state.notificationsEnabled)
      editor.putBoolean("hasSetNotificationPref", state.hasSetNotificationPref)
      editor.putBoolean("sessionStartingReminder", state.sessionStartingReminder)
      editor.putBoolean("sessionCompletedReminder", state.sessionCompletedReminder)
      editor.putBoolean("focusSessionPausedReminder", state.focusSessionPausedReminder)
      editor.putInt("reminderTimingMinutes", state.reminderTimingMinutes)

      // Save App Notifications
      val notifJsonArray = JSONArray()
      state.appNotifications.forEach { notif ->
        val obj = JSONObject()
        obj.put("id", notif.id)
        obj.put("title", notif.title)
        obj.put("message", notif.message)
        obj.put("timestamp", notif.timestamp)
        obj.put("timestampFormatted", formatRelativeNotificationTime(notif.timestamp))
        obj.put("isRead", notif.isRead)
        notifJsonArray.put(obj)
      }
      editor.putString("app_notifications_json", notifJsonArray.toString())

      // Active session state
      editor.putBoolean("isSessionActive", state.isSessionActive)
      editor.putBoolean("isSessionPaused", state.isSessionPaused)
      editor.putInt("remainingSeconds", state.remainingSeconds)
      editor.putInt("totalSessionSeconds", state.totalSessionSeconds)
      editor.putLong("lastSavedTimestamp", System.currentTimeMillis())

      // Save Apps
      val appsJsonArray = JSONArray()
      state.apps.forEach { app ->
        val obj = JSONObject()
        obj.put("id", app.id)
        obj.put("name", app.name)
        obj.put("category", app.category)
        obj.put("isBlocked", app.isBlocked)
        appsJsonArray.put(obj)
      }
      editor.putString("apps_json", appsJsonArray.toString())

      // Save History Items
      val historyJsonArray = JSONArray()
      state.historyItems.forEach { item ->
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("durationMinutes", item.durationMinutes)
        obj.put("selectedDurationMinutes", item.selectedDurationMinutes)
        obj.put("sessionType", item.sessionType)
        obj.put("timestampFormatted", item.timestampFormatted)
        obj.put("dateGroup", item.dateGroup)
        obj.put("isCompleted", item.isCompleted)
        obj.put("timestampMillis", item.timestampMillis)
        historyJsonArray.put(obj)
      }
      editor.putString("history_json", historyJsonArray.toString())

      editor.putBoolean("has_saved_data", true)
      editor.apply()
    } catch (_: Exception) {}
  }

  fun loadUiState(defaultState: HomeUiState): HomeUiState {
    val activeUid = getActiveUserId()
    return loadUiStateForUser(activeUid, defaultState)
  }

  fun loadUiStateForUser(userId: String?, defaultState: HomeUiState): HomeUiState {
    val prefs = getPrefsForUser(userId)
    if (!prefs.getBoolean("has_saved_data", false)) {
      return defaultState.copy(
        userId = userId ?: defaultState.userId,
        isLoggedIn = !userId.isNullOrEmpty()
      )
    }

    try {
      val userName = prefs.getString("userName", defaultState.userName) ?: defaultState.userName
      val isLoggedIn = prefs.getBoolean("isLoggedIn", !userId.isNullOrEmpty())
      val userEmail = prefs.getString("userEmail", defaultState.userEmail) ?: defaultState.userEmail
      val savedUserId = prefs.getString("userId", userId) ?: userId
      val accessToken = prefs.getString("accessToken", defaultState.accessToken)
      val refreshToken = prefs.getString("refreshToken", defaultState.refreshToken)
      val selectedDurationMinutes = prefs.getInt("selectedDurationMinutes", defaultState.selectedDurationMinutes)
      val isCustomDuration = prefs.getBoolean("isCustomDuration", defaultState.isCustomDuration)
      val customMinutes = prefs.getInt("customMinutes", defaultState.customMinutes)
      val isNightMode = prefs.getBoolean("isNightMode", defaultState.isNightMode)
      val defaultDurationMinutes = prefs.getInt("defaultDurationMinutes", defaultState.defaultDurationMinutes)
      val allowPause = prefs.getBoolean("allowPause", defaultState.allowPause)
      val focusReminders = prefs.getBoolean("focusReminders", defaultState.focusReminders)
      val notificationsEnabled = prefs.getBoolean("notificationsEnabled", defaultState.notificationsEnabled)
      val hasSetNotificationPref = prefs.getBoolean("hasSetNotificationPref", false)
      val sessionStartingReminder = prefs.getBoolean("sessionStartingReminder", defaultState.sessionStartingReminder)
      val sessionCompletedReminder = prefs.getBoolean("sessionCompletedReminder", defaultState.sessionCompletedReminder)
      val focusSessionPausedReminder = prefs.getBoolean("focusSessionPausedReminder", defaultState.focusSessionPausedReminder)
      val reminderTimingMinutes = prefs.getInt("reminderTimingMinutes", defaultState.reminderTimingMinutes)

      // App Notifications
      val appNotifications = mutableListOf<AppNotificationItem>()
      val notifJsonStr = prefs.getString("app_notifications_json", null)
      if (!notifJsonStr.isNullOrEmpty()) {
        try {
          val jsonArray = JSONArray(notifJsonStr)
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val ts = obj.optLong("timestamp", System.currentTimeMillis())
            appNotifications.add(
              AppNotificationItem(
                id = obj.getString("id"),
                title = obj.getString("title"),
                message = obj.getString("message"),
                timestamp = ts,
                timestampFormatted = formatRelativeNotificationTime(ts),
                isRead = obj.optBoolean("isRead", false)
              )
            )
          }
        } catch (_: Exception) {}
      }

      // Apps
      val apps = mutableListOf<AppBlockItem>()
      val appsJsonStr = prefs.getString("apps_json", null)
      if (!appsJsonStr.isNullOrEmpty()) {
        try {
          val jsonArray = JSONArray(appsJsonStr)
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            apps.add(
              AppBlockItem(
                id = obj.getString("id"),
                name = obj.getString("name"),
                category = obj.getString("category"),
                isBlocked = obj.getBoolean("isBlocked")
              )
            )
          }
        } catch (_: Exception) {
          apps.addAll(defaultState.apps)
        }
      } else {
        apps.addAll(defaultState.apps)
      }

      // History
      val historyItems = mutableListOf<FocusSessionHistoryItem>()
      val historyJsonStr = prefs.getString("history_json", null)
      if (!historyJsonStr.isNullOrEmpty()) {
        try {
          val jsonArray = JSONArray(historyJsonStr)
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val dur = obj.getInt("durationMinutes")
            val selDur = obj.optInt("selectedDurationMinutes", dur)
            val ts = obj.optLong("timestampMillis", 0L)
            historyItems.add(
              FocusSessionHistoryItem(
                id = obj.getString("id"),
                durationMinutes = dur,
                selectedDurationMinutes = selDur,
                sessionType = obj.optString("sessionType", "Focus session"),
                timestampFormatted = obj.getString("timestampFormatted"),
                dateGroup = obj.getString("dateGroup"),
                isCompleted = obj.getBoolean("isCompleted"),
                timestampMillis = ts
              )
            )
          }
        } catch (_: Exception) {}
      }

      // Active session
      var isSessionActive = prefs.getBoolean("isSessionActive", false)
      val isSessionPaused = prefs.getBoolean("isSessionPaused", false)
      var remainingSeconds = prefs.getInt("remainingSeconds", 0)
      val totalSessionSeconds = prefs.getInt("totalSessionSeconds", 0)
      val lastSavedTimestamp = prefs.getLong("lastSavedTimestamp", 0L)

      if (isSessionActive && !isSessionPaused && lastSavedTimestamp > 0L) {
        val elapsedSeconds = ((System.currentTimeMillis() - lastSavedTimestamp) / 1000L).toInt()
        if (elapsedSeconds > 0) {
          remainingSeconds = maxOf(0, remainingSeconds - elapsedSeconds)
        }
      }

      return defaultState.copy(
        userName = userName,
        isLoggedIn = isLoggedIn,
        userEmail = userEmail,
        userId = savedUserId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        selectedDurationMinutes = selectedDurationMinutes,
        isCustomDuration = isCustomDuration,
        customMinutes = customMinutes,
        apps = apps,
        historyItems = historyItems,
        isNightMode = isNightMode,
        defaultDurationMinutes = defaultDurationMinutes,
        allowPause = allowPause,
        focusReminders = focusReminders,
        notificationsEnabled = notificationsEnabled,
        hasSetNotificationPref = hasSetNotificationPref,
        appNotifications = appNotifications,
        sessionStartingReminder = sessionStartingReminder,
        sessionCompletedReminder = sessionCompletedReminder,
        focusSessionPausedReminder = focusSessionPausedReminder,
        reminderTimingMinutes = reminderTimingMinutes,
        isSessionActive = isSessionActive,
        isSessionPaused = isSessionPaused,
        remainingSeconds = remainingSeconds,
        totalSessionSeconds = totalSessionSeconds
      )
    } catch (_: Exception) {
      return defaultState
    }
  }

  fun isFirstLaunch(): Boolean {
    return globalPrefs.getBoolean("is_first_launch", true)
  }

  fun setFirstLaunchCompleted() {
    try {
      globalPrefs.edit().putBoolean("is_first_launch", false).apply()
    } catch (_: Exception) {}
  }

  fun hasRequestedSystemPermission(): Boolean {
    return globalPrefs.getBoolean("has_requested_system_permission", false)
  }

  fun setSystemPermissionRequested() {
    try {
      globalPrefs.edit().putBoolean("has_requested_system_permission", true).apply()
    } catch (_: Exception) {}
  }

  fun clearAll() {
    try {
      val activeUid = getActiveUserId()
      if (!activeUid.isNullOrEmpty()) {
        getPrefsForUser(activeUid).edit().clear().apply()
      }
      getPrefsForUser(null).edit().clear().apply()
      globalPrefs.edit().clear().apply()
    } catch (_: Exception) {}
  }

  fun getLastActivityTimestamp(): Long {
    return globalPrefs.getLong("last_activity_timestamp", 0L)
  }

  fun setLastActivityTimestamp(timestamp: Long = System.currentTimeMillis()) {
    try {
      globalPrefs.edit().putLong("last_activity_timestamp", timestamp).apply()
    } catch (_: Exception) {}
  }

  fun getLastInactivityReminderSentTimestamp(): Long {
    return globalPrefs.getLong("last_inactivity_reminder_sent_timestamp", 0L)
  }

  fun setLastInactivityReminderSentTimestamp(timestamp: Long) {
    try {
      globalPrefs.edit().putLong("last_inactivity_reminder_sent_timestamp", timestamp).apply()
    } catch (_: Exception) {}
  }

  fun areNotificationsEnabledLocally(): Boolean {
    val activeUid = getActiveUserId()
    val prefs = getPrefsForUser(activeUid)
    return if (prefs.getBoolean("has_saved_data", false)) {
      prefs.getBoolean("notificationsEnabled", true)
    } else {
      true
    }
  }

  fun appendAppNotification(item: AppNotificationItem) {
    try {
      val activeUid = getActiveUserId()
      val prefs = getPrefsForUser(activeUid)
      val notifJsonStr = prefs.getString("app_notifications_json", null)
      val jsonArray = if (!notifJsonStr.isNullOrEmpty()) JSONArray(notifJsonStr) else JSONArray()

      val newObj = JSONObject().apply {
        put("id", item.id)
        put("title", item.title)
        put("message", item.message)
        put("timestamp", item.timestamp)
        put("timestampFormatted", formatRelativeNotificationTime(item.timestamp))
        put("isRead", item.isRead)
      }

      val newArray = JSONArray()
      newArray.put(newObj)
      for (i in 0 until jsonArray.length()) {
        newArray.put(jsonArray.getJSONObject(i))
      }
      prefs.edit().putString("app_notifications_json", newArray.toString()).apply()
    } catch (_: Exception) {}
  }
  fun completeSessionInPrefs(durationMinutes: Int) {
    try {
      val activeUid = getActiveUserId()
      val prefs = getPrefsForUser(activeUid)
      val editor = prefs.edit()
      
      editor.putBoolean("isSessionActive", false)
      editor.putBoolean("isSessionPaused", false)
      editor.putInt("remainingSeconds", 0)
      
      // Add to history
      val historyJsonStr = prefs.getString("history_json", "[]") ?: "[]"
      val historyArray = try { JSONArray(historyJsonStr) } catch(_: Exception) { JSONArray() }
      
      val now = System.currentTimeMillis()
      val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
      val timeStr = timeFormat.format(java.util.Date(now))
      
      val newObj = JSONObject().apply {
        put("id", now.toString())
        put("durationMinutes", durationMinutes)
        put("selectedDurationMinutes", durationMinutes)
        put("sessionType", "Focus session")
        put("timestampFormatted", "Today · $timeStr")
        put("dateGroup", "Today")
        put("isCompleted", true)
        put("timestampMillis", now)
      }
      
      val updatedArray = JSONArray()
      updatedArray.put(newObj)
      for (i in 0 until historyArray.length()) {
        updatedArray.put(historyArray.get(i))
      }
      editor.putString("history_json", updatedArray.toString())
      editor.putLong("lastSavedTimestamp", now)
      editor.apply()
      
      // Append app notification
      appendAppNotification(
        AppNotificationItem(
          id = java.util.UUID.randomUUID().toString(),
          title = "The timer has ended.",
          message = "All apps are unblocked.",
          timestamp = now,
          isRead = false
        )
      )
    } catch (_: Exception) {}
  }

  fun isSessionActive(): Boolean {
    val activeUid = getActiveUserId()
    return getPrefsForUser(activeUid).getBoolean("isSessionActive", false)
  }

  fun getEffectiveDurationMinutes(): Int {
    val activeUid = getActiveUserId()
    val prefs = getPrefsForUser(activeUid)
    return if (prefs.getBoolean("isCustomDuration", false)) {
      prefs.getInt("customMinutes", 0)
    } else {
      prefs.getInt("selectedDurationMinutes", 0)
    }
  }

  fun isNightMode(): Boolean {
    val activeUid = getActiveUserId()
    val prefs = getPrefsForUser(activeUid)
    return if (prefs.getBoolean("has_saved_data", false)) {
      prefs.getBoolean("isNightMode", false)
    } else {
      globalPrefs.getBoolean("isNightMode", false)
    }
  }

  fun getDailyGoal(): DailyGoal? {
    val activeUid = getActiveUserId()
    val prefs = getPrefsForUser(activeUid)
    val jsonStr = prefs.getString("daily_goal_json", null)
      ?: globalPrefs.getString("daily_goal_json", null)
      ?: return null
    return try {
      DailyGoal.fromJson(JSONObject(jsonStr))
    } catch (_: Exception) {
      null
    }
  }

  fun getLastNotifiedGoalId(): String? {
    return globalPrefs.getString("last_notified_goal_id", null)
  }

  fun setLastNotifiedGoalId(goalId: String?) {
    try {
      if (goalId == null) {
        globalPrefs.edit().remove("last_notified_goal_id").apply()
      } else {
        globalPrefs.edit().putString("last_notified_goal_id", goalId).apply()
      }
    } catch (_: Exception) {}
  }

  fun saveDailyGoal(goal: DailyGoal?) {
    try {
      val activeUid = getActiveUserId()
      val userPrefs = getPrefsForUser(activeUid)
      if (goal != null) {
        val jsonStr = goal.toJson().toString()
        userPrefs.edit().putString("daily_goal_json", jsonStr).apply()
        globalPrefs.edit().putString("daily_goal_json", jsonStr).apply()
      } else {
        userPrefs.edit().remove("daily_goal_json").apply()
        globalPrefs.edit().remove("daily_goal_json").apply()
      }
    } catch (_: Exception) {}
  }

  fun clearDailyGoal() {
    saveDailyGoal(null)
  }

  fun loadAppBlockItems(): List<AppBlockItem> {
    val activeUid = getActiveUserId()
    val prefs = getPrefsForUser(activeUid)
    val appsJsonStr = prefs.getString("apps_json", null) ?: return emptyList()
    val list = mutableListOf<AppBlockItem>()
    try {
      val jsonArray = JSONArray(appsJsonStr)
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
          AppBlockItem(
            id = obj.getString("id"),
            name = obj.getString("name"),
            category = obj.getString("category"),
            isBlocked = obj.getBoolean("isBlocked")
          )
        )
      }
    } catch (_: Exception) {}
    return list
  }
}
