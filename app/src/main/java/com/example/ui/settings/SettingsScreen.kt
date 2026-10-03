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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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

/**
 * Settings Screen:
 * Restyled to match the Home page aesthetic: white 28px rounded cards with 1px #E3E9F0 borders,
 * clear typography hierarchy, ice blue-gray accents (#DCE5EE), and soft neutral tones.
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

  // Thematic colors matching Home design guidelines
  val groupBg = if (isNightMode) DarkCardSurface else Color.White
  val groupBorder = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val dividerColor = if (isNightMode) Color(0xFF252731) else Color(0xFFE8EDF3)
  val primaryText = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val secondaryText = if (isNightMode) DarkTextSecondary else Color(0xFF8E96A3)
  val iconTint = if (isNightMode) Color(0xFFDCDDE2) else Color(0xFF3A3A44)
  val chevronTint = if (isNightMode) Color(0xFF676974) else Color(0xFFA9B4C2)

  val displayName = if (userName.isNotBlank()) userName else if (isLoggedIn) "Lock In User" else "Guest"

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Transparent)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 130.dp) // Extra bottom padding for floating bottom nav
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. HEADLINE
      Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 32.sp,
          lineHeight = 35.2.sp,
          letterSpacing = (-0.02).em
        ),
        color = primaryText,
        modifier = Modifier.testTag("settings_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Customize your Lock In experience.",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        ),
        color = secondaryText
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 3. GROUP 1: ACCOUNT-RELATED SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder),
        shadowElevation = 0.dp
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
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
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
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder),
        shadowElevation = 0.dp
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
                  checkedTrackColor = Color(0xFF1A1A1F),
                  checkedBorderColor = Color.Transparent,
                  uncheckedThumbColor = Color.White,
                  uncheckedTrackColor = Color(0xFFDCE5EE),
                  uncheckedBorderColor = Color(0xFFC9D4E0)
                ),
                modifier = Modifier.testTag("appearance_mode_switch")
              )
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
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
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder),
        shadowElevation = 0.dp
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
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 6. GROUP 4: INFORMATION / SUPPORT SETTINGS
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = groupBg,
        border = BorderStroke(1.dp, groupBorder),
        shadowElevation = 0.dp
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
              infoDialogContent = "• How does Lock In work?\nLock In helps you stay undistracted by locking chosen apps during active focus sessions."
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
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
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
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
              infoDialogContent = "Lock In requires Usage Access and Overlay permissions solely to detect and block selected distracting apps during active sessions."
            }
          )
        }
      }

      if (isLoggedIn) {
        Spacer(modifier = Modifier.height(16.dp))

        // 7. SEPARATE PROMINENT SIGN OUT BUTTON AT BOTTOM
        val signOutBg = if (isNightMode) Color.White else Color(0xFF1A1A1F)
        val signOutContent = if (isNightMode) Color(0xFF1A1A1F) else Color.White

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .clickable { showLogoutConfirmation = true }
            .testTag("settings_logout_row"),
          shape = CircleShape,
          color = signOutBg,
          shadowElevation = 0.dp
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.Logout,
              contentDescription = "Sign Out",
              tint = signOutContent,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sign Out",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
              ),
              color = signOutContent
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // App version footer
      Text(
        text = "Lock In v1.0.0",
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp
        ),
        color = secondaryText.copy(alpha = 0.7f),
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
      .padding(horizontal = 20.dp, vertical = 18.dp),
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
        modifier = Modifier.size(22.dp)
      )

      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp
          ),
          color = primaryText
        )
        if (!subtitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Normal,
              fontSize = 15.sp
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
        modifier = Modifier.size(20.dp)
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
  val cardBg = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = Color(0xFF1A1A1F)
  val secondaryText = Color(0xFF8E96A3)

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
        modifier = Modifier.padding(28.dp)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = content,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.W400,
            lineHeight = 21.sp
          ),
          color = secondaryText
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
          Text("Got it", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp))
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
  LockInConfirmationDialog(
    icon = Icons.Outlined.WarningAmber,
    iconColor = if (isNightMode) Color(0xFFDCDDE2) else Color(0xFF3A3A44),
    iconBgColor = if (isNightMode) Color(0xFF252731) else Color(0xFFDCE5EE),
    title = "Reset App Data?",
    message = "This will permanently delete your focus activity, history, and preferences. This cannot be undone.",
    confirmButtonText = "Reset",
    confirmButtonColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
    confirmButtonTextColor = if (isNightMode) Color(0xFF1A1A1F) else Color.White,
    isNightMode = isNightMode,
    onDismiss = onDismiss,
    onConfirm = onConfirmReset
  )
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
  val cardBg = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)

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
        modifier = Modifier.padding(28.dp)
      ) {
        Text(
          text = "Edit Name",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = Color(0xFF1A1A1F)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Update your profile name below.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.W400,
            fontSize = 15.sp
          ),
          color = Color(0xFF8E96A3)
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it.take(30) },
          label = { Text("Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_name_input"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEEF1F5),
            unfocusedContainerColor = Color(0xFFEEF1F5),
            focusedBorderColor = Color(0xFF1A1A1F),
            unfocusedBorderColor = borderColor,
            focusedLabelColor = Color(0xFF1A1A1F),
            unfocusedLabelColor = Color(0xFF8E96A3),
            focusedTextColor = Color(0xFF1A1A1F),
            unfocusedTextColor = Color(0xFF1A1A1F),
            cursorColor = Color(0xFF1A1A1F),
            selectionColors = TextSelectionColors(
              handleColor = Color(0xFF1A1A1F),
              backgroundColor = Color(0xFF1A1A1F).copy(alpha = 0.2f)
            )
          )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
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
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp)
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
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF1A1A1F),
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp)
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
  LockInConfirmationDialog(
    icon = Icons.AutoMirrored.Outlined.Logout,
    iconColor = if (isNightMode) Color(0xFFDCDDE2) else Color(0xFF3A3A44),
    iconBgColor = if (isNightMode) Color(0xFF252731) else Color(0xFFDCE5EE),
    title = "Sign Out?",
    message = "Are you sure you want to sign out? Your account data will remain saved on your device.",
    confirmButtonText = "Sign Out",
    confirmButtonColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
    confirmButtonTextColor = if (isNightMode) Color(0xFF1A1A1F) else Color.White,
    isNightMode = isNightMode,
    onDismiss = onDismiss,
    onConfirm = onConfirmLogout
  )
}

@Composable
private fun LockInConfirmationDialog(
  icon: ImageVector,
  iconColor: Color,
  iconBgColor: Color,
  title: String,
  message: String,
  confirmButtonText: String,
  confirmButtonColor: Color,
  confirmButtonTextColor: Color,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val cardBg = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = Color(0xFF1A1A1F)
  val secondaryText = Color(0xFF8E96A3)

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
      color = Color.White,
      shadowElevation = 0.dp
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon Circle
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(iconBgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Message
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 15.sp,
            lineHeight = 21.sp
          ),
          color = secondaryText,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
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
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.W600,
                fontSize = 16.sp
              )
            )
          }

          Button(
            onClick = onConfirm,
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = confirmButtonColor,
              contentColor = confirmButtonTextColor
            )
          ) {
            Text(
              text = confirmButtonText,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.W600,
                fontSize = 16.sp
              )
            )
          }
        }
      }
    }
  }
}
