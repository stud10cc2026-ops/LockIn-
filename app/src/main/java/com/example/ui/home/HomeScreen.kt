package com.example.ui.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import com.example.data.DailyGoal
import com.example.widget.ProgressIndicatorWidgetProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.auth.AccountScreen
import com.example.ui.auth.AuthModalOverlay
import com.example.ui.history.HistoryContent
import com.example.ui.notifications.NotificationScreen
import com.example.ui.session.ActiveSessionScreen
import com.example.ui.settings.SettingsContent
import com.example.ui.stats.StatsContent
import com.example.ui.welcome.WelcomeIntroScreen
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ContainerNeutral
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextOffWhite
import com.example.ui.theme.DarkTextOnLime
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.LightContainerInner
import com.example.ui.theme.LightContainerNeutral
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MutedTextSecondary
import com.example.ui.theme.SignatureLimeAccent
import com.example.ui.theme.SignatureNeonLime
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.SubtleCaption
import com.example.ui.theme.WarmOffWhite
import com.example.util.AppBlockerHelper
import android.Manifest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.em
import com.example.ui.theme.RefActiveTabBlue
import com.example.ui.theme.RefNavBackground
import com.example.ui.theme.RefNavBorder
import com.example.ui.theme.RefPageBackgroundTop
import com.example.ui.theme.RefPageBackgroundBottom
import com.example.ui.theme.RefHeadlineGray
import com.example.ui.theme.RefNavIconGray
import com.example.ui.theme.RefNearBlack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import com.example.ui.habits.HabitsContent
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.util.NotificationHelper

