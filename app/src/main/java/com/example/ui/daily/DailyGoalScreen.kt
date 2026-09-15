package com.example.ui.daily

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyGoal
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextOffWhite
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureNeonLime
import com.example.widget.DailyGoalWidgetProvider
import java.time.LocalDate

@Composable
fun DailyGoalContent(
  goal: DailyGoal?,
  isNightMode: Boolean,
  onCreateGoal: (String) -> Unit,
  onResetGoal: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val today = remember { LocalDate.now() }
  var showResetDialog by remember { mutableStateOf(false) }

  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val cardBg = if (isNightMode) DarkCardSurface else CardWhite
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      if (goal == null) {
        // STATE 1: Goal Duration Selection
        GoalSetupSection(
          isNightMode = isNightMode,
          onSelectDuration = { durationType ->
            onCreateGoal(durationType)
            Toast.makeText(context, "Daily Goal started!", Toast.LENGTH_SHORT).show()
          }
        )
      } else {
        // STATE 2: Active or Completed Goal Display
        val currentDay = goal.getCurrentDay(today)
        val remainingDays = goal.getRemainingDays(today)
        val isCompleted = goal.isCompleted(today)

        GoalActiveSection(
          goal = goal,
          currentDay = currentDay,
          remainingDays = remainingDays,
          isCompleted = isCompleted,
          isNightMode = isNightMode,
          onAddToHomeScreen = {
            val pinned = DailyGoalWidgetProvider.requestPinAppWidget(context)
            if (pinned) {
              Toast.makeText(context, "Adding widget to Home Screen...", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(
                context,
                "To add widget: Long-press your home screen, tap Widgets, and choose Lock In™ Daily Goal.",
                Toast.LENGTH_LONG
              ).show()
            }
          },
          onStartNewGoal = {
            onResetGoal()
          },
          onRequestReset = {
            showResetDialog = true
          }
        )
      }

      // Bottom clearance for floating bottom navigation bar
      Spacer(modifier = Modifier.height(110.dp))
    }

    if (showResetDialog) {
      AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = {
          Text("Reset Daily Goal", fontWeight = FontWeight.Bold, color = textColor)
        },
        text = {
          Text(
            "Are you sure you want to end this goal and choose a new duration?",
            color = mutedColor
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showResetDialog = false
              onResetGoal()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = SignatureNeonLime,
              contentColor = DarkButtonCharcoal
            )
          ) {
            Text("Reset Goal", fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showResetDialog = false }) {
            Text("Cancel", color = mutedColor)
          }
        },
        containerColor = cardBg,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(20.dp)
      )
    }
  }
}

/**
 * First-time goal creation: Choose 1 Week, 1 Month, or 1 Year.
 */
