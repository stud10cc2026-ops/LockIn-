package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.DailyGoal
import com.example.data.FirebaseAuthManager
import com.example.data.FirebaseDataManager
import com.example.ui.notifications.AppNotificationItem
import com.example.ui.notifications.PendingDeletedNotification
import com.example.ui.notifications.formatRelativeNotificationTime
import com.example.ui.stats.DayBarData
import com.example.service.FocusBlockerService
import com.example.util.AppBlockerHelper
import com.example.util.NotificationHelper
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AppBlockItem(
  val id: String,
  val name: String,
  val category: String,
  val isBlocked: Boolean = true
)

data class FocusSessionHistoryItem(
  val id: String,
  val durationMinutes: Int,
  val selectedDurationMinutes: Int = durationMinutes,
  val sessionType: String = "Focus session",
  val timestampFormatted: String,
  val dateGroup: String,
  val isCompleted: Boolean = true,
  val timestampMillis: Long = System.currentTimeMillis()
)

data class HomeUiState(
  val userName: String = "",
  val selectedDurationMinutes: Int = 40,
  val isCustomDuration: Boolean = false,
  val customMinutes: Int = 45,
  val apps: List<AppBlockItem> = listOf(
    AppBlockItem("instagram", "Instagram", "Social", isBlocked = true),
    AppBlockItem("tiktok", "TikTok", "Short Video", isBlocked = true),
    AppBlockItem("youtube", "YouTube", "Video", isBlocked = true),
    AppBlockItem("whatsapp", "WhatsApp", "Messaging", isBlocked = true),
    AppBlockItem("reddit", "Reddit", "Forums", isBlocked = true),
    AppBlockItem("snapchat", "Snapchat", "Photos", isBlocked = true)
  ),
  val historyItems: List<FocusSessionHistoryItem> = emptyList(),
  val isManageSheetOpen: Boolean = false,
  val isSessionStartedDialog: Boolean = false,
  val isSessionActive: Boolean = false,
  val isSessionPaused: Boolean = false,
  val remainingSeconds: Int = 0,
  val totalSessionSeconds: Int = 0,
  val todayFocusFormatted: String = "0m",
  val todaySessionsCount: Int = 0,
  val activeTab: Int = 0, // 0: Home, 1: History, 2: Stats, 3: Settings
  val isNightMode: Boolean = false,
  val defaultDurationMinutes: Int = 40,
  val allowPause: Boolean = true,
  val focusReminders: Boolean = true,
  val notificationsEnabled: Boolean = true,
  val hasSetNotificationPref: Boolean = false,
  val showFirstLaunchNotificationPrompt: Boolean = false,
  val appNotifications: List<AppNotificationItem> = emptyList(),
  val pendingDeletedNotification: PendingDeletedNotification? = null,
  val isNotificationPageOpen: Boolean = false,
  val sessionStartingReminder: Boolean = true,
  val sessionCompletedReminder: Boolean = true,
  val focusSessionPausedReminder: Boolean = true,
  val reminderTimingMinutes: Int = 5,
  val isLoggedIn: Boolean = false,
  val userEmail: String = "",
  val userId: String? = null,
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val isAuthScreenOpen: Boolean = false,
  val authMode: String = "LOGIN", // "LOGIN", "SIGNUP", "FORGOT_PASSWORD", "CREATE_PASSWORD"
  val isAccountPageOpen: Boolean = false,
  val recoveryToken: String? = null,
  val showPermissionPrompt: Boolean = false,
  val blockedAppName: String? = null,
  val showWelcomeIntro: Boolean = false,
  val dailyGoal: DailyGoal? = null
) {
  val isAllAppsBlocked: Boolean
    get() = apps.all { it.isBlocked }

  val activeBlockedApps: List<AppBlockItem>
    get() = apps.filter { it.isBlocked }

  val effectiveDurationMinutes: Int
    get() = if (isCustomDuration) customMinutes else selectedDurationMinutes

  val hasUnreadNotifications: Boolean
    get() = appNotifications.any { !it.isRead }
}