private val ThinCalendarOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinCalendarOutline",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
  ).path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 1.5f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round
  ) {
    moveTo(6.5f, 5f)
    lineTo(17.5f, 5f)
    curveTo(18.88f, 5f, 20f, 6.12f, 20f, 7.5f)
    lineTo(20f, 18.5f)
    curveTo(20f, 19.88f, 18.88f, 21f, 17.5f, 21f)
    lineTo(6.5f, 21f)
    curveTo(5.12f, 21f, 4f, 19.88f, 4f, 18.5f)
    lineTo(4f, 7.5f)
    curveTo(4f, 6.12f, 5.12f, 5f, 6.5f, 5f)
    close()
    moveTo(4f, 10f)
    lineTo(20f, 10f)
    moveTo(8f, 3f)
    lineTo(8f, 6f)
    moveTo(16f, 3f)
    lineTo(16f, 6f)
  }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  val systemInDark = isSystemInDarkTheme()
  val effectiveNightMode = when (state.themeMode) {
    "DARK" -> true
    "LIGHT" -> false
    else -> systemInDark
  }

  BackHandler(enabled = state.isNotificationPageOpen) {
    viewModel.setNotificationPageOpen(false)
  }
  BackHandler(enabled = state.isAccountPageOpen) {
    viewModel.setAccountPageOpen(false)
  }
  BackHandler(enabled = state.activeTab != 0 && !state.isNotificationPageOpen && !state.isAccountPageOpen) {
    viewModel.setActiveTab(0)
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.onSystemPermissionResult(isGranted)
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.syncSystemNotificationPermission(context)
        viewModel.checkAppBlockerPermissionsOnResume(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  LaunchedEffect(Unit) {
    viewModel.requestPermissionEvent.collect {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        NotificationHelper.openNotificationSettings(context)
      }
    }
  }

  val isNightMode = state.isNightMode
  val backgroundBrush = if (isNightMode) {
    Brush.verticalGradient(listOf(DarkBackground, DarkBackground))
  } else {
    Brush.verticalGradient(
      colors = listOf(RefPageBackgroundTop, RefPageBackgroundBottom),
      startY = 0f,
      endY = Float.POSITIVE_INFINITY
    )
  }
  var showPremiumPopup by remember { mutableStateOf(false) }
  var showCustomDurationDialog by remember { mutableStateOf(false) }
  var timezoneToastMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(timezoneToastMessage) {
    if (timezoneToastMessage != null) {
      kotlinx.coroutines.delay(2500)
      timezoneToastMessage = null
    }
  }

  if (state.showWelcomeIntro) {
    WelcomeIntroScreen(
      isNightMode = state.isNightMode,
      onContinue = {
        viewModel.onWelcomeContinueClicked(context)
      },
      modifier = modifier
    )
  } else if (state.isSessionActive) {
    ActiveSessionScreen(
      durationMinutes = state.effectiveDurationMinutes,
      remainingSeconds = state.remainingSeconds,
      totalSeconds = state.totalSessionSeconds,
      isPaused = state.isSessionPaused,
      blockedAppName = state.blockedAppName,
      isNightMode = effectiveNightMode,
      themeMode = state.themeMode,
      onThemeModeSelected = { mode -> viewModel.setThemeMode(mode) },
      onNightModeToggle = { isNight -> viewModel.setNightMode(isNight) },
      onClearBlockedAppAlert = { viewModel.clearBlockedAppAlert() },
      onPauseResumeClick = { viewModel.togglePauseResumeFocusSession() },
      onEndSessionClick = { viewModel.endFocusSession() },
      modifier = modifier
    )
  } else {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(backgroundBrush)
    ) {
      AnimatedContent(
        targetState = state.activeTab,
        transitionSpec = {
          fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
          slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> height / 30 } togetherWith
          fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        modifier = Modifier
          .navigationBarsPadding()
          .imePadding(),
        label = "MainTabTransition"
      ) { targetTab ->
        when (targetTab) {
          1 -> {
            // HISTORY SCREEN
            HistoryContent(
              historyItems = state.historyItems.filter { it.isCompleted },
              isNightMode = isNightMode,
              onItemClick = { /* Detail view */ }
            )
          }
          2 -> {
            // STATS SCREEN
            val computed = viewModel.getComputedStats()
            StatsContent(
              weeklyTotalFormatted = computed.weeklyTotalFormatted,
              monthlyTotalFormatted = computed.monthlyTotalFormatted,
              totalSessions = computed.totalSessions,
              avgSessionFormatted = computed.avgSessionFormatted,
              bestSessionFormatted = computed.bestSessionFormatted,
              activeDaysCount = computed.activeDaysCount,
              totalDaysInPeriod = computed.totalDaysInPeriod,
              insightMessage = computed.insightMessage,
              weekDays = computed.weekDays,
              isNightMode = isNightMode
            )
          }
          3 -> {
            // HABITS SCREEN
            HabitsContent(
              userName = state.userName,
              isNightMode = isNightMode,
              selectedTimezoneId = state.selectedTimezoneId,
              activeGoalTitle = state.dailyGoal?.title ?: "",
              onStartLockInForHabit = { habit -> viewModel.startFocusSessionForHabit(habit, context) }
            )
          }
          4 -> {
            // SETTINGS SCREEN
            SettingsContent(
              userName = state.userName,
              isLoggedIn = state.isLoggedIn,
              userEmail = state.userEmail,
              isNightMode = state.isNightMode,
              defaultDurationMinutes = state.defaultDurationMinutes,
              allowPause = state.allowPause,
              focusReminders = state.focusReminders,
              notificationsEnabled = state.notificationsEnabled,
              onNotificationsToggle = { viewModel.setNotificationsEnabled(it, context) },
              onUpdateUserName = { viewModel.setUserName(it) },
              onNightModeToggle = { viewModel.setNightMode(it) },
              onDefaultDurationSelect = { viewModel.setDefaultDurationMinutes(it) },
              onAllowPauseToggle = { viewModel.setAllowPause(it) },
              onFocusRemindersToggle = { viewModel.setFocusReminders(it) },
              onOpenNotificationsPage = { viewModel.setNotificationPageOpen(true) },
              onOpenAccountPage = { viewModel.setAccountPageOpen(true) },
              onOpenAuthScreen = { viewModel.openAuthScreen(it) },
              onLogout = { viewModel.logout() },
              onResetAppData = { viewModel.resetAppData() },
              onGenerateDemoData = { viewModel.generateDemoData() },
              onClearDemoData = { viewModel.clearDemoData() }
            )
          }
          else -> {
            // HOMEPAGE (TAB 0 and fallback)
            val headerAlpha = remember { Animatable(0f) }
            val headerTranslationY = remember { Animatable(12f) }
            val mainContentAlpha = remember { Animatable(0f) }
            val mainContentTranslationY = remember { Animatable(16f) }

            LaunchedEffect(Unit) {
              launch {
                headerAlpha.animateTo(1f, animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing))
              }
              launch {
                headerTranslationY.animateTo(0f, animationSpec = tween(300, delayMillis = 0, easing = FastOutSlowInEasing))
              }
              launch {
                mainContentAlpha.animateTo(1f, animationSpec = tween(340, delayMillis = 70, easing = FastOutSlowInEasing))
              }
              launch {
                mainContentTranslationY.animateTo(0f, animationSpec = tween(340, delayMillis = 70, easing = FastOutSlowInEasing))
              }
            }

            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp) // Extra space for floating nav
            ) {
              Spacer(modifier = Modifier.height(14.dp))

              // 1. TOP HEADER
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .graphicsLayer {
                    alpha = headerAlpha.value
                    translationY = headerTranslationY.value.dp.toPx()
                  }
              ) {
                RefTopBar(
                  userName = state.userName,
                  selectedCountry = state.selectedCountry,
                  selectedTimezoneId = state.selectedTimezoneId,
                  isNightMode = isNightMode,
                  onDateClick = { viewModel.openCountryPicker() },
                  onProfileClick = { viewModel.setAccountPageOpen(true) },
                  onNotificationClick = { viewModel.setNotificationPageOpen(true) }
                )
              }

              Spacer(modifier = Modifier.height(24.dp))

              // MAIN HOMEPAGE CONTENT (STAGGERED)
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .graphicsLayer {
                    alpha = mainContentAlpha.value
                    translationY = mainContentTranslationY.value.dp.toPx()
                  }
              ) {
                // 2. MAIN HERO HEADLINE
                MainHeroSection(
                  userName = state.userName,
                  isLoggedIn = state.isLoggedIn,
                  isNightMode = isNightMode
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. FOCUS SESSION PANEL
                FocusSessionPanel(
                  selectedMinutes = state.selectedDurationMinutes,
                  isCustom = state.isCustomDuration,
                  customMinutes = state.customMinutes,
                  isNightMode = isNightMode,
                  onSelectPreset = { viewModel.selectPresetDuration(it) },
                  onSelectCustom = { viewModel.toggleCustomDuration() },
                  onCustomMinutesChange = { viewModel.updateCustomMinutes(it) },
                  onShowCustomDialog = { showCustomDurationDialog = true }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 5. PRIMARY ACTION CTA
                PrimaryActionButton(
                  onClick = { viewModel.onStartFocusSessionRequested(context) },
                  durationMinutes = state.effectiveDurationMinutes,
                  isNightMode = isNightMode
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 6. DAILY SNAPSHOT
                DailySnapshotSection(
                  todayFocusFormatted = state.todayFocusFormatted,
                  sessionsCount = state.todaySessionsCount,
                  selectedDurationMinutes = state.effectiveDurationMinutes,
                  isNightMode = isNightMode,
                  onNavigateToHistory = { viewModel.setActiveTab(1) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7. COMPACT PREMIUM UPGRADE SECTION
                PremiumUpgradeCard(
                  isNightMode = isNightMode,
                  onClick = { showPremiumPopup = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
              }
            }
          }
        }
      }

      // Notification Page Overlay
      AnimatedVisibility(
        visible = state.isNotificationPageOpen,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 25 },
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(150)) { it / 25 }
      ) {
        NotificationScreen(
          isNightMode = state.isNightMode,
          notificationsEnabled = state.notificationsEnabled,
          appNotifications = state.appNotifications,
          pendingDeletedNotification = state.pendingDeletedNotification,
          isSystemPermissionGranted = NotificationHelper.isSystemPermissionGranted(context),
          onBackClick = { viewModel.setNotificationPageOpen(false) },
          onNotificationsEnabledToggle = { viewModel.setNotificationsEnabled(it, context) },
          onDeleteNotification = { viewModel.deleteNotification(it) },
          onUndoDeleteNotification = { viewModel.undoDeleteNotification() },
          onOpenSystemSettings = { NotificationHelper.openNotificationSettings(context) },
          focusReminders = state.focusReminders,
          sessionCompletedReminder = state.sessionCompletedReminder,
          habitRemindersEnabled = state.habitRemindersEnabled,
          onFocusRemindersToggle = { viewModel.setFocusReminders(it) },
          onSessionCompletedToggle = { viewModel.setSessionCompletedReminder(it) },
          onHabitRemindersToggle = { viewModel.setHabitRemindersEnabled(it) },
          modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
        )
      }

      // Dedicated Account Screen Overlay
      AnimatedVisibility(
        visible = state.isAccountPageOpen,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { it / 25 },
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(150)) { it / 25 }
      ) {
        AccountScreen(
          isLoggedIn = state.isLoggedIn,
          userName = state.userName,
          userEmail = state.userEmail,
          isNightMode = state.isNightMode,
          onBackClick = { viewModel.setAccountPageOpen(false) },
          onSignUp = { name, email, pass ->
            viewModel.signUp(name, email, pass)
          },
          onUpdatePassword = { newPass ->
            viewModel.updatePassword(newPass)
          },
          onLogin = { email, pass ->
            viewModel.login(email, pass)
          },
          onSendPasswordReset = { email ->
            viewModel.sendPasswordReset(email)
          },
          onLogout = {
            viewModel.logout()
          },
          onUpdateName = {
            viewModel.setUserName(it)
          },
          onManualSync = {
            viewModel.triggerManualSync()
          },
          modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
        )
      }

      // SOFT BOTTOM FADE OVERLAY
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .align(Alignment.BottomCenter)
          .background(
            if (isNightMode) {
              Brush.verticalGradient(
                0.0f to Color(0x0013151A),
                0.55f to Color(0xB313151A),
                1.0f to Color(0xFF13151A)
              )
            } else {
              Brush.verticalGradient(
                0.0f to Color(0x00F6F8FA),
                0.55f to Color(0xB3F6F8FA),
                1.0f to Color(0xFFF6F8FA)
              )
            }
          )
      )

      // INTEGRATED FLOATING BOTTOM NAVIGATION BAR
      RefFloatingBottomNavigation(
        activeTab = state.activeTab,
        isNightMode = state.isNightMode,
        onTabSelected = { viewModel.setActiveTab(it) },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 16.dp) // 16px above bottom safe area
          .padding(horizontal = 12.dp) // 12px from left/right
      )

      // Auth Modal (Sign In / Sign Up)
      AuthModalOverlay(
        isOpen = state.isAuthScreenOpen,
        initialMode = state.authMode,
        isNightMode = state.isNightMode,
        onDismiss = { viewModel.closeAuthScreen() },
        onSignUp = { name, email, pass ->
          viewModel.signUp(name, email, pass)
        },
        onUpdatePassword = { newPass ->
          viewModel.updatePassword(newPass)
        },
        onLogin = { email, pass ->
          viewModel.login(email, pass)
        },
        onSendPasswordReset = { email ->
          viewModel.sendPasswordReset(email)
        }
      )

      // App Blocker Permission Request Dialog
      AnimatedVisibility(
        visible = state.showPermissionPrompt,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200), initialScale = 0.95f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150), targetScale = 0.95f)
      ) {
        FocusPermissionDialog(
          isNightMode = state.isNightMode,
          hasUsagePermission = AppBlockerHelper.isUsageStatsPermissionGranted(context),
          hasOverlayPermission = AppBlockerHelper.isOverlayPermissionGranted(context),
          onGrantUsagePermission = { AppBlockerHelper.openUsageAccessSettings(context) },
          onGrantOverlayPermission = { AppBlockerHelper.openOverlaySettings(context) },
          onDismiss = { viewModel.dismissPermissionPrompt() }
        )
      }

      AnimatedVisibility(
        visible = showCustomDurationDialog,
        enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
      ) {
        CustomDurationInputDialog(
          initialMinutes = state.customMinutes,
          isNightMode = isNightMode,
          onConfirm = { minutes ->
            viewModel.updateCustomMinutes(minutes)
            showCustomDurationDialog = false
          },
          onDismiss = { showCustomDurationDialog = false }
        )
      }

      AnimatedVisibility(
        visible = showPremiumPopup,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200), initialScale = 0.95f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150), targetScale = 0.95f)
      ) {
        PremiumUpgradePopup(
          isNightMode = isNightMode,
          onDismiss = { showPremiumPopup = false }
        )
      }

      // Country / Timezone Selection Dialog
      AnimatedVisibility(
        visible = state.isCountryPickerOpen,
        enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
      ) {
        CountryTimezoneDialog(
          currentCountry = state.selectedCountry,
          currentTimezoneId = state.selectedTimezoneId,
          isNightMode = isNightMode,
          onDismiss = { viewModel.closeCountryPicker() },
          onSave = { countryName, timezoneId ->
            viewModel.setSelectedCountryAndTimezone(countryName, timezoneId)
            val displayName = if (!countryName.isNullOrEmpty() && countryName != "Use Device Default") countryName else "Device Default"
            timezoneToastMessage = "$displayName saved"
          }
        )
      }

      // Confirmation Toast Notification for Timezone / Country saved
      AnimatedVisibility(
        visible = timezoneToastMessage != null,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(200)),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 100.dp)
          .navigationBarsPadding()
      ) {
        if (timezoneToastMessage != null) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF1A1A1F),
            border = BorderStroke(1.dp, Color(0xFF3A3A44)),
            shadowElevation = 8.dp,
            modifier = Modifier
              .padding(horizontal = 24.dp)
              .testTag("timezone_saved_toast")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = timezoneToastMessage!!,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp
                ),
                color = Color.White
              )
            }
          }
        }
      }

    }
  }
}



