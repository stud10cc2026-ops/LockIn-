package com.example.ui.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextOffWhite
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.LightContainerNeutral
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureNeonLime

/**
 * Settings Screen:
 * Redesigned to match the minimal, clean, reference style with grouped rounded cards,
 * clear typography hierarchy, and a prominent bottom Log Out button.
 */
@Composable
fun SettingsContent(
  userName: String = "Your name",
  isLoggedIn: Boolean = false,
  userEmail: String = "",
  isNightMode: Boolean,
  defaultDurationMinutes: Int,
  allowPause: Boolean,
  focusReminders: Boolean,
  notificationsEnabled: Boolean = true,
  onUpdateUserName: (String) -> Unit = {},
  onNightModeToggle: (Boolean) -> Unit,
  onDefaultDurationSelect: (Int) -> Unit,
  onAllowPauseToggle: (Boolean) -> Unit,
  onFocusRemindersToggle: (Boolean) -> Unit,
  onNotificationsToggle: (Boolean) -> Unit = {},
  onOpenNotificationsPage: () -> Unit = {},
  onOpenAccountPage: () -> Unit = {},
  onOpenAuthScreen: (String) -> Unit = {},
  onLogout: () -> Unit = {},
  onResetAppData: () -> Unit,
  onTestWelcomeFlow: () -> Unit = {},
  onGenerateDemoData: () -> Unit = {},
  onClearDemoData: () -> Unit = {},
  onNavigateBack: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showResetConfirmation by remember { mutableStateOf(false) }
  var showLogoutConfirmation by remember { mutableStateOf(false) }
  var showEditNameDialog by remember { mutableStateOf(false) }
  var infoDialogTitle by remember { mutableStateOf<String?>(null) }
  var infoDialogContent by remember { mutableStateOf<String?>(null) }

  val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

  // Strict thematic colors matching the reference style
  val pageBg = if (isNightMode) DarkBackground else LightPageBackground
  val groupBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val groupBorder = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val dividerColor = if (isNightMode) Color(0xFF252731) else Color(0xFFF0F0EE)
  val primaryText = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val secondaryText = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val iconTint = if (isNightMode) Color(0xFFDCDDE2) else Color(0xFF222328)
  val chevronTint = if (isNightMode) Color(0xFF676974) else Color(0xFFA5A6A3)

  val displayName = if (userName.isNotBlank()) userName else if (isLoggedIn) "Lock In User" else "Guest"
  val avatarLetter = if (userName.isNotBlank()) userName.trim().firstOrNull()?.uppercase() ?: "" else ""

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(pageBg)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 96.dp)
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. TOP HEADER
      Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 30.sp,
          lineHeight = 36.sp,
          letterSpacing = (-0.5).sp
        ),
        color = primaryText,
        modifier = Modifier.testTag("settings_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Customize your Lock In experience.",
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = secondaryText
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 2. PROMINENT PROFILE / ACCOUNT CARD
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .clickable { showEditNameDialog = true }
          .testTag("profile_card"),
        shape = RoundedCornerShape(20.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SignatureNeonLime),
              contentAlignment = Alignment.Center
            ) {
              if (avatarLetter.isNotEmpty()) {
                Text(
                  text = avatarLetter,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                  ),
                  color = DarkButtonCharcoal
                )
              } else {
                Icon(
                  imageVector = Icons.Outlined.Person,
                  contentDescription = "Profile",
                  modifier = Modifier.size(24.dp),
                  tint = DarkButtonCharcoal
                )
              }
            }

            Column {
              Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 16.sp
                ),
                color = primaryText
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (isLoggedIn && userEmail.isNotBlank()) "@${userEmail.substringBefore("@")}" else "Tap to edit name",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 13.sp
                ),
                color = secondaryText
              )
            }
          }

          Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Edit name",
            tint = chevronTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. GROUP 1: ACCOUNT-RELATED SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          SettingsRowItem(
            icon = Icons.Outlined.Person,
            title = "Account",
            subtitle = if (isLoggedIn) "Signed in as $userName" else "Sign in or create account",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "open_account_settings_row",
            onClick = onOpenAccountPage
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.8.dp,
            color = dividerColor
          )

          SettingsRowItem(
            icon = Icons.Outlined.Edit,
            title = "Edit Name",
            subtitle = displayName,
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "settings_edit_name_row",
            onClick = { showEditNameDialog = true }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. GROUP 2: APPEARANCE / NOTIFICATION SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Dark Mode Row
          SettingsRowItem(
            icon = if (isNightMode) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
            title = "Dark mode",
            subtitle = if (isNightMode) "Dark theme active" else "Light theme active",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "appearance_toggle_row",
            onClick = { onNightModeToggle(!isNightMode) },
            trailingContent = {
              Switch(
                checked = isNightMode,
                onCheckedChange = { onNightModeToggle(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = SignatureNeonLime,
                  uncheckedThumbColor = Color.White,
                  uncheckedTrackColor = if (isNightMode) Color(0xFF333540) else Color(0xFFDFE0DF)
                ),
                modifier = Modifier.testTag("appearance_mode_switch")
              )
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.8.dp,
            color = dividerColor
          )

          // Notifications Center Row
          SettingsRowItem(
            icon = Icons.Outlined.NotificationsNone,
            title = "Notification preferences",
            subtitle = "Alert sound & history",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "open_notifications_settings",
            onClick = onOpenNotificationsPage
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. GROUP 3: GENERAL SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Reset App
          SettingsRowItem(
            icon = Icons.Outlined.Refresh,
            title = "Reset App",
            subtitle = "Reset local history and preferences",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "reset_app_data_button",
            onClick = { showResetConfirmation = true }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.8.dp,
            color = dividerColor
          )

          // Test Welcome Flow
          SettingsRowItem(
            icon = Icons.Outlined.Build,
            title = "Preview Onboarding",
            subtitle = "Test first-launch flow & permissions",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "test_welcome_flow_button",
            onClick = onTestWelcomeFlow
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 6. GROUP 4: INFORMATION / SUPPORT SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          SettingsRowItem(
            icon = Icons.Outlined.HelpOutline,
            title = "FAQ",
            subtitle = "Frequently asked questions",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "settings_faq_row",
            onClick = {
              infoDialogTitle = "FAQ"
              infoDialogContent = "• How does Lock In work?\nLock In helps you stay undistracted by locking chosen apps during active focus sessions.\n\n• What is the Push-Up Challenge?\nTo escape an active lock early, you must complete genuine full-body push-ups verified by camera pose analysis.\n\n• Is my camera data private?\nYes! All pose detection and rep counting runs 100% locally on your device in real-time."
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.8.dp,
            color = dividerColor
          )

          SettingsRowItem(
            icon = Icons.Outlined.Description,
            title = "Terms of service",
            subtitle = "Privacy & terms of usage",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "settings_terms_row",
            onClick = {
              infoDialogTitle = "Terms of Service"
              infoDialogContent = "Lock In is designed to assist you in building disciplined digital habits.\n\nBy using the app, you agree that focus sessions and challenge unlocks are self-initiated. Lock In does not access, sell, or transmit any sensitive personal data."
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.8.dp,
            color = dividerColor
          )

          SettingsRowItem(
            icon = Icons.Outlined.Security,
            title = "User policy",
            subtitle = "Permissions and privacy policy",
            iconTint = iconTint,
            primaryText = primaryText,
            secondaryText = secondaryText,
            chevronTint = chevronTint,
            testTag = "settings_policy_row",
            onClick = {
              infoDialogTitle = "User Policy"
              infoDialogContent = "Lock In requires Usage Access and Overlay permissions solely to detect and block selected distracting apps during active sessions.\n\nCamera permission is used only during the escape push-up challenge to verify push-up movement. No video feeds or images are ever stored or uploaded."
            }
          )
        }
      }

      if (isLoggedIn) {
        Spacer(modifier = Modifier.height(24.dp))

        // 7. SEPARATE PROMINENT SIGN OUT BUTTON AT BOTTOM (Matching Reference Style)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickable { showLogoutConfirmation = true }
            .testTag("settings_logout_row"),
          shape = RoundedCornerShape(26.dp),
          color = Color.White,
          border = if (isNightMode) null else BorderStroke(1.dp, groupBorder)
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.Logout,
              contentDescription = "Sign Out",
              tint = Color(0xFFD93838),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sign Out",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = Color(0xFFD93838)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // App version footer
      Text(
        text = "Lock In v1.0.0",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = secondaryText.copy(alpha = 0.6f),
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }

    // Reset Confirmation Dialog
    AnimatedVisibility(
      visible = showResetConfirmation,
      enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
    ) {
      ResetConfirmationDialog(
        isNightMode = isNightMode,
        onDismiss = { showResetConfirmation = false },
        onConfirmReset = {
          showResetConfirmation = false
          onResetAppData()
        }
      )
    }

    // Logout Confirmation Dialog
    AnimatedVisibility(
      visible = showLogoutConfirmation,
      enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
    ) {
      LogoutConfirmationDialog(
        isNightMode = isNightMode,
        onDismiss = { showLogoutConfirmation = false },
        onConfirmLogout = {
          showLogoutConfirmation = false
          onLogout()
        }
      )
    }

    // Edit Name Dialog
    AnimatedVisibility(
      visible = showEditNameDialog,
      enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
    ) {
      EditNameDialog(
        currentName = userName,
        isNightMode = isNightMode,
        onDismiss = { showEditNameDialog = false },
        onSave = { newName ->
          showEditNameDialog = false
          onUpdateUserName(newName)
        }
      )
    }

    // Information Dialog (FAQ / Terms / Policy)
    AnimatedVisibility(
      visible = infoDialogTitle != null,
      enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f, animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
    ) {
      if (infoDialogTitle != null && infoDialogContent != null) {
        InfoDialog(
          title = infoDialogTitle!!,
          content = infoDialogContent!!,
          isNightMode = isNightMode,
          onDismiss = {
            infoDialogTitle = null
            infoDialogContent = null
          }
        )
      }
    }
  }
}

@Composable
private fun SettingsRowItem(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  iconTint: Color,
  primaryText: Color,
  secondaryText: Color,
  chevronTint: Color,
  testTag: String? = null,
  onClick: (() -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null
) {
  val clickModifier = if (onClick != null) {
    Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = null,
      onClick = onClick
    )
  } else Modifier

  val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(clickModifier)
      .then(tagModifier)
      .padding(horizontal = 16.dp, vertical = 15.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp)
      )

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
          ),
          color = primaryText
        )
        if (!subtitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(1.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.5.sp
            ),
            color = secondaryText
          )
        }
      }
    }

    if (trailingContent != null) {
      trailingContent()
    } else if (onClick != null) {
      Icon(
        imageVector = Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = chevronTint,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}



@Composable
private fun InfoDialog(
  title: String,
  content: String,
  isNightMode: Boolean,
  onDismiss: () -> Unit
) {
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val secondaryText = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(20.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(22.dp)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = content,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp
          ),
          color = secondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
            contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
          )
        ) {
          Text("Got it", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
        }
      }
    }
  }
}

