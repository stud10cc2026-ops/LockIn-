package com.example.ui.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextOffWhite

data class DayBarData(
  val dayLabel: String,
  val hours: Float,
  val displayTime: String = "",
  val isHighlight: Boolean = false
)

private val ThinBarChartOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinBarChartOutline",
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
    moveTo(5f, 20f)
    lineTo(5f, 14f)
    moveTo(12f, 20f)
    lineTo(12f, 8f)
    moveTo(19f, 20f)
    lineTo(19f, 4f)
  }.build()
}

private val ThinLightbulbOutlineIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "ThinLightbulbOutline",
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
    moveTo(9f, 21f)
    lineTo(15f, 21f)
    moveTo(10f, 18f)
    lineTo(14f, 18f)
    moveTo(12f, 3f)
    curveTo(7.58f, 3f, 4f, 6.58f, 4f, 11f)
    curveTo(4f, 13.68f, 5.32f, 16.05f, 7.34f, 17.5f)
    lineTo(16.66f, 17.5f)
    curveTo(18.68f, 16.05f, 20f, 13.68f, 20f, 11f)
    curveTo(20f, 6.58f, 16.42f, 3f, 12f, 3f)
    close()
  }.build()
}

/**
 * Stats Screen:
 * Restored typography, colors, and layout, with the Focus Time hero card styled in white.
 */
@Composable
fun StatsContent(
  weeklyTotalFormatted: String = "0m",
  monthlyTotalFormatted: String = "0m",
  totalSessions: Int = 0,
  avgSessionFormatted: String = "0 min",
  bestSessionFormatted: String = "0 min",
  activeDaysCount: Int = 0,
  totalDaysInPeriod: Int = 7,
  insightMessage: String = "Complete focus sessions to see insights.",
  weekDays: List<DayBarData> = listOf(
    DayBarData("Mon", 0f, isHighlight = false),
    DayBarData("Tue", 0f, isHighlight = false),
    DayBarData("Wed", 0f, isHighlight = false),
    DayBarData("Thu", 0f, isHighlight = false),
    DayBarData("Fri", 0f, isHighlight = true),
    DayBarData("Sat", 0f, isHighlight = false),
    DayBarData("Sun", 0f, isHighlight = false)
  ),
  isNightMode: Boolean = false,
  modifier: Modifier = Modifier
) {
  var selectedPeriod by remember { mutableStateOf(0) } // 0: Week, 1: Month

  val maxHours = maxOf(2.0f, weekDays.maxOfOrNull { it.hours } ?: 2.0f)

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

      // 1. HEADER WITH PERIOD SWITCH
      StatsHeader(
        selectedPeriod = selectedPeriod,
        isNightMode = isNightMode,
        onPeriodSelected = { selectedPeriod = it }
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 2. MAIN FOCUS SUMMARY CARD (WHITE)
      MainFocusSummaryCard(
        totalFocusTime = if (selectedPeriod == 0) weeklyTotalFormatted else monthlyTotalFormatted,
        periodLabel = if (selectedPeriod == 0) "This week" else "This month",
        isNightMode = isNightMode
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. WEEKLY OVERVIEW BAR CHART
      WeeklyOverviewSection(
        days = weekDays,
        maxHours = maxHours,
        isNightMode = isNightMode
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 4. KEY METRICS ROW
      KeyMetricsSection(
        sessionsCount = totalSessions,
        avgSession = avgSessionFormatted,
        bestSession = bestSessionFormatted,
        isNightMode = isNightMode
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 5. FOCUS CONSISTENCY SECTION
      ConsistencySection(
        activeDays = activeDaysCount,
        totalDays = totalDaysInPeriod,
        isNightMode = isNightMode
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 6. SUBTLE INSIGHT CARD
      InsightSection(
        message = insightMessage,
        isNightMode = isNightMode
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun StatsHeader(
  selectedPeriod: Int,
  isNightMode: Boolean,
  onPeriodSelected: (Int) -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .padding(end = 8.dp)
    ) {
      Text(
        text = "Stats",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 32.sp,
          lineHeight = 35.2.sp,
          letterSpacing = (-0.02).em
        ),
        color = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F),
        modifier = Modifier.testTag("stats_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "See how you're building your focus.",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        ),
        color = Color(0xFF8E96A3)
      )
    }

    // Segmented Period Switcher (Week / Month)
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = surfaceColor,
      border = BorderStroke(1.dp, borderColor),
      shadowElevation = 0.dp
    ) {
      Row(modifier = Modifier.padding(3.dp)) {
        PeriodTabItem(
          label = "Week",
          isSelected = selectedPeriod == 0,
          isNightMode = isNightMode,
          onClick = { onPeriodSelected(0) }
        )
        PeriodTabItem(
          label = "Month",
          isSelected = selectedPeriod == 1,
          isNightMode = isNightMode,
          onClick = { onPeriodSelected(1) }
        )
      }
    }
  }
}

@Composable
private fun PeriodTabItem(
  label: String,
  isSelected: Boolean,
  isNightMode: Boolean,
  onClick: () -> Unit
) {
  val surfaceColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
    } else {
      Color.Transparent
    },
    animationSpec = tween(durationMillis = 200)
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) Color(0xFF1A1A1F) else Color.White
    } else {
      Color(0xFF8E96A3)
    },
    animationSpec = tween(durationMillis = 200)
  )

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(surfaceColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 7.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        fontSize = 13.sp
      ),
      color = textColor,
      maxLines = 1,
      softWrap = false
    )
  }
}