/**
 * Press Scale Animation Modifier
 */
@Composable
fun Modifier.pressScale(onClick: () -> Unit): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1f,
    label = "PressScale"
  )
  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick
    )
}

@Composable
private fun RefTopBar(
  userName: String,
  selectedCountry: String?,
  selectedTimezoneId: String?,
  isNightMode: Boolean,
  onDateClick: () -> Unit,
  onProfileClick: () -> Unit,
  onNotificationClick: () -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) DarkTextOffWhite else RefNearBlack
  val iconColor = if (isNightMode) DarkTextOffWhite else RefNavIconGray

  val timeZone = remember(selectedTimezoneId) {
    if (selectedTimezoneId.isNullOrEmpty() || selectedTimezoneId == "DEVICE_DEFAULT") {
      java.util.TimeZone.getDefault()
    } else {
      java.util.TimeZone.getTimeZone(selectedTimezoneId)
    }
  }

  val today = remember(timeZone) {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    sdf.timeZone = timeZone
    sdf.format(Date())
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .clip(RoundedCornerShape(22.dp))
        .clickable { onDateClick() }
        .testTag("home_date_timezone_picker_trigger")
    ) {
      // Calendar Button (Clickable)
      Box(
        modifier = Modifier
          .size(44.dp)
          .shadow(if (isNightMode) 0.dp else 4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
          .background(surfaceColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = ThinCalendarOutlineIcon,
          contentDescription = "Date Icon",
          tint = iconColor,
          modifier = Modifier.size(22.dp)
        )
      }

      // Date Pill with down-chevron icon (Clickable)
      Box(
        modifier = Modifier
          .height(44.dp)
          .shadow(if (isNightMode) 0.dp else 4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.1f))
          .background(surfaceColor, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(start = 16.dp, end = 12.dp)
        ) {
          Text(
            text = today,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor
          )
          Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Select Timezone",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Notification Bell
      Box(
        modifier = Modifier
          .size(44.dp)
          .shadow(if (isNightMode) 0.dp else 4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
          .background(surfaceColor, CircleShape)
          .pressScale(onClick = onNotificationClick),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Notifications,
          contentDescription = "Notifications",
          tint = iconColor,
          modifier = Modifier.size(22.dp)
        )
      }

      // Profile Button
      Box(
        modifier = Modifier
          .size(44.dp)
          .shadow(if (isNightMode) 0.dp else 4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.1f))
          .background(surfaceColor, CircleShape)
          .pressScale(onClick = onProfileClick),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Person,
          contentDescription = "Profile",
          tint = iconColor,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}

/**
 * Main Hero Section: Two-tone headline.
 */
@Composable
private fun MainHeroSection(userName: String, isLoggedIn: Boolean, isNightMode: Boolean) {
  val nameToUse = userName.ifBlank { "Guest" }
  
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "Hello $nameToUse,",
      style = MaterialTheme.typography.displayMedium.copy(
        fontSize = 26.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 1.15.em,
        letterSpacing = (-0.26).sp, // -0.01em
        color = if (isNightMode) DarkTextSecondary else RefHeadlineGray
      )
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "Ready to lock in?",
      style = MaterialTheme.typography.displayLarge.copy(
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 1.15.em,
        letterSpacing = (-0.64).sp, // -0.02em
        color = if (isNightMode) DarkTextPrimary else RefNearBlack
      )
    )
  }
}

