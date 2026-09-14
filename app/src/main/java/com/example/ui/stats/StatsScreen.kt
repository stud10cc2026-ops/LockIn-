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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
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

data class DayBarData(
  val dayLabel: String,
  val hours: Float,
  val displayTime: String = "",
  val isHighlight: Boolean = false
)

/**
 * Stats Screen:
 * Displays focus statistics, weekly bar chart, key metrics, consistency tracking,
 * and a subtle focus insight in a clean, minimal design.
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
  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val textColor = if (isNightMode) Color.White else LightTextPrimary
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

      // 1. HEADER WITH PERIOD SWITCH
      StatsHeader(
        selectedPeriod = selectedPeriod,
        isNightMode = isNightMode,
        textColor = textColor,
        mutedColor = mutedColor,
        onPeriodSelected = { selectedPeriod = it }
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 2. MAIN FOCUS SUMMARY CARD
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
  textColor: Color,
  mutedColor: Color,
  onPeriodSelected: (Int) -> Unit
) {
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

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
          fontWeight = FontWeight.Bold,
          fontSize = 30.sp,
          lineHeight = 36.sp,
          letterSpacing = (-0.5).sp
        ),
        color = textColor,
        modifier = Modifier.testTag("stats_header_title")
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "See how you're building your focus.",
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = mutedColor
      )
    }

    // Segmented Period Switcher (Week / Month)
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = containerColor,
      border = BorderStroke(1.dp, borderColor)
    ) {
      Row(modifier = Modifier.padding(2.dp)) {
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
      if (isNightMode) SignatureNeonLime else DarkButtonCharcoal
    } else {
      Color.Transparent
    },
    animationSpec = tween(durationMillis = 200)
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isNightMode) DarkTextOnLime else Color.White
    } else {
      if (isNightMode) DarkTextSecondary else LightTextSecondary
    },
    animationSpec = tween(durationMillis = 200)
  )

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(surfaceColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.sp
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
  val surfaceColor = if (isNightMode) DarkCardSurface else Color(0xFF141414)
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFF262626)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
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
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(SignatureNeonLime),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.BarChart,
              contentDescription = null,
              tint = DarkButtonCharcoal,
              modifier = Modifier.size(14.dp)
            )
          }
          Text(
            text = "Focus time",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            ),
            color = Color.White
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF262626))
            .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
          Text(
            text = periodLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.6.sp
            ),
            color = Color(0xFFD4D4D4)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

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
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            letterSpacing = (-0.8).sp
          ),
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = "Total time spent locked in $periodLabel",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
        color = Color(0xFFA3A3A3)
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
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val barBg = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

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
        Text(
          text = "Weekly Activity",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.5.sp
          ),
          color = textColor
        )

        Text(
          text = "Hours / day",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
          color = mutedColor
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Minimal Vertical Bar Chart
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
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                color = if (day.isHighlight) textColor else mutedColor,
                modifier = Modifier.padding(bottom = 4.dp)
              )
            } else {
              Spacer(modifier = Modifier.height(14.dp))
            }

            Box(
              modifier = Modifier
                .width(16.dp)
                .height((90 * animatedHeightFraction).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(
                  if (day.isHighlight) SignatureNeonLime
                  else if (day.hours > 0) {
                    if (isNightMode) Color.White else DarkButtonCharcoal
                  } else barBg
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = day.dayLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (day.isHighlight) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
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
    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        ),
        color = textColor
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp
        ),
        color = mutedColor
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
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) Color.White else LightTextPrimary
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
      Column {
        Text(
          text = "Consistency",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "You showed up this week.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.5.sp
          ),
          color = mutedColor
        )
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isNightMode) DarkContainerNeutral else SignatureNeonLime
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (isNightMode) SignatureNeonLime else DarkButtonCharcoal)
          )
          Text(
            text = "$activeDays of $totalDays days",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            ),
            color = if (isNightMode) Color.White else DarkButtonCharcoal
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
  val surfaceColor = if (isNightMode) DarkCardSurface else LightCardSurface
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val containerColor = if (isNightMode) DarkContainerNeutral else LightContainerNeutral

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(containerColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Lightbulb,
          contentDescription = null,
          tint = if (isNightMode) SignatureNeonLime else textColor,
          modifier = Modifier.size(16.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "FOCUS INSIGHT",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            letterSpacing = 0.6.sp
          ),
          color = mutedColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = message,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.5.sp,
            lineHeight = 17.sp
          ),
          color = textColor
        )
      }
    }
  }
}
