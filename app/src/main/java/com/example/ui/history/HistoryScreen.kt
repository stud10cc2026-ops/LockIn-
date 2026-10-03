package com.example.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.ui.home.FocusSessionHistoryItem
import com.example.ui.theme.DarkBackground
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
import kotlinx.coroutines.delay

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

private val ThinCheckOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinCheckOutline",
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
    moveTo(20f, 6f)
    lineTo(9f, 17f)
    lineTo(4f, 12f)
  }.build()
}

private val ThinStopOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinStopOutline",
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
    moveTo(7.5f, 6f)
    lineTo(16.5f, 6f)
    curveTo(17.33f, 6f, 18f, 6.67f, 18f, 7.5f)
    lineTo(18f, 16.5f)
    curveTo(18f, 17.33f, 17.33f, 18f, 16.5f, 18f)
    lineTo(7.5f, 18f)
    curveTo(6.67f, 18f, 6f, 17.33f, 6f, 16.5f)
    lineTo(6f, 7.5f)
    curveTo(6f, 6.67f, 6.67f, 6f, 7.5f, 6f)
    close()
  }.build()
}

/**
 * History Screen:
 * Displays previous completed and ended focus sessions in a calm,
 * minimal chronological list matching the Home page styling.
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
        .padding(bottom = 120.dp) // Space for floating nav
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. HEADER
      HistoryHeader(
        isNightMode = isNightMode
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
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val iconColor = if (isNightMode) DarkTextOffWhite else Color(0xFF3A3A44)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "History",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 32.sp,
          lineHeight = 35.2.sp,
          letterSpacing = (-0.02).em
        ),
        color = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F),
        modifier = Modifier.testTag("history_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Your focus, day by day.",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        ),
        color = Color(0xFF8E96A3)
      )
    }

    // Calendar Symbol Button (44px white circle, thin outline calendar icon, no shadow)
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(surfaceColor)
        .clickable { /* Filter action */ }
        .testTag("history_filter_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = ThinCalendarOutlineIcon,
        contentDescription = "Filter history",
        tint = iconColor,
        modifier = Modifier.size(22.dp)
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
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val dividerColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE8EDF3)

  Column {
    // Section Header / Day Label ("TODAY")
    Text(
      text = dateGroup.uppercase(),
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.08.em
      ),
      color = Color(0xFF8E96A3),
      modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )

    // Grouped session rows inside 28px rounded card
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(28.dp),
      color = surfaceColor,
      border = BorderStroke(1.dp, borderColor),
      shadowElevation = 0.dp
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
              color = dividerColor,
              thickness = 1.dp,
              modifier = Modifier.padding(horizontal = 20.dp)
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
  val durationColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val subtextColor = Color(0xFF8E96A3)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = "${item.durationMinutes} min",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp
        ),
        color = durationColor
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = item.sessionType,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp
          ),
          color = subtextColor
        )

        Text(
          text = "·",
          color = subtextColor,
          fontSize = 13.sp
        )

        Text(
          text = item.timestampFormatted,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp
          ),
          color = subtextColor
        )
      }
    }

    // Status Indicator Badge
    if (item.isCompleted) {
      val chipBg = Color(0xFFDCE5EE)
      val chipTextColor = Color(0xFF1A1A1F)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = ThinCheckOutlineIcon,
            contentDescription = null,
            tint = chipTextColor,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Completed",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            ),
            color = chipTextColor
          )
        }
      }
    } else {
      val chipBg = Color(0xFFECEFF3)
      val chipTextColor = Color(0xFF6B7380)
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = ThinStopOutlineIcon,
            contentDescription = null,
            tint = chipTextColor,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Ended early",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            ),
            color = chipTextColor
          )
        }
      }
    }
  }
}

@Composable
private fun HistoryEmptyState(isNightMode: Boolean) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)
  val containerColor = if (isNightMode) DarkContainerNeutral else Color(0xFFECEFF3)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 24.dp),
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
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