@Composable
private fun ResetConfirmationDialog(
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onConfirmReset: () -> Unit
) {
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(20.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF0F0)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = Color(0xFFD93838),
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Reset App Data?",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          color = if (isNightMode) Color.White else LightTextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "This will permanently delete your focus activity, history, and preferences.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp
          ),
          color = if (isNightMode) DarkTextSecondary else LightTextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral,
              contentColor = if (isNightMode) Color.White else LightTextPrimary
            )
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          Button(
            onClick = onConfirmReset,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFD93838),
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Reset",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EditNameDialog(
  currentName: String,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var nameInput by remember { mutableStateOf(currentName) }
  val isValid = nameInput.trim().isNotEmpty()
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(onClick = onDismiss),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(20.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(22.dp)
      ) {
        Text(
          text = "Edit Name",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          color = if (isNightMode) Color.White else LightTextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it.take(30) },
          label = { Text("Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_name_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral,
            unfocusedContainerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral,
            focusedBorderColor = if (isNightMode) SignatureNeonLime else Color.Black,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = if (isNightMode) SignatureNeonLime else Color.Black,
            unfocusedLabelColor = if (isNightMode) DarkTextSecondary else LightTextSecondary,
            focusedTextColor = if (isNightMode) Color.White else Color.Black,
            unfocusedTextColor = if (isNightMode) Color.White else Color.Black,
            cursorColor = if (isNightMode) SignatureNeonLime else Color.Black,
            selectionColors = TextSelectionColors(
              handleColor = if (isNightMode) SignatureNeonLime else Color.Black,
              backgroundColor = (if (isNightMode) SignatureNeonLime else Color.Black).copy(alpha = 0.2f)
            )
          )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral,
              contentColor = if (isNightMode) Color.White else LightTextPrimary
            )
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          Button(
            onClick = {
              val trimmed = nameInput.trim()
              if (trimmed.isNotEmpty()) {
                onSave(trimmed)
              }
            },
            enabled = isValid,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
              contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
            )
          ) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}

@Composable
fun LogoutConfirmationDialog(
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onConfirmLogout: () -> Unit
) {
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.6f))
      .clickable { onDismiss() },
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 28.dp)
        .fillMaxWidth()
        .clickable(enabled = false) {}
        .testTag("logout_confirmation_dialog"),
      shape = RoundedCornerShape(20.dp),
      color = cardBg,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF0F0)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            tint = Color(0xFFD93838),
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Sign out?",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          color = if (isNightMode) Color.White else LightTextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Your account data will remain saved on your device.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp
          ),
          color = if (isNightMode) DarkTextSecondary else LightTextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral,
              contentColor = if (isNightMode) Color.White else LightTextPrimary
            )
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          Button(
            onClick = onConfirmLogout,
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .testTag("confirm_logout_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFD93838),
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Sign Out",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}