private data class NavTabData(
  val id: Int,
  val icon: ImageVector,
  val activeIcon: ImageVector
)

private sealed class NavItemType(val key: Any) {
  data class Tab(val data: NavTabData, val isSelected: Boolean, val endPadding: Dp) : NavItemType(data.id)
  data class SpacerItem(val keyName: String) : NavItemType(keyName)
}

@Composable
private fun RefFloatingBottomNavigation(
  activeTab: Int,
  isNightMode: Boolean,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else RefNavBorder

  val allNavTabs = remember {
    listOf(
      NavTabData(0, Icons.Outlined.Home, Icons.Filled.Home),
      NavTabData(1, Icons.Outlined.History, Icons.Outlined.History),
      NavTabData(2, Icons.Outlined.BarChart, Icons.Filled.BarChart),
      NavTabData(3, Icons.Outlined.Checklist, Icons.Filled.Checklist),
      NavTabData(4, Icons.Outlined.Settings, Icons.Filled.Settings)
    )
  }

  val activeTabData = allNavTabs.firstOrNull { it.id == activeTab } ?: allNavTabs[0]
  val rightTabs = remember(activeTab) { allNavTabs.filter { it.id != activeTab } }

  val activePillBg = if (isNightMode) Color(0xFF2C3038) else RefActiveTabBlue
  val activeIconTint = if (isNightMode) Color.White else RefNearBlack
  val inactiveIconTint = if (isNightMode) DarkTextSecondary else RefNavIconGray

  var isTabChanging by remember { mutableStateOf(false) }
  val pillScale by animateFloatAsState(
    targetValue = if (isTabChanging) 0.88f else 1f,
    animationSpec = spring(stiffness = 380f, dampingRatio = 0.68f),
    finishedListener = { isTabChanging = false },
    label = "PillSettleScale"
  )

  LaunchedEffect(activeTab) {
    isTabChanging = true
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(58.dp)
      .border(1.dp, borderColor, RoundedCornerShape(29.dp)),
    shape = RoundedCornerShape(29.dp),
    color = bgColor,
    shadowElevation = 0.dp // No shadow
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. ACTIVE TAB PILL (FAR LEFT, FIXED UNCOMPRESSIBLE SIZE, SETTLE SCALE ANIMATION)
      Box(
        modifier = Modifier
          .requiredSize(44.dp)
          .scale(pillScale)
          .clip(CircleShape)
          .background(activePillBg),
        contentAlignment = Alignment.Center
      ) {
        Crossfade(
          targetState = activeTabData,
          animationSpec = tween(220, easing = FastOutSlowInEasing),
          label = "ActivePillIconCrossfade"
        ) { tabData ->
          Icon(
            imageVector = tabData.activeIcon,
            contentDescription = null,
            tint = activeIconTint,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // 2. DYNAMIC SPACER PUSHING INACTIVE TABS TO THE RIGHT
      Spacer(modifier = Modifier.weight(1f))

      // 3. GROUPED INACTIVE TABS ON THE RIGHT
      Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        rightTabs.forEach { tab ->
          Box(
            modifier = Modifier
              .requiredSize(40.dp)
              .clip(CircleShape)
              .clickable { onTabSelected(tab.id) },
            contentAlignment = Alignment.Center
          ) {
            Crossfade(
              targetState = tab,
              animationSpec = tween(200, easing = FastOutSlowInEasing),
              label = "InactiveIconCrossfade"
            ) { targetTab ->
              Icon(
                imageVector = targetTab.icon,
                contentDescription = null,
                tint = inactiveIconTint,
                modifier = Modifier.size(22.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun NavItem(
  icon: ImageVector,
  activeIcon: ImageVector,
  isSelected: Boolean,
  isNightMode: Boolean,
  onClick: () -> Unit
) {
  val iconColor = if (isSelected) {
    if (isNightMode) Color.White else RefNearBlack
  } else {
    if (isNightMode) DarkTextSecondary else RefNavIconGray
  }

  var scaleTarget by remember(isSelected) { mutableFloatStateOf(if (isSelected) 1.05f else 1.0f) }

  LaunchedEffect(isSelected) {
    if (isSelected) {
      scaleTarget = 1.05f
      kotlinx.coroutines.delay(180)
      scaleTarget = 1.0f
    } else {
      scaleTarget = 1.0f
    }
  }

  val animatedScale by animateFloatAsState(
    targetValue = scaleTarget,
    animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
    label = "ActiveTabScale"
  )

  Box(
    modifier = Modifier
      .size(46.dp)
      .graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
      }
      .clip(CircleShape)
      .background(if (isSelected && !isNightMode) RefActiveTabBlue else Color.Transparent)
      .pressScale(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (isSelected) activeIcon else icon,
      contentDescription = null,
      tint = iconColor,
      modifier = Modifier.size(22.dp)
    )
  }
}

/**
 * Focus Session Panel: Clean grouped surface holding duration controls.
 */
@Composable
private fun FocusSessionPanel(
  selectedMinutes: Int,
  isCustom: Boolean,
  customMinutes: Int,
  isNightMode: Boolean,
  onSelectPreset: (Int) -> Unit,
  onSelectCustom: () -> Unit,
  onCustomMinutesChange: (Int) -> Unit,
  onShowCustomDialog: () -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier.padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.HourglassEmpty,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = "Focus Session",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp
            ),
            color = textColor
          )
        }
      }

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = "Choose how long you want to stay locked in.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        color = mutedColor
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val durationPresets = listOf(25, 40, 60)
        durationPresets.forEach { minutes ->
          val isSelected = !isCustom && selectedMinutes == minutes
          DurationPill(
            label = "$minutes min",
            isSelected = isSelected,
            isNightMode = isNightMode,
            onClick = { onSelectPreset(minutes) },
            modifier = Modifier.weight(1f)
          )
        }
        DurationPill(
          label = "Custom",
          isSelected = isCustom,
          isNightMode = isNightMode,
          onClick = { onSelectCustom() },
          modifier = Modifier.weight(1f)
        )
      }

      AnimatedVisibility(visible = isCustom) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Custom Duration",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = mutedColor
            )

            Surface(
              onClick = onShowCustomDialog,
              shape = RoundedCornerShape(8.dp),
              color = containerColor,
              border = BorderStroke(1.dp, borderColor)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = "$customMinutes min",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = textColor
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Slider(
            value = customMinutes.coerceIn(1, 300).toFloat(),
            onValueChange = { onCustomMinutesChange(it.toInt()) },
            valueRange = 5f..300f,
            colors = SliderDefaults.colors(
              thumbColor = if (isNightMode) Color.White else DarkButtonCharcoal,
              activeTrackColor = if (isNightMode) Color.White else DarkButtonCharcoal,
              activeTickColor = if (isNightMode) DarkButtonCharcoal else DarkButtonCharcoal,
              inactiveTrackColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder,
              inactiveTickColor = if (isNightMode) Color.White.copy(alpha = 0.5f) else DarkButtonCharcoal.copy(alpha = 0.35f)
            )
          )
        }
      }
    }
  }
}