@Composable
private fun GoalSetupSection(
  isNightMode: Boolean,
  onSelectDuration: (String) -> Unit
) {
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val cardBg = if (isNightMode) DarkCardSurface else CardWhite
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = "Daily Goal",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
      ),
      color = textColor,
      modifier = Modifier.testTag("daily_goal_title")
    )

    Text(
      text = "Choose how long you want to stay locked in.",
      style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
      color = mutedColor,
      modifier = Modifier.testTag("daily_goal_subtitle")
    )
  }

  Spacer(modifier = Modifier.height(8.dp))

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    val options = listOf(
      Triple("1_WEEK", "1 Week", "7 days of relentless focus"),
      Triple("1_MONTH", "1 Month", "30 days of consistent discipline"),
      Triple("1_YEAR", "1 Year", "365 days of ultimate transformation")
    )

    options.forEach { (type, label, description) ->
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .border(1.dp, borderColor, RoundedCornerShape(18.dp))
          .clickable { onSelectDuration(type) }
          .testTag("goal_option_${type.lowercase()}"),
        color = cardBg,
        shape = RoundedCornerShape(18.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = label,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              ),
              color = textColor
            )
            Text(
              text = description,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
              color = mutedColor
            )
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(if (isNightMode) DarkButtonCharcoal else Color(0xFFF0F0F4)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CalendarToday,
              contentDescription = label,
              tint = if (isNightMode) Color.White else DarkButtonCharcoal,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

/**
 * Display active or completed daily goal progress card.
 */
@Composable
private fun GoalActiveSection(
  goal: DailyGoal,
  currentDay: Int,
  remainingDays: Int,
  isCompleted: Boolean,
  isNightMode: Boolean,
  onAddToHomeScreen: () -> Unit,
  onStartNewGoal: () -> Unit,
  onRequestReset: () -> Unit
) {
  val today = remember { LocalDate.now() }
  val textColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val cardBg = if (isNightMode) DarkCardSurface else CardWhite
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder

  // Title Header
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = "Daily Goal",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 28.sp,
          letterSpacing = (-0.5).sp
        ),
        color = textColor
      )
      Text(
        text = "${goal.durationLabel} Goal",
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        color = mutedColor
      )
    }

    if (isCompleted) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SignatureNeonLime.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, SignatureNeonLime.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = SignatureNeonLime,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "COMPLETED",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              letterSpacing = 0.5.sp
            ),
            color = SignatureNeonLime
          )
        }
      }
    }
  }

  // Compact dark charcoal rounded progress card (matching reference)
  val dayFormatted = String.format("%02d", currentDay)
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("daily_goal_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF131417)),
    border = BorderStroke(1.dp, Color(0xFF24262E)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Top Row: Tag / Label + Progress count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(SignatureNeonLime)
          )
          Text(
            text = "DAY $dayFormatted",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              letterSpacing = 0.5.sp
            ),
            color = Color.White,
            modifier = Modifier.testTag("daily_goal_current_day")
          )
        }

        Text(
          text = if (isCompleted) "COMPLETE" else "Day $currentDay of ${goal.totalDays}",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            letterSpacing = 0.3.sp
          ),
          color = if (isCompleted) SignatureNeonLime else Color(0xFF8A92A0),
          modifier = Modifier.testTag("daily_goal_progress_text")
        )
      }

      // Exact Dotted Progress Grid
      DailyGoalDotGrid(
        totalDays = goal.totalDays,
        currentDay = currentDay,
        isCompleted = isCompleted,
        isNightMode = isNightMode,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
      )

      // Bottom Row: Year on left ("2026"), Days left on right ("22 days left" / "178 days left")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = today.year.toString(),
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
          ),
          color = Color(0xFF8A92A0)
        )

        val remainingText = when {
          isCompleted -> "Goal complete"
          remainingDays == 1 -> "1 day left"
          else -> "$remainingDays days left"
        }
        Text(
          text = remainingText,
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.3.sp
          ),
          color = if (isCompleted) SignatureNeonLime else Color(0xFF8A92A0),
          modifier = Modifier.testTag("daily_goal_status_text")
        )
      }
    }
  }

      // Start & End Dates footer
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isNightMode) Color(0xFF141518) else Color(0xFFF7F7F9),
        border = BorderStroke(1.dp, borderColor)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "START DATE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = mutedColor
            )
            Text(
              text = goal.startDateStr,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              ),
              color = textColor
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "END DATE",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              ),
              color = mutedColor
            )
            Text(
              text = goal.endDateStr,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              ),
              color = textColor
            )
          }
        }
      }

  // "+ Add to Home Screen" Button
  Button(
    onClick = onAddToHomeScreen,
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .testTag("add_to_home_screen_button"),
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isNightMode) DarkCardSurface else Color.White,
      contentColor = textColor
    ),
    border = BorderStroke(1.dp, borderColor)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(
        imageVector = Icons.Outlined.AddBox,
        contentDescription = "+ Add to Home Screen",
        tint = SignatureNeonLime,
        modifier = Modifier.size(20.dp)
      )
      Text(
        text = "+ Add to Home Screen",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        ),
        color = textColor
      )
    }
  }

  // Goal Complete / Reset actions
  if (isCompleted) {
    Button(
      onClick = onStartNewGoal,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("start_new_goal_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = SignatureNeonLime,
        contentColor = DarkButtonCharcoal
      )
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Refresh,
          contentDescription = null,
          tint = DarkButtonCharcoal,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Start New Goal",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        )
      }
    }
  } else {
    // Secondary option to change/reset active goal
    TextButton(
      onClick = onRequestReset,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("reset_daily_goal_button")
    ) {
      Text(
        text = "Change Goal Duration",
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp
        ),
        color = mutedColor
      )
    }
  }
}

/**
 * Minimalist dot matrix progress visualization representing the user's actual goal duration
 * and progress, closely reproducing the reference's exact dotted-progress design.
 */
@Composable
fun DailyGoalDotGrid(
  totalDays: Int,
  currentDay: Int,
  isCompleted: Boolean,
  isNightMode: Boolean,
  modifier: Modifier = Modifier
) {
  val cols = when {
    totalDays <= 7 -> 7
    totalDays <= 31 -> 10
    else -> 28
  }
  val rows = (totalDays + cols - 1) / cols

  val canvasHeight = when {
    totalDays <= 7 -> 32.dp
    totalDays <= 31 -> 48.dp
    else -> 84.dp
  }

  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(canvasHeight)
  ) {
    val spacingX = size.width / cols
    val spacingY = size.height / rows
    val dotRadius = when {
      totalDays <= 7 -> 5.dp.toPx()
      totalDays <= 31 -> 4.dp.toPx()
      else -> 2.6.dp.toPx()
    }

    for (day in 1..totalDays) {
      val col = (day - 1) % cols
      val row = (day - 1) / cols

      val cx = col * spacingX + spacingX / 2f
      val cy = row * spacingY + spacingY / 2f

      val isPassed = day < currentDay || isCompleted
      val isCurrent = day == currentDay && !isCompleted

      val color = when {
        isCurrent -> SignatureNeonLime
        isPassed -> if (isNightMode) Color.White.copy(alpha = 0.95f) else DarkButtonCharcoal
        else -> if (isNightMode) Color(0xFF282B33) else Color(0xFFE2E6EC)
      }

      drawCircle(
        color = color,
        radius = if (isCurrent) dotRadius * 1.25f else dotRadius,
        center = Offset(cx, cy)
      )
    }
  }
}
