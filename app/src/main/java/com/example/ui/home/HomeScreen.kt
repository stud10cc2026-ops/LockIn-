package com.example.ui.home

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.DailyGoal
import com.example.ui.daily.DailyGoalDotGrid
import com.example.widget.DailyGoalWidgetProvider
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
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsStateWithLifecycle()

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
  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  var showDailyGoalDialog by remember { mutableStateOf(false) }
  var startWithEditName by remember { mutableStateOf(false) }

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
      onClearBlockedAppAlert = { viewModel.clearBlockedAppAlert() },
      onPauseResumeClick = { viewModel.togglePauseResumeFocusSession() },
      onEndSessionClick = { viewModel.endFocusSession() },
      onPushUpChallengeCompleted = { viewModel.completePushUpChallengeSession() },
      modifier = modifier
    )
  } else {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(bgColor)
        .navigationBarsPadding()
        .imePadding()
    ) {
      AnimatedContent(
        targetState = state.activeTab,
        transitionSpec = {
          fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
          slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> height / 30 } togetherWith
          fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 90.dp) // Space for integrated bottom navigation
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
                TopHeader(
                  userName = state.userName,
                  isLoggedIn = state.isLoggedIn,
                  hasUnreadNotifications = state.hasUnreadNotifications,
                  isNightMode = isNightMode,
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
                MainHeroSection(isNightMode = isNightMode)

                Spacer(modifier = Modifier.height(20.dp))

                // 3. FOCUS SESSION PANEL
                FocusSessionPanel(
                  selectedMinutes = state.selectedDurationMinutes,
                  isCustom = state.isCustomDuration,
                  customMinutes = state.customMinutes,
                  isNightMode = isNightMode,
                  onSelectPreset = { viewModel.selectPresetDuration(it) },
                  onSelectCustom = { viewModel.toggleCustomDuration() },
                  onCustomMinutesChange = { viewModel.updateCustomMinutes(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. ESCAPE CHALLENGE CARD
                EscapeChallengeCard(isNightMode = isNightMode)

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
                  isNightMode = isNightMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7. COMPACT DAILY GOAL SECTION
                HomeDailyGoalSection(
                  goal = state.dailyGoal,
                  isNightMode = isNightMode,
                  onOpenSetupDialog = {
                    showDailyGoalDialog = true
                    startWithEditName = false
                  },
                  onEditName = {
                    showDailyGoalDialog = true
                    startWithEditName = true
                  }
                )

                Spacer(modifier = Modifier.height(16.dp))
              }
            }
          }
        }
      }

      // INTEGRATED FLOATING BOTTOM NAVIGATION BAR
      FloatingBottomNavigation(
        activeTab = state.activeTab,
        isNightMode = state.isNightMode,
        onTabSelected = { viewModel.setActiveTab(it) },
        modifier = Modifier.align(Alignment.BottomCenter)
      )

      // Daily Goal Setup / Overview Dialog
      if (showDailyGoalDialog) {
        DailyGoalDialog(
          goal = state.dailyGoal,
          isNightMode = state.isNightMode,
          startWithEditName = startWithEditName,
          onDismiss = {
            showDailyGoalDialog = false
            startWithEditName = false
          },
          onCreateGoalWithDetails = { title, totalDays, label ->
            viewModel.createDailyGoalWithDetails(title, totalDays, label)
            showDailyGoalDialog = false
            startWithEditName = false
            Toast.makeText(context, "Your goal has been set", Toast.LENGTH_SHORT).show()
          },
          onUpdateGoalTitle = { newTitle ->
            viewModel.updateDailyGoalTitle(newTitle)
          },
          onUpdateGoalDuration = { totalDays, label ->
            viewModel.updateDailyGoalDuration(totalDays, label)
          },
          onResetGoal = {
            viewModel.resetDailyGoal()
          }
        )
      }

      // Session Started Dialog/Overlay
      AnimatedVisibility(
        visible = state.isSessionStartedDialog,
        enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200), initialScale = 0.95f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150), targetScale = 0.95f)
      ) {
        SessionStartedOverlay(
          durationMinutes = state.effectiveDurationMinutes,
          isNightMode = state.isNightMode,
          onDismiss = { viewModel.dismissSessionStartedDialog() }
        )
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
          onFocusRemindersToggle = { viewModel.setFocusReminders(it) },
          onSessionCompletedToggle = { viewModel.setSessionCompletedReminder(it) }
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
          }
        )
      }

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
    }
  }
}



