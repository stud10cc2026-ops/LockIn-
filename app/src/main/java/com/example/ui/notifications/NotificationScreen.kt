package com.example.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.LightContainerNeutral
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureNeonLime

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
  onFocusRemindersToggle: (Boolean) -> Unit = {},
  onSessionCompletedToggle: (Boolean) -> Unit = {}
) {
  val backgroundColor = if (isNightMode) DarkBackground else LightPageBackground
  val cardColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedTextColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

  Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 100.dp)
        .testTag("notification_screen")
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // HEADER
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .background(cardColor)
            .clickable { onBackClick() }
            .testTag("notification_back_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = textColor,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
          text = "Notifications",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp
          ),
          color = textColor
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Manage notification preferences and activity history.",
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = mutedTextColor,
        modifier = Modifier.padding(start = 4.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 1. MASTER NOTIFICATION TOGGLE
      Text(
        text = "PREFERENCE",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 11.5.sp,
          letterSpacing = 0.8.sp
        ),
        color = mutedTextColor,
        modifier = Modifier.padding(start = 4.dp)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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

      // 2. SYSTEM PERMISSION WARNING
      if (notificationsEnabled && !isSystemPermissionGranted) {
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
          shape = RoundedCornerShape(16.dp),
          color = if (isNightMode) Color(0xFF3E1A1A) else Color(0xFFFFEBEE),
          border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
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
                    fontSize = 14.5.sp
                  ),
                  color = if (isNightMode) Color.White else Color(0xFFC62828)
                )
                Text(
                  text = "System notifications are disabled for Lock In. Please enable them in settings.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                  color = if (isNightMode) Color.White.copy(alpha = 0.8f) else Color(0xFFB71C1C)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = onOpenSystemSettings,
              modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("open_system_notification_settings"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
              )
            ) {
              Text(
                text = "Open System Settings",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // 3. NOTIFICATION HISTORY SECTION
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "HISTORY",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 0.8.sp
          ),
          color = mutedTextColor,
          modifier = Modifier.padding(start = 4.dp)
        )

        if (appNotifications.isNotEmpty()) {
          Text(
            text = "${appNotifications.size} item${if (appNotifications.size > 1) "s" else ""}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = mutedTextColor
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      if (appNotifications.isEmpty()) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
          shape = RoundedCornerShape(16.dp),
          color = cardColor,
          border = BorderStroke(1.dp, borderColor)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(containerColor),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = mutedTextColor,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "No notifications yet",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
              ),
              color = textColor
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
              text = "Notifications generated by Lock In will appear here.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
              color = mutedTextColor
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    // 4. UNDO SNACKBAR
    AnimatedVisibility(
      visible = pendingDeletedNotification != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1E1E),
        border = BorderStroke(1.dp, Color(0xFF333333))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Notification deleted",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Medium,
              color = Color.White,
              fontSize = 13.sp
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
                fontSize = 13.sp
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
      .clip(RoundedCornerShape(16.dp))
      .testTag("notification_item_${notification.id}"),
    shape = RoundedCornerShape(16.dp),
    color = cardColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(SignatureNeonLime),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Notifications,
          contentDescription = null,
          tint = Color(0xFF171817),
          modifier = Modifier.size(16.dp)
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
              fontSize = 13.5.sp
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
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = mutedTextColor,
            maxLines = 1,
            softWrap = false
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = notification.message,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
          color = mutedTextColor
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(32.dp)
          .testTag("delete_notification_${notification.id}")
      ) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = "Delete notification",
          tint = if (isNightMode) Color.White.copy(alpha = 0.5f) else Color(0xFF888888),
          modifier = Modifier.size(18.dp)
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
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(containerColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = textColor,
        modifier = Modifier.size(18.dp)
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 15.sp
        ),
        color = textColor
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = mutedTextColor
      )
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color(0xFF171817),
        checkedTrackColor = SignatureNeonLime,
        uncheckedThumbColor = if (isNightMode) Color(0xFFA0A0A5) else LightTextSecondary,
        uncheckedTrackColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
      )
    )
  }
}