@Composable
private fun CustomDurationInputDialog(
  initialMinutes: Int,
  isNightMode: Boolean,
  onConfirm: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  var textValue by remember { mutableStateOf(initialMinutes.toString()) }
  val cardBg = Color.White
  val textColor = Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)
  val inputBg = Color(0xFFEEF1F5)
  val borderColor = Color(0xFFE3E9F0)

  val parsedInt = textValue.toIntOrNull()
  val isTooHigh = parsedInt != null && parsedInt > 300
  val isTooLow = parsedInt != null && parsedInt < 1
  val isInvalid = textValue.isBlank() || isTooHigh || isTooLow

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x73141A26))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .shadow(
          elevation = 32.dp,
          shape = RoundedCornerShape(24.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = cardBg,
      shadowElevation = 0.dp
    ) {
      Column(modifier = Modifier.padding(28.dp)) {
        Text(
          text = "Enter Custom Duration",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Set your focus time in minutes (1 to 300 min):",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.W400
          ),
          color = mutedColor
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it.filter { char -> char.isDigit() } },
            singleLine = true,
            isError = isTooHigh || isTooLow,
            label = { Text("Minutes") },
            textStyle = androidx.compose.ui.text.TextStyle(
              color = textColor,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = inputBg,
              unfocusedContainerColor = inputBg,
              focusedBorderColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else Color(0xFF1A1A1F),
              unfocusedBorderColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else borderColor,
              errorBorderColor = Color(0xFFD93838),
              errorLabelColor = Color(0xFFD93838),
              focusedLabelColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else Color(0xFF1A1A1F),
              unfocusedLabelColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else mutedColor,
              focusedTextColor = textColor,
              unfocusedTextColor = textColor,
              cursorColor = Color(0xFF1A1A1F),
              selectionColors = TextSelectionColors(
                handleColor = Color(0xFF1A1A1F),
                backgroundColor = Color(0xFF1A1A1F).copy(alpha = 0.2f)
              )
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )

          if (isTooHigh || isTooLow) {
            val errorMsg = if (isTooHigh) "Duration exceeds 300 min. Please reduce the number." else "Duration must be at least 1 min."
            Text(
              text = errorMsg,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
              color = Color(0xFFD93838)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFEEF1F5),
              contentColor = Color(0xFF1A1A1F)
            )
          ) {
            Text(
              text = "Cancel",
              fontWeight = FontWeight.W600
            )
          }
          Button(
            enabled = !isInvalid,
            onClick = {
              if (parsedInt != null && parsedInt in 1..300) {
                onConfirm(parsedInt)
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF1A1A1F),
              contentColor = Color.White
            ),
            shape = CircleShape
          ) {
            Text(
              text = "Save",
              fontWeight = FontWeight.W600
            )
          }
        }
      }
    }
  }
}