/**
 * Top Header: Avatar + Greeting on left, Notification Bell on right.
 */
@Composable
private fun TopHeader(
  userName: String,
  isLoggedIn: Boolean = true,
  hasUnreadNotifications: Boolean = false,
  isNightMode: Boolean,
  onNotificationClick: () -> Unit
) {
  val displayName = if (userName.isNotBlank()) userName else if (isLoggedIn) "User" else "Guest"
  val avatarLetter = if (userName.isNotBlank()) {
    userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
  } else ""

  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // User Avatar Circle displaying first letter of user's saved name
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(SignatureNeonLime),
        contentAlignment = Alignment.Center
      ) {
        if (avatarLetter.isNotEmpty()) {
          Text(
            text = avatarLetter,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            ),
            color = DarkTextOnLime
          )
        } else {
          Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(20.dp),
            tint = DarkTextOnLime
          )
        }
      }

      Column {
        Text(
          text = if (isLoggedIn) "Welcome back" else "Welcome",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
          color = mutedColor
        )
        Text(
          text = displayName,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
          ),
          color = textColor
        )
      }
    }

    // Notification Bell Button
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(surfaceColor)
        .border(1.dp, borderColor, CircleShape)
        .clickable(onClick = onNotificationClick)
        .testTag("notification_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.Notifications,
        contentDescription = "Notifications",
        tint = textColor,
        modifier = Modifier.size(20.dp)
      )
      if (hasUnreadNotifications) {
        // Tiny notification dot
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)
            .size(5.dp)
            .clip(CircleShape)
            .background(SignatureNeonLime)
        )
      }
    }
  }
}

/**
 * Main Hero Section: Large clean title & supporting sentence.
 */
@Composable
private fun MainHeroSection(isNightMode: Boolean) {
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = "Ready to lock in?",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      ),
      color = textColor
    )
    Text(
      text = "Choose a session. Block the noise. Get it done.",
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
      color = mutedColor
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
  onCustomMinutesChange: (Int) -> Unit
) {
  var showCustomDialog by remember { mutableStateOf(false) }

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

        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(SignatureNeonLime)
        )
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
              onClick = { showCustomDialog = true },
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
              thumbColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
              activeTrackColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
              activeTickColor = if (isNightMode) DarkButtonCharcoal else DarkButtonCharcoal,
              inactiveTrackColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder,
              inactiveTickColor = if (isNightMode) SignatureNeonLime.copy(alpha = 0.5f) else DarkButtonCharcoal.copy(alpha = 0.35f)
            )
          )
        }
      }
    }
  }

  if (showCustomDialog) {
    CustomDurationInputDialog(
      initialMinutes = customMinutes,
      isNightMode = isNightMode,
      onConfirm = { minutes ->
        onCustomMinutesChange(minutes)
        showCustomDialog = false
      },
      onDismiss = { showCustomDialog = false }
    )
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
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val inputBg = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  val parsedInt = textValue.toIntOrNull()
  val isTooHigh = parsedInt != null && parsedInt > 300
  val isTooLow = parsedInt != null && parsedInt < 1
  val isInvalid = textValue.isBlank() || isTooHigh || isTooLow

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Enter Custom Duration",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = textColor
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Set your focus time in minutes (1 to 300 min):",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
          color = mutedColor
        )
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
            focusedBorderColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else if (isNightMode) SignatureNeonLime else Color.Black,
            unfocusedBorderColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else borderColor,
            errorBorderColor = Color(0xFFD93838),
            errorLabelColor = Color(0xFFD93838),
            focusedLabelColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else if (isNightMode) SignatureNeonLime else Color.Black,
            unfocusedLabelColor = if (isTooHigh || isTooLow) Color(0xFFD93838) else mutedColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = if (isNightMode) SignatureNeonLime else Color.Black
          ),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        if (isTooHigh) {
          Text(
            text = "Duration exceeds 300 min. Please reduce the number.",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
            color = Color(0xFFD93838)
          )
        } else if (isTooLow && textValue.isNotEmpty()) {
          Text(
            text = "Please enter a duration between 1 and 300 min.",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
            color = Color(0xFFD93838)
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        enabled = !isInvalid,
        onClick = {
          if (parsedInt != null && parsedInt in 1..300) {
            onConfirm(parsedInt)
          }
        }
      ) {
        Text(
          text = "Save",
          fontWeight = FontWeight.Bold,
          color = if (isInvalid) mutedColor.copy(alpha = 0.5f) else if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = mutedColor)
      }
    },
    containerColor = cardBg,
    shape = RoundedCornerShape(18.dp)
  )
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
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.5f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
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
            tint = SignatureNeonLime,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Session Configured",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Ready to stay locked in for $durationMinutes minutes.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
          color = mutedColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNightMode) Color.White else DarkButtonCharcoal,
            contentColor = if (isNightMode) Color.Black else Color.White
          )
        ) {
          Text(
            text = "Back to Home",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
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
      if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
    } else {
      if (isNightMode) DarkContainerNeutral else LightContainerNeutral
    },
    animationSpec = tween(durationMillis = 200)
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) DarkTextOnLime else Color.White
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
      .clickable(onClick = onClick)
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
 * Escape Challenge Card: Secondary card detailing early exit pushup commitment.
 */
