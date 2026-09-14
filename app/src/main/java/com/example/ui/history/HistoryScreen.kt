package com.example.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.home.FocusSessionHistoryItem
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextOffWhite
import com.example.ui.theme.DarkTextOnLime
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.LightContainerNeutral
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureNeonLime
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenLight
import kotlinx.coroutines.delay

/**
 * History Screen:
 * Displays previous completed and ended focus sessions in a calm,
 * minimal chronological list with subtle dividers and clean status indicators.
 */
@Composable
fun HistoryContent(
  historyItems: List<FocusSessionHistoryItem>,
  isNightMode: Boolean = false,
  onItemClick: (FocusSessionHistoryItem) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val completedItems = historyItems.filter { it.isCompleted }
  val groupedHistory = completedItems.groupBy { it.dateGroup }

  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 90.dp) // Space for bottom navigation
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. HEADER
      HistoryHeader(
        isNightMode = isNightMode,
        textColor = textColor,
        mutedColor = mutedColor
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 2. HISTORY LIST OR EMPTY STATE
      if (completedItems.isEmpty()) {
        HistoryEmptyState(isNightMode = isNightMode)
      } else {
        groupedHistory.entries.forEachIndexed { index, (dateGroup, items) ->
          val delayMs = index * 50
          var isVisible by remember { mutableStateOf(false) }
          LaunchedEffect(Unit) {
            delay(delayMs.toLong())
            isVisible = true
          }
          AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut()
          ) {
            Column {
              HistoryDateSection(
                dateGroup = dateGroup,
                items = items,
                isNightMode = isNightMode,
                onItemClick = onItemClick
              )
              Spacer(modifier = Modifier.height(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryHeader(
  isNightMode: Boolean,
  textColor: Color,
  mutedColor: Color
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "History",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 30.sp,
          lineHeight = 36.sp,
          letterSpacing = (-0.5).sp
        ),
        color = textColor,
        modifier = Modifier.testTag("history_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Your focus, day by day.",
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = mutedColor
      )
    }

    // Filter/Calendar Symbol Button
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(surfaceColor)
        .border(1.dp, borderColor, CircleShape)
        .clickable { /* Filter action */ }
        .testTag("history_filter_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.CalendarToday,
        contentDescription = "Filter history",
        tint = textColor,
        modifier = Modifier.size(17.dp)
      )
    }
  }
}

@Composable
private fun HistoryDateSection(
  dateGroup: String,
  items: List<FocusSessionHistoryItem>,
  isNightMode: Boolean,
  onItemClick: (FocusSessionHistoryItem) -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Column {
    // Section Header
    Text(
      text = dateGroup.uppercase(),
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        letterSpacing = 0.8.sp
      ),
      color = mutedColor,
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Grouped session rows inside a clean rounded surface
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = surfaceColor,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Column {
        items.forEachIndexed { index, item ->
          HistorySessionRow(
            item = item,
            isNightMode = isNightMode,
            onClick = { onItemClick(item) }
          )
          if (index < items.size - 1) {
            HorizontalDivider(
              color = borderColor,
              thickness = 0.75.dp,
              modifier = Modifier.padding(horizontal = 16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HistorySessionRow(
  item: FocusSessionHistoryItem,
  isNightMode: Boolean,
  onClick: () -> Unit
) {
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "${item.durationMinutes} min",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.5.sp
          ),
          color = textColor
        )

        Text(
          text = "·",
          color = mutedColor,
          fontSize = 14.sp
        )

        Text(
          text = item.sessionType,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp
          ),
          color = mutedColor
        )
      }

      Text(
        text = item.timestampFormatted,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.5.sp
        ),
        color = mutedColor
      )
    }

    // Status Indicator Badge
    if (item.isCompleted) {
      val badgeBg = if (isNightMode) SignatureNeonLime.copy(alpha = 0.16f) else SignatureNeonLime
      val badgeContentColor = if (isNightMode) SignatureNeonLime else DarkTextOnLime
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeBg,
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = badgeContentColor,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "Completed",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            ),
            color = badgeContentColor
          )
        }
      }
    } else {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.HourglassEmpty,
            contentDescription = null,
            tint = mutedColor,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "Ended early",
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
}

@Composable
private fun HistoryEmptyState(isNightMode: Boolean) {
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 24.dp),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp),
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
          imageVector = Icons.Outlined.History,
          contentDescription = null,
          tint = mutedColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "No focus sessions yet",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp
        ),
        color = textColor
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Completed sessions will appear here.",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 13.sp
        ),
        color = mutedColor
      )
    }
  }
}