/**
 * Session Started Confirmation Dialog/Overlay.
 */
@Composable
private fun SessionStartedOverlay(
  durationMinutes: Int,
  isNightMode: Boolean,
  onDismiss: () -> Unit
) {
  val cardBg = Color.White
  val textColor = Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)
  val containerColor = Color(0xFFF6F8FA)
  val borderColor = Color(0xFFE3E9F0)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x73141A26))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .shadow(
          elevation = 32.dp,
          shape = RoundedCornerShape(24.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 0.dp
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (isNightMode) Color.White else Color(0xFF1A1A1F),
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Session Configured",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Ready to stay locked in for $durationMinutes minutes.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
          color = mutedColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A1A1F),
            contentColor = Color.White
          )
        ) {
          Text(
            text = "Back to Home",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
          )
        }
      }
    }
  }
}

/**
 * Duration Pill for Focus Panel.
 */
@Composable
private fun DurationPill(
  label: String,
  isSelected: Boolean,
  isNightMode: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val surfaceColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) Color.White else DarkButtonCharcoal
    } else {
      if (isNightMode) DarkContainerNeutral else LightContainerNeutral
    },
    animationSpec = tween(durationMillis = 200)
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) Color.Black else Color.White
    } else {
      if (isNightMode) DarkTextSecondary else LightTextPrimary
    },
    animationSpec = tween(durationMillis = 200)
  )

  val borderColor by animateColorAsState(
    targetValue = if (isSelected) {
      Color.Transparent
    } else {
      if (isNightMode) DarkSubtleBorder else LightSubtleBorder
    },
    animationSpec = tween(durationMillis = 200)
  )

  Box(
    modifier = modifier
      .height(38.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(surfaceColor)
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .pressScale(onClick = onClick)
      .testTag("duration_pill_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.5.sp
      ),
      color = textColor
    )
  }
}