@Composable
private fun EscapeChallengeCard(isNightMode: Boolean) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = "Early exit",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
          ),
          color = textColor
        )
      }

      Text(
        text = "Complete 15 push-ups to end the session early.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
        color = mutedColor
      )
    }
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
  val btnBg = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
  val contentColor = if (isNightMode) DarkTextOnLime else Color.White

  Button(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .testTag("start_focus_button"),
    shape = RoundedCornerShape(14.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = btnBg,
      contentColor = contentColor
    ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
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
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Surface(
    modifier = Modifier.fillMaxWidth(),
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

      // Goal metric as clean, normal text
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = todayFocusFormatted,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp
          ),
          color = textColor
        )
        Text(
          text = "goal",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          ),
          color = mutedColor
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
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = cardBg,
    titleContentColor = textColor,
    textContentColor = textColor,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Lock,
          contentDescription = null,
          tint = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "App Blocker Permission",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "To block distracting apps during focus sessions, Lock In needs permissions to monitor foreground apps.",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
          color = mutedColor
        )
        if (!hasUsagePermission) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = containerColor,
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
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                  contentColor = if (isNightMode) DarkTextOnLime else Color.White
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
            shape = RoundedCornerShape(12.dp),
            color = containerColor,
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
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                  contentColor = if (isNightMode) DarkTextOnLime else Color.White
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
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = mutedColor)
      }
    }
  )
}

/**
 * Compact Daily Goal Section on the Home Page
 */
@Composable
private fun HomeDailyGoalSection(
  goal: DailyGoal?,
  isNightMode: Boolean,
  onOpenSetupDialog: () -> Unit,
  onEditName: () -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .testTag("home_daily_goal_card"),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    if (goal == null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenSetupDialog() }
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Set Daily Goal",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.5.sp
            ),
            color = textColor
          )
          Text(
            text = "Choose a goal and stay consistent.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = mutedColor
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
        ) {
          Text(
            text = "+ Set Goal",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            ),
            color = if (isNightMode) DarkButtonCharcoal else Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    } else {
      val currentDay = goal.getCurrentDay()
      val totalDays = goal.totalDays
      val remainingDays = goal.getRemainingDays()

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.clickable { onEditName() }
          ) {
            Text(
              text = goal.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp
              ),
              color = textColor
            )
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = "Edit Name",
              tint = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
              modifier = Modifier.size(14.dp)
            )
          }
          Text(
            text = "Day $currentDay of $totalDays • $remainingDays days left",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = mutedColor
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onOpenSetupDialog() }
            .testTag("show_daily_goal_btn")
        ) {
          Text(
            text = "Show",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.5.sp
            ),
            color = if (isNightMode) DarkButtonCharcoal else Color.White,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
          )
        }
      }
    }
  }
}