data class ComputedStats(
  val weeklyTotalFormatted: String = "0m",
  val monthlyTotalFormatted: String = "0m",
  val totalSessions: Int = 0,
  val avgSessionFormatted: String = "0 min",
  val bestSessionFormatted: String = "0 min",
  val activeDaysCount: Int = 0,
  val totalDaysInPeriod: Int = 7,
  val insightMessage: String = "Complete focus sessions to see insights.",
  val weekDays: List<DayBarData> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = AppPreferences(application)
  private val firebaseAuthManager = FirebaseAuthManager()
  private val firebaseDataManager = FirebaseDataManager()

  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null
  private var undoDeleteJob: Job? = null

  init {
    val loadedState = prefs.loadUiState(_uiState.value)
    val isSystemGranted = NotificationHelper.isSystemPermissionGranted(getApplication())
    val updatedHistory = loadedState.historyItems.map { item ->
      if (item.timestampMillis > 0L) {
        item.copy(dateGroup = getDateGroupLabel(item.timestampMillis))
      } else {
        item
      }
    }
    val initialNotifEnabled = if (!isSystemGranted) false else loadedState.notificationsEnabled
    val now = System.currentTimeMillis()
    val refreshedNotifs = loadedState.appNotifications.map { notif ->
      notif.copy(timestampFormatted = formatRelativeNotificationTime(notif.timestamp, now))
    }
    val isFirstLaunch = prefs.isFirstLaunch()
    val savedGoal = prefs.getDailyGoal()
    val stateWithStats = recalculateTodayStats(
      loadedState.copy(
        showWelcomeIntro = isFirstLaunch,
        historyItems = updatedHistory,
        showFirstLaunchNotificationPrompt = false,
        notificationsEnabled = initialNotifEnabled,
        focusReminders = if (!isSystemGranted) false else loadedState.focusReminders,
        appNotifications = refreshedNotifs,
        dailyGoal = savedGoal
      )
    )
    _uiState.value = stateWithStats

    // Check for goal completion notification on startup
    try {
      checkAndNotifyGoalCompletion(savedGoal)
    } catch (_: Exception) {}

    if (stateWithStats.isLoggedIn && !stateWithStats.userId.isNullOrEmpty()) {
      syncWithCloudData(stateWithStats.userId, stateWithStats.accessToken)
    }

    if (stateWithStats.isSessionActive) {
      if (stateWithStats.remainingSeconds > 0) {
        FocusBlockerService.startService(getApplication())
        resumeTimerLoop()
      } else {
        // Session expired while app was closed - finalize it now
        completeSessionNaturally()
      }
    }
  }

  fun resetAppCompletely() {
    resetAppData()
  }

  private fun checkAndNotifyGoalCompletion(goal: DailyGoal?) {
    if (goal == null) return
    if (goal.isCompleted()) {
      val lastNotified = prefs.getLastNotifiedGoalId()
      if (lastNotified != goal.id) {
        val title = "Goal Completed!"
        val message = "You completed your ${goal.title} goal. Great work!"
        
        // Trigger notification (this also appends to appNotifications and saves state)
        triggerNotification(title, message)
        
        prefs.setLastNotifiedGoalId(goal.id)
      }
    }
  }

  private fun syncWithCloudData(userId: String, idToken: String?) {
    viewModelScope.launch(Dispatchers.IO) {
      val cloudData = firebaseDataManager.loadUserData(userId, idToken)
      if (cloudData != null) {
        _uiState.update { current ->
          if (current.userId != userId) return@update current
          val updatedHistory = (cloudData.historyItems ?: current.historyItems).map { item ->
            if (item.timestampMillis > 0L) {
              item.copy(dateGroup = getDateGroupLabel(item.timestampMillis))
            } else {
              item
            }
          }
          val updatedNotifs = cloudData.appNotifications ?: current.appNotifications
          val newName = cloudData.userName?.ifBlank { null } ?: current.userName
          val merged = current.copy(
            userName = newName,
            userEmail = cloudData.userEmail ?: current.userEmail,
            historyItems = updatedHistory,
            appNotifications = updatedNotifs,
            selectedDurationMinutes = cloudData.selectedDurationMinutes ?: current.selectedDurationMinutes,
            isCustomDuration = cloudData.isCustomDuration ?: current.isCustomDuration,
            customMinutes = cloudData.customMinutes ?: current.customMinutes,
            isNightMode = cloudData.isNightMode ?: current.isNightMode,
            defaultDurationMinutes = cloudData.defaultDurationMinutes ?: current.defaultDurationMinutes,
            allowPause = cloudData.allowPause ?: current.allowPause,
            focusReminders = cloudData.focusReminders ?: current.focusReminders,
            notificationsEnabled = cloudData.notificationsEnabled ?: current.notificationsEnabled,
            apps = cloudData.apps ?: current.apps,
            dailyGoal = cloudData.dailyGoal ?: current.dailyGoal
          )
          if (cloudData.dailyGoal != null && current.dailyGoal != cloudData.dailyGoal) {
            prefs.saveDailyGoal(cloudData.dailyGoal)
            com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
          }
          recalculateTodayStats(merged)
        }
        prefs.saveUiState(_uiState.value)
      } else if (userId.isNotBlank()) {
        firebaseDataManager.saveUserData(userId, idToken, _uiState.value)
      }
    }
  }

  private val _requestPermissionEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val requestPermissionEvent: SharedFlow<Unit> = _requestPermissionEvent.asSharedFlow()

  fun completeWelcomeIntro() {
    prefs.setFirstLaunchCompleted()
    _uiState.update {
      it.copy(
        showWelcomeIntro = false,
        showFirstLaunchNotificationPrompt = false
      )
    }
    com.example.util.InactivityReminderManager.recordActivityAndReschedule(getApplication())
  }

  fun onWelcomeContinueClicked(context: Context? = null) {
    completeWelcomeIntro()
    val appContext = context ?: getApplication()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val isGranted = NotificationHelper.isSystemPermissionGranted(appContext)
      val hasRequested = prefs.hasRequestedSystemPermission()
      if (!isGranted && !hasRequested) {
        prefs.setSystemPermissionRequested()
        _requestPermissionEvent.tryEmit(Unit)
      } else {
        syncSystemNotificationPermission(appContext)
      }
    } else {
      syncSystemNotificationPermission(appContext)
    }
  }

  fun onFirstLaunchNotificationChoice(turnOn: Boolean, context: Context) {
    prefs.setFirstLaunchCompleted()
    prefs.setSystemPermissionRequested()
    if (turnOn) {
      val isSystemGranted = NotificationHelper.isSystemPermissionGranted(context)
      _uiState.update {
        it.copy(
          hasSetNotificationPref = true,
          showFirstLaunchNotificationPrompt = false
        )
      }
      if (!isSystemGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          _requestPermissionEvent.tryEmit(Unit)
        } else {
          NotificationHelper.openNotificationSettings(context)
        }
      } else {
        onSystemPermissionResult(true)
      }
    } else {
      _uiState.update {
        it.copy(
          notificationsEnabled = false,
          focusReminders = false,
          hasSetNotificationPref = true,
          showFirstLaunchNotificationPrompt = false
        )
      }
      saveCurrentState()
    }
  }

  fun requestInitialSystemPermissionOnFirstLaunch(context: Context) {
    syncSystemNotificationPermission(context)
  }

  fun requestSystemPermissionIfNeeded() {
    // System permission state is synced via syncSystemNotificationPermission on ON_RESUME
  }

  fun onSystemPermissionResult(isGranted: Boolean) {
    prefs.setSystemPermissionRequested()
    prefs.setFirstLaunchCompleted()
    _uiState.update {
      it.copy(
        notificationsEnabled = isGranted,
        focusReminders = isGranted,
        hasSetNotificationPref = true,
        showFirstLaunchNotificationPrompt = false
      )
    }
    saveCurrentState()
  }

  fun refreshNotificationTimestamps() {
    val now = System.currentTimeMillis()
    _uiState.update { current ->
      val refreshed = current.appNotifications.map { notif ->
        notif.copy(timestampFormatted = formatRelativeNotificationTime(notif.timestamp, now))
      }
      current.copy(appNotifications = refreshed)
    }
  }

  fun syncSystemNotificationPermission(context: Context) {
    val isSystemGranted = NotificationHelper.isSystemPermissionGranted(context)
    refreshNotificationTimestamps()
    _uiState.update { current ->
      current.copy(
        notificationsEnabled = isSystemGranted,
        focusReminders = if (!isSystemGranted) false else current.focusReminders
      )
    }
    saveCurrentState()
  }

  fun setNotificationsEnabled(enabled: Boolean, context: Context? = null) {
    val appContext = context ?: getApplication()
    val isSystemGranted = NotificationHelper.isSystemPermissionGranted(appContext)

    if (enabled) {
      if (isSystemGranted) {
        _uiState.update {
          it.copy(
            notificationsEnabled = true,
            focusReminders = true,
            hasSetNotificationPref = true
          )
        }
        saveCurrentState()
      } else {
        _uiState.update {
          it.copy(
            notificationsEnabled = false,
            focusReminders = false
          )
        }
        saveCurrentState()
      }
    } else {
      _uiState.update {
        it.copy(
          notificationsEnabled = false,
          focusReminders = false,
          hasSetNotificationPref = true
        )
      }
      saveCurrentState()
    }
  }

  fun triggerNotification(title: String, message: String) {
    if (!_uiState.value.notificationsEnabled) return

    val now = System.currentTimeMillis()
    val newItem = AppNotificationItem(
      title = title,
      message = message,
      timestamp = now,
      timestampFormatted = formatRelativeNotificationTime(now, now)
    )

    _uiState.update { current ->
      val refreshedExisting = current.appNotifications.map { notif ->
        notif.copy(timestampFormatted = formatRelativeNotificationTime(notif.timestamp, now))
      }
      current.copy(
        appNotifications = listOf(newItem) + refreshedExisting
      )
    }
    saveCurrentState()

    NotificationHelper.postSystemNotification(getApplication(), title, message)
  }

  fun deleteNotification(notification: AppNotificationItem) {
    val currentList = _uiState.value.appNotifications
    val index = currentList.indexOfFirst { it.id == notification.id }
    if (index == -1) return

    finalizePendingDelete()

    val newList = currentList.filter { it.id != notification.id }
    _uiState.update {
      it.copy(
        appNotifications = newList,
        pendingDeletedNotification = PendingDeletedNotification(item = notification, index = index)
      )
    }
    saveCurrentState()

    undoDeleteJob?.cancel()
    undoDeleteJob = viewModelScope.launch {
      delay(4000L)
      finalizePendingDelete()
    }
  }

  fun undoDeleteNotification() {
    val pending = _uiState.value.pendingDeletedNotification ?: return
    undoDeleteJob?.cancel()
    undoDeleteJob = null

    val currentList = _uiState.value.appNotifications.toMutableList()
    val restoreIndex = minOf(pending.index, currentList.size)
    currentList.add(restoreIndex, pending.item)

    _uiState.update {
      it.copy(
        appNotifications = currentList,
        pendingDeletedNotification = null
      )
    }
    saveCurrentState()
  }

  fun finalizePendingDelete() {
    undoDeleteJob?.cancel()
    undoDeleteJob = null
    if (_uiState.value.pendingDeletedNotification != null) {
      _uiState.update {
        it.copy(pendingDeletedNotification = null)
      }
      saveCurrentState()
    }
  }

  private fun getDateGroupLabel(timestampMillis: Long): String {
    val sessionCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val nowCal = Calendar.getInstance()

    val isToday = sessionCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                  sessionCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
    if (isToday) return "Today"

    val isYesterday = sessionCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                      sessionCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) - 1
    if (isYesterday) return "Yesterday"

    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMillis))
  }

  private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
  }

  private fun isSameWeek(sessionMillis: Long, nowCal: Calendar): Boolean {
    val sCal = Calendar.getInstance().apply {
      firstDayOfWeek = Calendar.MONDAY
      timeInMillis = sessionMillis
    }
    val nCal = (nowCal.clone() as Calendar).apply {
      firstDayOfWeek = Calendar.MONDAY
    }
    return sCal.get(Calendar.YEAR) == nCal.get(Calendar.YEAR) &&
           sCal.get(Calendar.WEEK_OF_YEAR) == nCal.get(Calendar.WEEK_OF_YEAR)
  }

  private fun isSameMonth(sessionMillis: Long, nowCal: Calendar): Boolean {
    val sCal = Calendar.getInstance().apply { timeInMillis = sessionMillis }
    return sCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
           sCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
  }

  private fun recalculateTodayStats(state: HomeUiState): HomeUiState {
    val nowMillis = System.currentTimeMillis()
    val todayCompletedSessions = state.historyItems.filter { item ->
      item.isCompleted && (isSameDay(if (item.timestampMillis > 0L) item.timestampMillis else nowMillis, nowMillis) || (item.timestampMillis == 0L && item.dateGroup == "Today"))
    }
    val totalMins = todayCompletedSessions.sumOf { it.durationMinutes }
    val formatted = formatMinutes(totalMins)
    return state.copy(
      todayFocusFormatted = formatted,
      todaySessionsCount = todayCompletedSessions.size
    )
  }

  private fun saveCurrentState() {
    val currentState = _uiState.value
    val uid = currentState.userId
    val token = currentState.accessToken
    viewModelScope.launch(Dispatchers.IO) {
      prefs.saveUiState(currentState)
      if (currentState.isLoggedIn && !uid.isNullOrEmpty()) {
        firebaseDataManager.saveUserData(uid, token, currentState)
      }
    }
  }

  fun getComputedStats(): ComputedStats {
    val history = _uiState.value.historyItems.filter { it.isCompleted }
    val daysOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val nowCal = Calendar.getInstance()
    val nowMillis = nowCal.timeInMillis
    val todayLabel = getTodayDayOfWeekLabel()

    if (history.isEmpty()) {
      val emptyWeek = daysOrder.map {
        DayBarData(it, 0f, displayTime = "0m", isHighlight = (it == todayLabel))
      }
      return ComputedStats(
        weeklyTotalFormatted = "0m",
        monthlyTotalFormatted = "0m",
        totalSessions = 0,
        avgSessionFormatted = "0m",
        bestSessionFormatted = "0m",
        activeDaysCount = 0,
        totalDaysInPeriod = 7,
        insightMessage = "Start a focus session to build your stats.",
        weekDays = emptyWeek
      )
    }

    val weeklyItems = history.filter { item ->
      val ts = if (item.timestampMillis > 0L) item.timestampMillis else nowMillis
      isSameWeek(ts, nowCal) || (item.timestampMillis == 0L && item.dateGroup == "Today")
    }

    val monthlyItems = history.filter { item ->
      val ts = if (item.timestampMillis > 0L) item.timestampMillis else nowMillis
      isSameMonth(ts, nowCal) || (item.timestampMillis == 0L && item.dateGroup == "Today")
    }

    val weeklyTotalMins = weeklyItems.sumOf { it.durationMinutes }
    val monthlyTotalMins = monthlyItems.sumOf { it.durationMinutes }

    val totalSessions = history.size
    val totalAllMins = history.sumOf { it.durationMinutes }
    val avgMins = if (totalSessions > 0) Math.round(totalAllMins.toDouble() / totalSessions).toInt() else 0
    val bestMins = if (history.isNotEmpty()) history.maxOf { it.durationMinutes } else 0

    val daysMinsMap = mutableMapOf("Mon" to 0, "Tue" to 0, "Wed" to 0, "Thu" to 0, "Fri" to 0, "Sat" to 0, "Sun" to 0)

    weeklyItems.forEach { item ->
      val ts = if (item.timestampMillis > 0L) item.timestampMillis else nowMillis
      val itemCal = Calendar.getInstance().apply { timeInMillis = ts }
      val dayKey = when (itemCal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        Calendar.SUNDAY -> "Sun"
        else -> "Mon"
      }
      daysMinsMap[dayKey] = (daysMinsMap[dayKey] ?: 0) + item.durationMinutes
    }

    val weekDays = daysOrder.map { label ->
      val mins = daysMinsMap[label] ?: 0
      val hoursVal = mins / 60.0f
      DayBarData(
        dayLabel = label,
        hours = hoursVal,
        displayTime = formatMinutes(mins),
        isHighlight = (label == todayLabel)
      )
    }

    val activeDaysCount = daysMinsMap.count { it.value > 0 }

    val insightMsg = when {
      activeDaysCount >= 5 -> "Outstanding consistency! You're building a solid focus habit."
      totalSessions >= 3 -> "Great momentum! Consistency builds habit."
      totalSessions > 0 -> "Keep going! Every session counts towards your goal."
      else -> "Complete focus sessions to see insights."
    }

    return ComputedStats(
      weeklyTotalFormatted = formatMinutes(weeklyTotalMins),
      monthlyTotalFormatted = formatMinutes(monthlyTotalMins),
      totalSessions = totalSessions,
      avgSessionFormatted = formatMinutes(avgMins),
      bestSessionFormatted = formatMinutes(bestMins),
      activeDaysCount = activeDaysCount,
      totalDaysInPeriod = 7,
      insightMessage = insightMsg,
      weekDays = weekDays
    )
  }

  private fun getTodayDayOfWeekLabel(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> "Mon"
      Calendar.TUESDAY -> "Tue"
      Calendar.WEDNESDAY -> "Wed"
      Calendar.THURSDAY -> "Thu"
      Calendar.FRIDAY -> "Fri"
      Calendar.SATURDAY -> "Sat"
      Calendar.SUNDAY -> "Sun"
      else -> "Mon"
    }
  }

  private fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
      hours == 0 -> "${mins}m"
      mins == 0 -> "${hours}h"
      else -> "${hours}h ${mins}m"
    }
  }

  fun selectPresetDuration(minutes: Int) {
    _uiState.update {
      it.copy(
        selectedDurationMinutes = minutes,
        isCustomDuration = false
      )
    }
    saveCurrentState()
  }

  fun toggleCustomDuration() {
    _uiState.update { it.copy(isCustomDuration = true) }
    saveCurrentState()
  }

  fun updateCustomMinutes(minutes: Int) {
    _uiState.update {
      it.copy(
        customMinutes = minutes.coerceIn(1, 300),
        isCustomDuration = true
      )
    }
    saveCurrentState()
  }

  fun toggleAppBlocked(appId: String) {
    _uiState.update { state ->
      val updatedApps = state.apps.map { app ->
        if (app.id == appId) app.copy(isBlocked = !app.isBlocked) else app
      }
      state.copy(apps = updatedApps)
    }
    saveCurrentState()
  }

  fun openManageAppsSheet() {
    _uiState.update { it.copy(isManageSheetOpen = true) }
  }

  fun closeManageAppsSheet() {
    _uiState.update { it.copy(isManageSheetOpen = false) }
  }

  fun onStartFocusSessionRequested(context: Context) {
    val hasUsage = AppBlockerHelper.isUsageStatsPermissionGranted(context)
    val hasOverlay = AppBlockerHelper.isOverlayPermissionGranted(context)

    if (!hasUsage || !hasOverlay) {
      _uiState.update { it.copy(showPermissionPrompt = true) }
    } else {
      startFocusSession()
    }
  }

  fun checkAppBlockerPermissionsOnResume(context: Context) {
    if (_uiState.value.showPermissionPrompt) {
      val hasUsage = AppBlockerHelper.isUsageStatsPermissionGranted(context)
      val hasOverlay = AppBlockerHelper.isOverlayPermissionGranted(context)
      if (hasUsage && hasOverlay) {
        _uiState.update { it.copy(showPermissionPrompt = false) }
        startFocusSession()
      }
    }
  }

  fun dismissPermissionPrompt() {
    _uiState.update { it.copy(showPermissionPrompt = false) }
  }

  fun startFocusSession() {
    val totalSecs = _uiState.value.effectiveDurationMinutes * 60
    _uiState.update {
      it.copy(
        isSessionActive = true,
        isSessionPaused = false,
        remainingSeconds = totalSecs,
        totalSessionSeconds = totalSecs,
        isSessionStartedDialog = false,
        showPermissionPrompt = false
      )
    }
    saveCurrentState()
    com.example.util.SessionAlarmManager.scheduleSessionCompletion(getApplication(), totalSecs)
    FocusBlockerService.startService(getApplication())
    resumeTimerLoop()
    triggerNotification("Focus Session started", "All apps are blocked.")
    com.example.util.InactivityReminderManager.recordActivityAndReschedule(getApplication())
  }

  private fun resumeTimerLoop() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      var lastTick = System.currentTimeMillis()
      while (isActive && _uiState.value.isSessionActive && _uiState.value.remainingSeconds > 0) {
        delay(1000L)
        val now = System.currentTimeMillis()
        val elapsed = maxOf(1, ((now - lastTick) / 1000L).toInt())
        lastTick = now
        if (!_uiState.value.isSessionPaused) {
          _uiState.update { state ->
            val newRem = maxOf(0, state.remainingSeconds - elapsed)
            state.copy(remainingSeconds = newRem)
          }
          saveCurrentState()
        }
      }
      if (_uiState.value.isSessionActive && _uiState.value.remainingSeconds <= 0) {
        completeSessionNaturally()
      }
    }
  }

  fun setBlockedAppAlert(appName: String) {
    _uiState.update { it.copy(blockedAppName = appName) }
  }

  fun clearBlockedAppAlert() {
    _uiState.update { it.copy(blockedAppName = null) }
  }

  fun togglePauseResumeFocusSession() {
    val wasPaused = _uiState.value.isSessionPaused
    val newState = !wasPaused
    _uiState.update { it.copy(isSessionPaused = newState) }
    saveCurrentState()
    
    if (newState) {
      // Paused: cancel background alarm
      com.example.util.SessionAlarmManager.cancelSessionCompletion(getApplication())
    } else {
      // Resumed: reschedule background alarm with remaining time
      com.example.util.SessionAlarmManager.scheduleSessionCompletion(getApplication(), _uiState.value.remainingSeconds)
    }
  }

  private fun completeSessionNaturally() {
    timerJob?.cancel()
    timerJob = null
    com.example.util.SessionAlarmManager.cancelSessionCompletion(getApplication())
    FocusBlockerService.stopService(getApplication())

    val currentDuration = _uiState.value.effectiveDurationMinutes
    val now = System.currentTimeMillis()
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(now))
    val dateGroupStr = getDateGroupLabel(now)

    val newItem = FocusSessionHistoryItem(
      id = now.toString(),
      durationMinutes = currentDuration,
      selectedDurationMinutes = currentDuration,
      sessionType = "Focus session",
      timestampFormatted = "Today · $timeStr",
      dateGroup = dateGroupStr,
      isCompleted = true,
      timestampMillis = now
    )

    _uiState.update { state ->
      val updatedHistory = listOf(newItem) + state.historyItems
      val stateWithHistory = state.copy(
        isSessionActive = false,
        isSessionPaused = false,
        remainingSeconds = 0,
        blockedAppName = null,
        historyItems = updatedHistory
      )
      recalculateTodayStats(stateWithHistory)
    }
    saveCurrentState()

    triggerNotification("The timer has ended.", "All apps are unblocked.")
    com.example.util.InactivityReminderManager.recordActivityAndReschedule(getApplication())
  }

  fun completePushUpChallengeSession() {
    timerJob?.cancel()
    timerJob = null
    com.example.util.SessionAlarmManager.cancelSessionCompletion(getApplication())
    FocusBlockerService.stopService(getApplication())

    val totalSecs = _uiState.value.totalSessionSeconds
    val remSecs = _uiState.value.remainingSeconds
    val selectedMins = _uiState.value.effectiveDurationMinutes
    val elapsedSecs = maxOf(0, totalSecs - remSecs)
    val elapsedMins = if (elapsedSecs == 0) 1 else maxOf(1, Math.round(elapsedSecs / 60.0).toInt())
    val now = System.currentTimeMillis()
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(now))
    val dateGroupStr = getDateGroupLabel(now)

    val newItem = FocusSessionHistoryItem(
      id = now.toString(),
      durationMinutes = elapsedMins,
      selectedDurationMinutes = selectedMins,
      sessionType = "Focus session",
      timestampFormatted = "Today · $timeStr",
      dateGroup = dateGroupStr,
      isCompleted = true,
      timestampMillis = now
    )

    _uiState.update { state ->
      val updatedHistory = listOf(newItem) + state.historyItems
      val stateWithHistory = state.copy(
        isSessionActive = false,
        isSessionPaused = false,
        remainingSeconds = 0,
        blockedAppName = null,
        historyItems = updatedHistory
      )
      recalculateTodayStats(stateWithHistory)
    }
    saveCurrentState()

    triggerNotification("Push-Up Challenge Completed", "Push-up challenge complete! All apps are unblocked.")
    com.example.util.InactivityReminderManager.recordActivityAndReschedule(getApplication())
  }

  fun recordEarlyExitSession() {
    timerJob?.cancel()
    timerJob = null
    FocusBlockerService.stopService(getApplication())

    val totalSecs = _uiState.value.totalSessionSeconds
    val remSecs = _uiState.value.remainingSeconds
    val selectedMins = _uiState.value.effectiveDurationMinutes
    val elapsedSecs = maxOf(0, totalSecs - remSecs)
    
    val elapsedMins = if (elapsedSecs == 0) 1 else maxOf(1, Math.round(elapsedSecs / 60.0).toInt())
    val now = System.currentTimeMillis()
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(now))
    val dateGroupStr = getDateGroupLabel(now)

    val newItem = FocusSessionHistoryItem(
      id = now.toString(),
      durationMinutes = elapsedMins,
      selectedDurationMinutes = selectedMins,
      sessionType = "Focus session",
      timestampFormatted = "Today · $timeStr",
      dateGroup = dateGroupStr,
      isCompleted = false,
      timestampMillis = now
    )

    _uiState.update { state ->
      val updatedHistory = listOf(newItem) + state.historyItems
      val stateWithHistory = state.copy(
        isSessionActive = false,
        isSessionPaused = false,
        remainingSeconds = 0,
        blockedAppName = null,
        historyItems = updatedHistory
      )
      recalculateTodayStats(stateWithHistory)
    }
    saveCurrentState()
    com.example.util.InactivityReminderManager.recordActivityAndReschedule(getApplication())
  }

  fun endFocusSession() {
    com.example.util.SessionAlarmManager.cancelSessionCompletion(getApplication())
    FocusBlockerService.stopService(getApplication())
    recordEarlyExitSession()
  }

  fun dismissSessionStartedDialog() {
    _uiState.update { it.copy(isSessionStartedDialog = false) }
    saveCurrentState()
  }

  fun setActiveTab(index: Int) {
    _uiState.update { it.copy(activeTab = index) }
    saveCurrentState()
  }

  fun createDailyGoal(durationType: String) {
    val newGoal = DailyGoal.create(durationType)
    prefs.saveDailyGoal(newGoal)
    _uiState.update { it.copy(dailyGoal = newGoal) }
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
    saveCurrentState()
  }

  fun createDailyGoalWithDetails(title: String, totalDays: Int, label: String? = null) {
    val newGoal = DailyGoal.create(title = title, totalDaysInput = totalDays, labelInput = label)
    prefs.saveDailyGoal(newGoal)
    _uiState.update { it.copy(dailyGoal = newGoal) }
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
    saveCurrentState()
  }

  fun updateDailyGoalTitle(newTitle: String) {
    val currentGoal = _uiState.value.dailyGoal ?: return
    val updatedGoal = currentGoal.copy(title = if (newTitle.isNotBlank()) newTitle.trim() else "Daily Goal")
    prefs.saveDailyGoal(updatedGoal)
    _uiState.update { it.copy(dailyGoal = updatedGoal) }
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
    saveCurrentState()
  }

  fun updateDailyGoalDuration(totalDays: Int, label: String? = null) {
    val currentGoal = _uiState.value.dailyGoal ?: return
    val validDays = totalDays.coerceIn(7, 365)
    val start = try { java.time.LocalDate.parse(currentGoal.startDateStr) } catch (_: Exception) { java.time.LocalDate.now() }
    val newEndDate = start.plusDays(validDays.toLong()).toString()
    val newLabel = label ?: "$validDays Days"
    val updatedGoal = currentGoal.copy(
      totalDays = validDays,
      durationLabel = newLabel,
      endDateStr = newEndDate
    )
    prefs.saveDailyGoal(updatedGoal)
    _uiState.update { it.copy(dailyGoal = updatedGoal) }
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
    saveCurrentState()
  }

  fun resetDailyGoal() {
    prefs.clearDailyGoal()
    _uiState.update { it.copy(dailyGoal = null) }
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
    saveCurrentState()
  }

  fun setNightMode(isNight: Boolean) {
    _uiState.update { it.copy(isNightMode = isNight) }
    saveCurrentState()
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())
  }

  fun setDefaultDurationMinutes(minutes: Int) {
    _uiState.update {
      it.copy(
        defaultDurationMinutes = minutes,
        selectedDurationMinutes = minutes
      )
    }
    saveCurrentState()
  }

  fun setAllowPause(allow: Boolean) {
    _uiState.update { it.copy(allowPause = allow) }
    saveCurrentState()
  }

  fun setFocusReminders(enabled: Boolean) {
    _uiState.update { it.copy(focusReminders = enabled) }
    saveCurrentState()
  }

  fun setNotificationPageOpen(open: Boolean) {
    if (open) {
      refreshNotificationTimestamps()
      _uiState.update { current ->
        val readNotifs = current.appNotifications.map { it.copy(isRead = true) }
        current.copy(
          isNotificationPageOpen = true,
          appNotifications = readNotifs
        )
      }
    } else {
      _uiState.update { it.copy(isNotificationPageOpen = false) }
    }
    saveCurrentState()
  }

  fun setSessionStartingReminder(enabled: Boolean) {
    _uiState.update { it.copy(sessionStartingReminder = enabled) }
    saveCurrentState()
  }

  fun setSessionCompletedReminder(enabled: Boolean) {
    _uiState.update { it.copy(sessionCompletedReminder = enabled) }
    saveCurrentState()
  }

  fun setFocusSessionPausedReminder(enabled: Boolean) {
    _uiState.update { it.copy(focusSessionPausedReminder = enabled) }
    saveCurrentState()
  }

  fun setReminderTimingMinutes(minutes: Int) {
    _uiState.update { it.copy(reminderTimingMinutes = minutes) }
    saveCurrentState()
  }

  fun setUserName(name: String) {
    val trimmed = name.trim()
    if (trimmed.isNotEmpty()) {
      _uiState.update { it.copy(userName = trimmed) }
      saveCurrentState()
    }
  }

  fun setAccountPageOpen(isOpen: Boolean) {
    _uiState.update { it.copy(isAccountPageOpen = isOpen) }
  }

  fun openAuthScreen(mode: String = "LOGIN") {
    _uiState.update { it.copy(isAuthScreenOpen = true, authMode = mode) }
  }

  fun closeAuthScreen() {
    _uiState.update { it.copy(isAuthScreenOpen = false) }
  }

  fun signUp(name: String, email: String, pass: String): String? {
    val trimmedName = name.trim()
    val trimmedEmail = email.trim()

    if (trimmedName.isEmpty()) return "Please enter your name."
    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
      return "Please enter a valid email address."
    }
    if (pass.isEmpty()) return "Please enter a password."
    if (pass.length < 6) return "Password must be at least 6 characters long."

    val authResult = runBlocking(Dispatchers.IO) {
      firebaseAuthManager.signUp(trimmedEmail, pass)
    }

    if (!authResult.success) {
      return authResult.errorMessage ?: "User already exists. Please sign in"
    }

    val userId = authResult.userId
    val finalEmail = authResult.email?.ifEmpty { trimmedEmail } ?: trimmedEmail
    if (!userId.isNullOrEmpty()) {
      val userState = prefs.loadUiStateForUser(userId, HomeUiState()).copy(
        userName = trimmedName,
        userEmail = finalEmail,
        userId = userId,
        isLoggedIn = false
      )
      prefs.saveUiStateForUser(userId, userState)
      viewModelScope.launch(Dispatchers.IO) {
        firebaseDataManager.saveUserData(userId, authResult.idToken, userState)
      }
    }

    return "VERIFY_EMAIL:We have sent you a verification email to $finalEmail. Please verify it and log in."
  }

  fun handlePasswordResetDeepLink(token: String) {
    _uiState.update {
      it.copy(
        recoveryToken = token,
        isAuthScreenOpen = true,
        authMode = "CREATE_PASSWORD"
      )
    }
  }

  suspend fun sendPasswordReset(email: String): Pair<Boolean, String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val trimmedEmail = email.trim()
    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
      return@withContext Pair(false, "Please enter a valid email address.")
    }
    val result = firebaseAuthManager.sendPasswordResetEmail(getApplication(), trimmedEmail)
    if (result.success) {
      Pair(true, result.errorMessage ?: "Password reset link sent to $trimmedEmail.")
    } else {
      Pair(false, result.errorMessage ?: "Failed to send password reset email. Please try again.")
    }
  }

  fun updatePassword(newPass: String): Pair<Boolean, String> {
    val trimmedPass = newPass.trim()
    if (trimmedPass.isEmpty()) return Pair(false, "Please enter a new password.")
    if (trimmedPass.length < 8) return Pair(false, "Password must be at least 8 characters long.")
    if (!trimmedPass.any { it.isLetter() }) return Pair(false, "Password must contain at least 1 letter.")
    if (!trimmedPass.any { it.isDigit() }) return Pair(false, "Password must contain at least 1 number.")

    _uiState.update {
      it.copy(
        recoveryToken = null
      )
    }
    saveCurrentState()
    return Pair(true, "Password updated successfully.")
  }

  fun login(email: String, pass: String): String? {
    val trimmedEmail = email.trim()

    if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
      return "Please enter a valid email address."
    }
    if (pass.isEmpty()) return "Please enter your password."

    val authResult = runBlocking(Dispatchers.IO) {
      firebaseAuthManager.signIn(trimmedEmail, pass)
    }

    if (!authResult.success) {
      if (authResult.isEmailUnverified) {
        val finalEmail = authResult.email?.ifEmpty { trimmedEmail } ?: trimmedEmail
        return "VERIFY_EMAIL:We have sent you a verification email to $finalEmail. Please verify it and log in."
      }
      return authResult.errorMessage ?: "Email or password is incorrect"
    }

    val userId = authResult.userId
    val idToken = authResult.idToken
    val finalEmail = authResult.email?.ifEmpty { trimmedEmail } ?: trimmedEmail

    val cleanDefault = HomeUiState()
    val localCachedState = if (!userId.isNullOrEmpty()) {
      prefs.loadUiStateForUser(userId, cleanDefault)
    } else {
      cleanDefault
    }

    val finalName = if (localCachedState.userName.isNotBlank()) {
      localCachedState.userName
    } else {
      finalEmail.substringBefore("@").replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
      }
    }

    val updatedState = recalculateTodayStats(
      localCachedState.copy(
        userName = finalName,
        userEmail = finalEmail,
        userId = userId,
        accessToken = idToken,
        refreshToken = authResult.refreshToken,
        isLoggedIn = true,
        isAuthScreenOpen = false,
        isAccountPageOpen = false
      )
    )

    _uiState.value = updatedState
    prefs.saveUiState(updatedState)

    if (!userId.isNullOrEmpty()) {
      syncWithCloudData(userId, idToken)
    }

    triggerNotification("Welcome back", "Ready to lock in?")
    return null
  }

  fun logout() {
    prefs.clearActiveUserSession()
    prefs.clearDailyGoal()
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())

    val freshLoggedOutState = HomeUiState(
      isLoggedIn = false,
      userName = "",
      userEmail = "",
      userId = null,
      accessToken = null,
      refreshToken = null,
      isAccountPageOpen = false,
      isAuthScreenOpen = false,
      historyItems = emptyList(),
      appNotifications = emptyList(),
      todayFocusFormatted = "0m",
      todaySessionsCount = 0,
      dailyGoal = null
    )

    _uiState.value = recalculateTodayStats(freshLoggedOutState)
    prefs.saveUiState(_uiState.value)
  }

  fun resetAppData() {
    timerJob?.cancel()
    timerJob = null
    FocusBlockerService.stopService(getApplication())

    prefs.clearAll()
    com.example.widget.DailyGoalWidgetProvider.updateAllWidgets(getApplication())

    val freshState = HomeUiState(
      userName = "",
      isLoggedIn = false,
      userEmail = "",
      userId = null,
      accessToken = null,
      refreshToken = null,
      selectedDurationMinutes = 40,
      isCustomDuration = false,
      customMinutes = 45,
      historyItems = emptyList(),
      todayFocusFormatted = "0m",
      todaySessionsCount = 0,
      isNightMode = false,
      defaultDurationMinutes = 40,
      allowPause = false,
      focusReminders = NotificationHelper.isSystemPermissionGranted(getApplication()),
      notificationsEnabled = NotificationHelper.isSystemPermissionGranted(getApplication()),
      hasSetNotificationPref = false,
      showFirstLaunchNotificationPrompt = false,
      appNotifications = emptyList(),
      sessionStartingReminder = true,
      sessionCompletedReminder = true,
      focusSessionPausedReminder = true,
      reminderTimingMinutes = 15,
      isSessionActive = false,
      isSessionPaused = false,
      remainingSeconds = 0,
      totalSessionSeconds = 0,
      isNotificationPageOpen = false,
      isAccountPageOpen = false,
      showWelcomeIntro = true
    )

    _uiState.value = recalculateTodayStats(freshState)
    prefs.saveUiState(_uiState.value)
  }

  fun generateDemoData() {
    val now = System.currentTimeMillis()
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun createSession(daysAgo: Int, hour: Int, minute: Int, duration: Int): FocusSessionHistoryItem {
      val cal = Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_YEAR, -daysAgo)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      val ts = cal.timeInMillis
      val dateLabel = when (daysAgo) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> dateFormat.format(Date(ts))
      }
      val timeLabel = timeFormat.format(Date(ts))
      val dateGroupStr = getDateGroupLabel(ts)

      return FocusSessionHistoryItem(
        id = "demo_${ts}_$duration",
        durationMinutes = duration,
        selectedDurationMinutes = duration,
        sessionType = "Focus session",
        timestampFormatted = "$dateLabel · $timeLabel",
        dateGroup = dateGroupStr,
        isCompleted = true,
        timestampMillis = ts
      )
    }

    val demoItems = listOf(
      // Today
      createSession(0, 9, 30, 25),
      createSession(0, 11, 15, 10),
      createSession(0, 14, 0, 40),
      // Yesterday
      createSession(1, 10, 0, 20),
      createSession(1, 16, 30, 35),
      // 2 days ago
      createSession(2, 9, 0, 15),
      createSession(2, 13, 15, 60),
      // 3 days ago
      createSession(3, 15, 45, 5),
      // 4 days ago
      createSession(4, 10, 30, 30),
      createSession(4, 16, 0, 45),
      // 5 days ago (1-minute completed session)
      createSession(5, 11, 0, 1),
      // 6 days ago
      createSession(6, 14, 30, 25),
      // Older sessions in current month
      createSession(10, 10, 0, 40),
      createSession(14, 15, 30, 50),
      createSession(18, 9, 15, 30),
      createSession(22, 16, 0, 45)
    )

    _uiState.update { state ->
      val updatedHistory = (demoItems + state.historyItems).distinctBy { it.id }
      val stateWithHistory = state.copy(historyItems = updatedHistory)
      recalculateTodayStats(stateWithHistory)
    }
    saveCurrentState()
  }

  fun clearDemoData() {
    _uiState.update { state ->
      val nonDemoHistory = state.historyItems.filter { !it.id.startsWith("demo_") }
      val stateWithHistory = state.copy(historyItems = nonDemoHistory)
      recalculateTodayStats(stateWithHistory)
    }
    saveCurrentState()
  }
}