/**
 * Primary Action Button: Clean, bold CTA with right arrow.
 */
@Composable
private fun PrimaryActionButton(
  onClick: () -> Unit,
  durationMinutes: Int,
  isNightMode: Boolean
) {
  val btnBg = if (isNightMode) Color.White else DarkButtonCharcoal
  val contentColor = if (isNightMode) Color.Black else Color.White

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .background(btnBg, RoundedCornerShape(14.dp))
      .pressScale(onClick = onClick)
      .testTag("start_focus_button"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Start Focus Session",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 15.5.sp
        ),
        color = contentColor
      )

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

/**
 * Daily Snapshot Section: Minimal summary stats in a clean card.
 */
@Composable
private fun DailySnapshotSection(
  todayFocusFormatted: String,
  sessionsCount: Int,
  selectedDurationMinutes: Int,
  isNightMode: Boolean,
  onNavigateToHistory: () -> Unit = {}
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val arrowBg = if (isNightMode) DarkButtonCharcoal else LightContainerInner
  val arrowTint = if (isNightMode) DarkTextOffWhite else LightTextPrimary

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .pressScale(onClick = onNavigateToHistory)
      .testTag("todays_focus_card"),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Column {
          Text(
            text = "Today's focus",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.5.sp
            ),
            color = textColor
          )
          Text(
            text = "$sessionsCount sessions completed",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = mutedColor
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.HourglassEmpty,
              contentDescription = null,
              tint = mutedColor,
              modifier = Modifier.size(15.dp)
            )
            Column {
              Text(
                text = todayFocusFormatted,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp
                ),
                color = textColor
              )
              Text(
                text = "Focus time",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = mutedColor
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.BarChart,
              contentDescription = null,
              tint = mutedColor,
              modifier = Modifier.size(15.dp)
            )
            Column {
              Text(
                text = "$sessionsCount",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp
                ),
                color = textColor
              )
              Text(
                text = "Sessions",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = mutedColor
              )
            }
          }
        }
      }

      // Arrow mark on the right side - styled like action arrow
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(arrowBg, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "View History",
          tint = arrowTint,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

/**
 * Integrated Bottom Navigation Bar: Clean white rounded floating navigation container
 * with dark charcoal pill highlight for the active destination.
 */
@Composable
private fun FloatingBottomNavigation(
  activeTab: Int,
  isNightMode: Boolean = false,
  onTabSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val destinations = listOf(
    NavDestination("Home", Icons.Outlined.Home),
    NavDestination("History", Icons.Outlined.History),
    NavDestination("Stats", Icons.Outlined.BarChart),
    NavDestination("Settings", Icons.Outlined.Settings)
  )

  val navBgColor = if (isNightMode) DarkCardSurface else Color.White
  val navBorderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val selectedPillBg = if (isNightMode) Color(0xFF2C2F36) else DarkButtonCharcoal
  val selectedContentColor = Color.White
  val unselectedIconColor = if (isNightMode) DarkTextSecondary else DarkButtonCharcoal

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RectangleShape,
    color = navBgColor,
    border = BorderStroke(1.dp, navBorderColor),
    shadowElevation = 8.dp,
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
    destinations.forEachIndexed { index, dest ->
      val isSelected = index == activeTab
      val animatedWeight by animateFloatAsState(
        targetValue = if (isSelected) 1.55f else 1.0f,
        animationSpec = spring(
          stiffness = Spring.StiffnessMediumLow,
          dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "nav_weight_${dest.label}"
      )
      val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) selectedPillBg else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_bg_${dest.label}"
      )
      val iconTint by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedIconColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_tint_${dest.label}"
      )

      Box(
        modifier = Modifier
          .weight(animatedWeight)
          .height(48.dp)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onTabSelected(index) }
          .testTag("nav_tab_${dest.label.lowercase()}"),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(pillBgColor)
            .then(
              if (isSelected && isNightMode) Modifier.border(1.dp, DarkSubtleBorder, RoundedCornerShape(24.dp))
              else Modifier
            )
            .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = dest.icon,
              contentDescription = dest.label,
              tint = iconTint,
              modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(
              visible = isSelected,
              enter = fadeIn(animationSpec = tween(160, delayMillis = 50)) + expandHorizontally(
                animationSpec = spring(
                  stiffness = Spring.StiffnessMediumLow,
                  dampingRatio = Spring.DampingRatioNoBouncy
                )
              ),
              exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(
                animationSpec = spring(
                  stiffness = Spring.StiffnessMediumLow,
                  dampingRatio = Spring.DampingRatioNoBouncy
                )
              )
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = dest.label,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp
                  ),
                  color = selectedContentColor,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }
    }
  }
}
}