/**
 * Dialog for setting or managing the Daily Goal directly on Home Page
 */
@Composable
private fun DailyGoalDialog(
  goal: DailyGoal?,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onCreateGoalWithDetails: (String, Int, String?) -> Unit,
  onUpdateGoalTitle: (String) -> Unit,
  onUpdateGoalDuration: (Int, String?) -> Unit,
  onResetGoal: () -> Unit,
  startWithEditName: Boolean = false
) {
  val context = LocalContext.current
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val optionBg = if (isNightMode) Color(0xFF1E2024) else LightContainerInner

  // Setup Flow state (when goal == null)
  var setupStep by remember { mutableIntStateOf(1) } // 1: Goal Name, 2: Duration Presets, 3: Custom Days Input
  var goalTitleInput by remember { mutableStateOf("") }
  var selectedDays by remember { mutableIntStateOf(30) }
  var selectedLabel by remember { mutableStateOf("1 Month") }
  var customDaysInput by remember { mutableStateOf("") }

  // Separate dialog states for editing an existing goal (when goal != null)
  var showEditTitleDialog by remember { mutableStateOf(startWithEditName) }
  var editedTitleInput by remember(goal?.title) { mutableStateOf(goal?.title ?: "") }

  // State for change goal confirmation popup
  var showChangeConfirmation by remember { mutableStateOf(false) }

  val presets = listOf(
    Triple("1 Week", 7, "7 days"),
    Triple("2 Weeks", 14, "14 days"),
    Triple("1 Month", 30, "30 days"),
    Triple("2 Months", 60, "60 days"),
    Triple("3 Months", 90, "90 days"),
    Triple("6 Months", 180, "180 days"),
    Triple("1 Year", 365, "365 days")
  )

  // -------------------------------------------------------------
  // CHANGE GOAL CONFIRMATION POPUP
  // -------------------------------------------------------------
  if (showChangeConfirmation) {
    AlertDialog(
      onDismissRequest = { showChangeConfirmation = false },
      containerColor = cardBg,
      titleContentColor = textColor,
      textContentColor = textColor,
      shape = RoundedCornerShape(20.dp),
      title = {
        Text(
          text = "Remove goal?",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      },
      text = {
        Text(
          text = "Are you sure you want to remove your current goal? This cannot be undone.",
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
          color = mutedColor
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showChangeConfirmation = false
            onResetGoal()
            setupStep = 1
            goalTitleInput = ""
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE57373),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Remove", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showChangeConfirmation = false
            Toast.makeText(context, "Goal kept unchanged. Undo", Toast.LENGTH_SHORT).show()
          }
        ) {
          Text("No", color = textColor, fontWeight = FontWeight.SemiBold)
        }
      }
    )
  }

  // -------------------------------------------------------------
  // SEPARATE EDIT GOAL NAME POPUP
  // -------------------------------------------------------------
  if (showEditTitleDialog && goal != null) {
    AlertDialog(
      onDismissRequest = { 
        if (startWithEditName) onDismiss() else showEditTitleDialog = false 
      },
      containerColor = cardBg,
      titleContentColor = textColor,
      textContentColor = textColor,
      shape = RoundedCornerShape(20.dp),
      title = {
        Text(
          text = "Edit Goal Name",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Enter a new goal name below:",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = mutedColor
          )
          OutlinedTextField(
            value = editedTitleInput,
            onValueChange = { editedTitleInput = it },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_goal_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
              unfocusedBorderColor = borderColor,
              focusedContainerColor = optionBg,
              unfocusedContainerColor = optionBg,
              focusedTextColor = textColor,
              unfocusedTextColor = textColor
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (editedTitleInput.isNotBlank()) {
              onUpdateGoalTitle(editedTitleInput.trim())
              showEditTitleDialog = false
              Toast.makeText(context, "Goal name updated", Toast.LENGTH_SHORT).show()
            }
          },
          enabled = editedTitleInput.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
            contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Save", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { 
          if (startWithEditName) onDismiss() else showEditTitleDialog = false 
        }) {
          Text("Cancel", color = mutedColor)
        }
      }
    )
  }

  // -------------------------------------------------------------
  // MAIN DIALOG (SETUP POPUP 1 OR SETUP POPUP 2 OR PROGRESS POPUP)
  // -------------------------------------------------------------
  if (!startWithEditName || !showEditTitleDialog) {
    AlertDialog(
      onDismissRequest = onDismiss,
    containerColor = cardBg,
    titleContentColor = textColor,
    textContentColor = textColor,
    shape = RoundedCornerShape(20.dp),
    title = {
      if (goal == null) {
        val titleText = when (setupStep) {
          1 -> "What is your goal?"
          2 -> "How long do you want to Lock In?"
          else -> "Enter custom days"
        }
        Text(
          text = titleText,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      } else {
        Text(
          text = goal.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          modifier = Modifier.fillMaxWidth(),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        if (goal == null) {
          if (setupStep == 1) {
            // =========================================================
            // SETUP POPUP 1 — Goal Name ONLY
            // =========================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Write your goal below to get started.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = mutedColor
              )

              OutlinedTextField(
                value = goalTitleInput,
                onValueChange = { goalTitleInput = it },
                placeholder = {
                  Text(
                    "Goal name",
                    color = mutedColor.copy(alpha = 0.7f),
                    fontSize = 13.5.sp
                  )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("goal_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                  unfocusedBorderColor = borderColor,
                  focusedContainerColor = optionBg,
                  unfocusedContainerColor = optionBg,
                  focusedTextColor = textColor,
                  unfocusedTextColor = textColor
                )
              )
            }
          } else if (setupStep == 2) {
            // =========================================================
            // SETUP POPUP 2 — Duration Presets ONLY
            // =========================================================
            Column(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Goal: \"${goalTitleInput.ifBlank { "Daily Goal" }}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp
                ),
                color = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                modifier = Modifier.padding(bottom = 4.dp)
              )

              presets.forEach { (label, days, subtext) ->
                val isSelected = selectedDays == days
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      selectedDays = days
                      selectedLabel = label
                    },
                  color = if (isSelected) (if (isNightMode) SignatureNeonLime.copy(alpha = 0.15f) else DarkButtonCharcoal.copy(alpha = 0.1f)) else optionBg,
                  border = BorderStroke(
                    1.dp,
                    if (isSelected) (if (isNightMode) SignatureNeonLime else DarkButtonCharcoal) else Color.Transparent
                  ),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(56.dp)
                      .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 15.sp
                        ),
                        color = textColor
                      )
                      Text(
                        text = subtext,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = mutedColor
                      )
                    }
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    setupStep = 3
                  },
                color = optionBg,
                border = BorderStroke(1.dp, Color.Transparent),
                shape = RoundedCornerShape(12.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Custom Days",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    ),
                    color = textColor
                  )
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = mutedColor,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          } else {
            // =========================================================
            // SETUP POPUP 3 — Enter custom days
            // =========================================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Enter the number of days for your goal (7 - 365).",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = mutedColor
              )

              OutlinedTextField(
                value = customDaysInput,
                onValueChange = { input ->
                  if (input.all { it.isDigit() } && input.length <= 4) {
                    customDaysInput = input
                  }
                },
                placeholder = { Text("e.g., 60", fontSize = 13.sp, color = mutedColor.copy(alpha = 0.7f)) },
                trailingIcon = {
                  Text(
                    text = "days",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = mutedColor,
                    modifier = Modifier.padding(end = 12.dp)
                  )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                  unfocusedBorderColor = borderColor,
                  focusedContainerColor = optionBg,
                  unfocusedContainerColor = optionBg,
                  focusedTextColor = textColor,
                  unfocusedTextColor = textColor
                )
              )

              val customDaysVal = customDaysInput.toIntOrNull()
              if (customDaysVal != null) {
                if (customDaysVal < 7) {
                  Text(
                    text = "Minimum duration is 7 days",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.error
                  )
                } else if (customDaysVal > 365) {
                  Text(
                    text = "Maximum duration is 365 days",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        } else {
          // =========================================================
          // PROGRESS POPUP (When user taps "Show" on Home page)
          // =========================================================
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val currentDay = goal.getCurrentDay()
            val totalDays = goal.totalDays
            val remainingDays = goal.getRemainingDays()
            val isCompleted = goal.isCompleted()

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isCompleted) "Day $totalDays of $totalDays" else "Day $currentDay of $totalDays",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                ),
                color = textColor
              )
              Text(
                text = if (isCompleted) "Goal Complete" else "$remainingDays days left",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = if (isCompleted) SignatureNeonLime else mutedColor
                )
              )
            }

            DailyGoalDotGrid(
              totalDays = totalDays,
              currentDay = currentDay,
              isCompleted = isCompleted,
              isNightMode = isNightMode,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
            )
          }
        }
      }
    },
    confirmButton = {
      if (goal == null) {
        when (setupStep) {
          1 -> {
            Button(
              onClick = { setupStep = 2 },
              enabled = goalTitleInput.isNotBlank(),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Continue", fontWeight = FontWeight.Bold)
            }
          }
          2 -> {
            Button(
              onClick = {
                onCreateGoalWithDetails(goalTitleInput.trim(), selectedDays, selectedLabel)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Lock In Goal", fontWeight = FontWeight.Bold)
            }
          }
          else -> {
            val days = customDaysInput.toIntOrNull() ?: 0
            val isValid = days in 7..365
            Button(
              onClick = {
                onCreateGoalWithDetails(goalTitleInput.trim(), days, "$days Days")
              },
              enabled = isValid,
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Confirm", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          // Start/End Dates moved here to reduce gap with buttons
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Start: ${goal.startDateStr}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = mutedColor
            )
            Text(
              text = "End: ${goal.endDateStr}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = mutedColor
            )
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 0.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
          // Home screen widget button
          TextButton(
            onClick = {
              val pinned = DailyGoalWidgetProvider.requestPinAppWidget(context)
              if (pinned) {
                Toast.makeText(context, "Adding widget to Home screen...", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "To add widget: Long-press your home screen, tap Widgets, and choose Lock In™ Daily Goal.", Toast.LENGTH_LONG).show()
              }
            }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.AddBox,
                contentDescription = null,
                tint = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Home screen",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                color = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
              )
            }
          }

          TextButton(onClick = onDismiss) {
            Text(
              text = "Close",
              color = mutedColor,
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
            )
          }

          TextButton(
            onClick = { showChangeConfirmation = true },
            modifier = Modifier.testTag("remove_goal_btn")
          ) {
            Text(
              text = "Remove",
              color = if (isNightMode) Color(0xFFE57373) else Color(0xFFD32F2F),
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp)
            )
          }
          }
        }
      }
    },
    dismissButton = {
      if (goal == null) {
        when (setupStep) {
          2 -> {
            TextButton(onClick = { setupStep = 1 }) {
              Text("Back", color = mutedColor)
            }
          }
          3 -> {
            TextButton(onClick = { setupStep = 2 }) {
              Text("Back", color = mutedColor)
            }
          }
          else -> {
            TextButton(onClick = onDismiss) {
              Text("Cancel", color = mutedColor)
            }
          }
        }
      } else {
        // Handled in confirmButton for better full-width control
        null
      }
    }
    )
  }
}