@Composable
private fun MainFocusSummaryCard(
  totalFocusTime: String,
  periodLabel: String,
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(Color(0xFFDCE5EE)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = ThinBarChartOutlineIcon,
              contentDescription = null,
              tint = Color(0xFF1A1A1F),
              modifier = Modifier.size(14.dp)
            )
          }
          Text(
            text = "Focus time",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 15.sp
            ),
            color = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFDCE5EE))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = periodLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.08.em
            ),
            color = Color(0xFF1A1A1F)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      AnimatedContent(
        targetState = totalFocusTime,
        transitionSpec = {
          fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
        },
        label = "FocusTimeUpdate"
      ) { targetTime ->
        Text(
          text = targetTime,
          style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 38.sp,
            letterSpacing = (-0.02).em
          ),
          color = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Total time spent locked in $periodLabel",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium
        ),
        color = Color(0xFF8E96A3)
      )
    }
  }
}

@Composable
private fun WeeklyOverviewSection(
  days: List<DayBarData>,
  maxHours: Float,
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)
  val barEmptyBg = if (isNightMode) Color(0xFF2F3947) else Color(0xFFE3E9F0)
  val barActiveBg = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Weekly Activity",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Text(
          text = "Hours / day",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          ),
          color = mutedColor
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Vertical Bar Chart
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        days.forEach { day ->
          val fraction = if (maxHours > 0) (day.hours / maxHours).coerceIn(0.08f, 1.0f) else 0.08f
          val animatedHeightFraction by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 500),
            label = "bar_height"
          )

          val isActive = day.isHighlight || day.hours > 0

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.weight(1f)
          ) {
            if (day.hours > 0) {
              val labelText = if (day.displayTime.isNotEmpty()) day.displayTime else "${(day.hours * 60).toInt()}m"
              Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                color = textColor,
                modifier = Modifier.padding(bottom = 4.dp)
              )
            } else {
              Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
              modifier = Modifier
                .width(16.dp)
                .height((85 * animatedHeightFraction).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(if (isActive) barActiveBg else barEmptyBg)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = day.dayLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (day.isHighlight) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp
              ),
              color = if (day.isHighlight) textColor else mutedColor
            )
          }
        }
      }
    }
  }
}

@Composable
private fun KeyMetricsSection(
  sessionsCount: Int,
  avgSession: String,
  bestSession: String,
  isNightMode: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    MetricItemCard(
      value = "$sessionsCount",
      label = "Sessions",
      isNightMode = isNightMode,
      modifier = Modifier.weight(1f)
    )

    MetricItemCard(
      value = avgSession,
      label = "Avg session",
      isNightMode = isNightMode,
      modifier = Modifier.weight(1f)
    )

    MetricItemCard(
      value = bestSession,
      label = "Best session",
      isNightMode = isNightMode,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun MetricItemCard(
  value: String,
  label: String,
  isNightMode: Boolean,
  modifier: Modifier = Modifier
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(vertical = 18.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp
        ),
        color = textColor
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 14.sp
        ),
        color = mutedColor,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun ConsistencySection(
  activeDays: Int,
  totalDays: Int,
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Consistency",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = "You showed up this week.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
          ),
          color = mutedColor
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFDCE5EE)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(Color(0xFF1A1A1F))
          )
          Text(
            text = "$activeDays of $totalDays days",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            ),
            color = Color(0xFF1A1A1F)
          )
        }
      }
    }
  }
}

@Composable
private fun InsightSection(
  message: String,
  isNightMode: Boolean
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) DarkTextOffWhite else Color(0xFF1A1A1F)
  val mutedColor = Color(0xFF8E96A3)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier.padding(18.dp),
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
          imageVector = ThinLightbulbOutlineIcon,
          contentDescription = null,
          tint = Color(0xFF1A1A1F),
          modifier = Modifier.size(18.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "FOCUS INSIGHT",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.08.em
          ),
          color = mutedColor
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp
          ),
          color = textColor
        )
      }
    }
  }
}