private data class NavDestination(val label: String, val icon: ImageVector)

@Composable
private fun FocusPermissionDialog(
  isNightMode: Boolean,
  hasUsagePermission: Boolean,
  hasOverlayPermission: Boolean,
  onGrantUsagePermission: () -> Unit,
  onGrantOverlayPermission: () -> Unit,
  onDismiss: () -> Unit
) {
  val cardBg = Color.White
  val textColor = Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)
  val containerColor = if (isNightMode) Color(0xFFF6F8FA) else Color(0xFFF6F8FA)
  val borderColor = Color(0xFFE3E9F0)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x73141A26))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .shadow(
          elevation = 32.dp,
          shape = RoundedCornerShape(24.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = cardBg,
      shadowElevation = 0.dp
    ) {
      Column(modifier = Modifier.padding(28.dp)) {
        Text(
          text = "Permissions Required",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Lock In needs these to block apps.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.W400
          ),
          color = mutedColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!hasUsagePermission) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFF6F8FA),
              border = BorderStroke(1.dp, borderColor),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "1. Usage Access Permission",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                  ),
                  color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Detects when blocked apps are opened while timer is active.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                  color = mutedColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = onGrantUsagePermission,
                  shape = CircleShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1F),
                    contentColor = Color.White
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                ) {
                  Text("Grant Usage Access", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
              }
            }
          }
          if (!hasOverlayPermission) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFF6F8FA),
              border = BorderStroke(1.dp, borderColor),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "2. Display Over Other Apps",
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                  ),
                  color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Shows the Lock In shield over restricted apps.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                  color = mutedColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = onGrantOverlayPermission,
                  shape = CircleShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1F),
                    contentColor = Color.White
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                ) {
                  Text("Grant Display Over Apps", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFEEF1F5),
              contentColor = Color(0xFF1A1A1F)
            )
          ) {
            Text("Cancel", fontWeight = FontWeight.W600, fontSize = 16.sp)
          }
        }
      }
    }
  }
}

/**
 * Compact Premium Upgrade Card on the Home Page
 */
@Composable
private fun PremiumUpgradeCard(
  isNightMode: Boolean,
  onClick: () -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("premium_upgrade_card"),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "Upgrade to Premium",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.5.sp
          ),
          color = textColor
        )
        Text(
          text = "Unlock more features and take your focus further.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.5.sp,
            lineHeight = 16.sp
          ),
          color = mutedColor
        )
      }

      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isNightMode) Color.White else DarkButtonCharcoal
      ) {
        Text(
          text = "Upgrade →",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          ),
          color = if (isNightMode) Color.Black else Color.White,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}

/**
 * Premium Upgrade Popup Dialog
 */
@Composable
private fun PremiumUpgradePopup(
  isNightMode: Boolean,
  onDismiss: () -> Unit
) {
  val surfaceBg = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x73141A26))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss
      ),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .shadow(
          elevation = 32.dp,
          shape = RoundedCornerShape(24.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = surfaceBg,
      border = if (isNightMode) BorderStroke(1.dp, borderColor) else null,
      shadowElevation = 0.dp
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon / Visual Element
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isNightMode) Color(0xFF282B33) else Color(0xFFF6F8FA)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Unlock Premium",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
          ),
          color = textColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Get more powerful tools to stay focused.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
          color = mutedColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Features List
        val features = listOf(
          "Advanced Stats",
          "Focus Profiles",
          "Focus Schedules",
          "Premium Themes",
          "More customization"
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          features.forEach { feature ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = if (isNightMode) Color.White.copy(alpha = 0.7f) else Color(0xFF1A1A1F).copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = feature,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Medium,
                  fontSize = 15.sp
                ),
                color = textColor
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Button(
          onClick = { /* Upgrade logic placeholder */ },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
            contentColor = if (isNightMode) Color.Black else Color.White
          )
        ) {
          Text(
            text = "Upgrade to Premium",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Text(
            text = "Not now",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            ),
            color = mutedColor
          )
        }
      }
    }
  }
}

