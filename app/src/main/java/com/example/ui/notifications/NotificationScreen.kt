package com.example.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.RefPageBackgroundBottom
import com.example.ui.theme.RefPageBackgroundTop
import com.example.ui.theme.SignatureNeonLime

private val ThinBackArrowOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinBackArrowOutline",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
  ).path(
    stroke = SolidColor(Color(0xFF3A3A44)),
    strokeLineWidth = 1.5f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round
  ) {
    moveTo(19f, 12f)
    lineTo(5f, 12f)
    moveTo(12f, 19f)
    lineTo(5f, 12f)
    lineTo(12f, 5f)
  }.build()
}

@Composable
fun NotificationScreen(
  isNightMode: Boolean = false,
  notificationsEnabled: Boolean = true,
  appNotifications: List<AppNotificationItem> = emptyList(),
  pendingDeletedNotification: PendingDeletedNotification? = null,
  isSystemPermissionGranted: Boolean = true,
  onBackClick: () -> Unit,
  onNotificationsEnabledToggle: (Boolean) -> Unit,
  onDeleteNotification: (AppNotificationItem) -> Unit,
  onUndoDeleteNotification: () -> Unit,
  onOpenSystemSettings: () -> Unit,
  focusReminders: Boolean = true,
  sessionCompletedReminder: Boolean = true,
  habitRemindersEnabled: Boolean = true,
  onFocusRemindersToggle: (Boolean) -> Unit = {},
  onSessionCompletedToggle: (Boolean) -> Unit = {},
  onHabitRemindersToggle: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val backgroundBrush = if (isNightMode) {
    Brush.verticalGradient(listOf(DarkBackground, DarkBackground))
  } else {
    Brush.verticalGradient(
      colors = listOf(RefPageBackgroundTop, RefPageBackgroundBottom),
      startY = 0f,
      endY = Float.POSITIVE_INFINITY
    )
  }

  val cardColor = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) DarkTextPrimary else Color(0xFF1A1A1F)
  val mutedTextColor = if (isNightMode) DarkTextSecondary else Color(0xFF8E96A3)
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val containerColor = if (isNightMode) DarkContainerNeutral else Color(0xFFF1F5F9)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundBrush)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 130.dp)
        .testTag("notification_screen")
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // BACK BUTTON
      val backInteractionSource = remember { MutableInteractionSource() }
      val isBackPressed by backInteractionSource.collectIsPressedAsState()
      val backScale by animateFloatAsState(
        targetValue = if (isBackPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "back_button_scale"
      )

      Box(
        modifier = Modifier
          .graphicsLayer {
            scaleX = backScale
            scaleY = backScale
          }
          .size(44.dp)
          .clip(CircleShape)
          .background(cardColor)
          .border(1.dp, borderColor, CircleShape)
          .clickable(
            interactionSource = backInteractionSource,
            indication = null
          ) { onBackClick() }
          .testTag("notification_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = ThinBackArrowOutlineIcon,
          contentDescription = "Back",
          tint = if (isNightMode) Color.White else Color(0xFF3A3A44),
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // HEADLINE & SUBTITLE
      Text(
        text = "Notifications",
        style = MaterialTheme.typography.displayLarge.copy(
          fontSize = 32.sp,
          fontWeight = FontWeight.SemiBold,
          lineHeight = 1.1.em,
          letterSpacing = (-0.64).sp, // -0.02em
          color = textColor
        )
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = "Your alerts and reminders.",
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = mutedTextColor
        )
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 1. MASTER NOTIFICATION TOGGLE CARD
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        border = BorderStroke(1.dp, borderColor)
      ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
          NotificationSettingRow(
            icon = if (notificationsEnabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
            title = "App Notifications",
            subtitle = if (notificationsEnabled) "Lock In notifications are enabled" else "Lock In notifications are disabled",
            checked = notificationsEnabled,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            isNightMode = isNightMode,
            containerColor = containerColor,
            onCheckedChange = onNotificationsEnabledToggle,
            testTag = "master_notification_switch"
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // NEW ACTIVITY REMINDERS GROUP
      Text(
        text = "ACTIVITY REMINDERS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          letterSpacing = 0.08.em
        ),
        color = mutedTextColor,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
      )

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        border = BorderStroke(1.dp, borderColor)
      ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
          NotificationSettingRow(
            icon = androidx.compose.material.icons.Icons.Outlined.NotificationsActive,
            title = "Habit Reminders",
            subtitle = "Smart alerts for pending rituals",
            checked = habitRemindersEnabled,
            textColor = textColor,
            mutedTextColor = mutedTextColor,
            isNightMode = isNightMode,
            containerColor = containerColor,
            onCheckedChange = onHabitRemindersToggle,
            testTag = "habit_reminder_toggle"
          )
        }
      }

      // 2. SYSTEM PERMISSION WARNING CARD
      if (notificationsEnabled && !isSystemPermissionGranted) {
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
          shape = RoundedCornerShape(28.dp),
          color = if (isNightMode) Color(0xFF3E1A1A) else Color(0xFFFFEBEE),
          border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.NotificationsActive,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "System Permission Required",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                  ),
                  color = if (isNightMode) Color.White else Color(0xFFC62828)
                )
                Text(
                  text = "System notifications are disabled for Lock In. Enable them in system settings.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                  color = if (isNightMode) Color.White.copy(alpha = 0.8f) else Color(0xFFB71C1C)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = onOpenSystemSettings,
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("open_system_notification_settings"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
              )
            ) {
              Text(
                text = "Open System Settings",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // 3. NOTIFICATION HISTORY SECTION
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ACTIVITY HISTORY",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.08.em
          ),
          color = mutedTextColor
        )

        if (appNotifications.isNotEmpty()) {
          Text(
            text = "${appNotifications.size} item${if (appNotifications.size > 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = mutedTextColor
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (appNotifications.isEmpty()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
          shape = RoundedCornerShape(28.dp),
          color = cardColor,
          border = BorderStroke(1.dp, borderColor)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCE5EE)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color(0xFF1A1A1F),
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "No notifications yet",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
              ),
              color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Alerts and reminders will appear here.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
              color = mutedTextColor
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          appNotifications.forEach { notification ->
            NotificationHistoryCard(
              notification = notification,
              isNightMode = isNightMode,
              textColor = textColor,
              mutedTextColor = mutedTextColor,
              borderColor = borderColor,
              cardColor = cardColor,
              onDelete = { onDeleteNotification(notification) }
            )
          }
        }
      }
    }

    // 4. UNDO DELETED NOTIFICATION SNACKBAR
    AnimatedVisibility(
      visible = pendingDeletedNotification != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 80.dp)
        .padding(horizontal = 24.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1F),
        border = BorderStroke(1.dp, Color(0xFF333333))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Notification deleted",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Medium,
              color = Color.White,
              fontSize = 14.sp
            )
          )

          TextButton(
            onClick = onUndoDeleteNotification,
            modifier = Modifier.testTag("undo_delete_notification")
          ) {
            Text(
              text = "UNDO",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = SignatureNeonLime,
                fontSize = 14.sp
              )
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationHistoryCard(
  notification: AppNotificationItem,
  isNightMode: Boolean,
  textColor: Color,
  mutedTextColor: Color,
  borderColor: Color,
  cardColor: Color,
  onDelete: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(28.dp))
      .testTag("notification_item_${notification.id}"),
    shape = RoundedCornerShape(28.dp),
    color = cardColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(Color(0xFFDCE5EE)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Notifications,
          contentDescription = null,
          tint = Color(0xFF1A1A1F),
          modifier = Modifier.size(20.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = notification.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
              .weight(1f, fill = false)
              .padding(end = 6.dp)
          )

          Text(
            text = formatRelativeNotificationTime(notification.timestamp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
            color = mutedTextColor,
            maxLines = 1,
            softWrap = false
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = notification.message,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.5.sp),
          color = mutedTextColor
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(36.dp)
          .testTag("delete_notification_${notification.id}")
      ) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Delete notification",
          tint = Color(0xFFA9B4C2),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun NotificationSettingRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  textColor: Color,
  mutedTextColor: Color,
  isNightMode: Boolean,
  containerColor: Color,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color(0xFFDCE5EE)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = Color(0xFF1A1A1F),
        modifier = Modifier.size(20.dp)
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp
        ),
        color = textColor
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        color = mutedTextColor
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = Color(0xFF0B0F19),
        checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFE2E8F0),
        uncheckedBorderColor = Color(0xFFE2E8F0)
      )
    )
  }
}
