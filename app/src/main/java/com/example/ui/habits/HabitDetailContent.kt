package com.example.ui.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.app.PendingIntent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

@Composable
fun HabitDetailContent(
  habit: HabitItem,
  habitLogs: Map<String, Map<String, Boolean>>,
  isNightMode: Boolean,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onRemove: () -> Unit,
  onToggleDate: ((String) -> Unit)? = null,
  onUpdateHabit: ((HabitItem) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val pageBgGradient = if (isNightMode) {
    Brush.verticalGradient(listOf(DarkBackground, DarkBackground))
  } else {
    Brush.verticalGradient(listOf(Color(0xFFE8ECF3), Color(0xFFFFFFFF)))
  }

  val textColor = if (isNightMode) Color.White else Color(0xFF1C1E23)
  val secondaryText = Color(0xFF8E96A3)
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)

  val today = LocalDate.now()
  val startDate = try { LocalDate.parse(habit.startDateStr) } catch (_: Exception) { today }
  val startedDateFormatted = startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

  // Stats Calculations
  val streak = calculateCurrentStreak(habit.id, habitLogs, today)
  val currentMonth = YearMonth.from(today)
  val daysInMonth = currentMonth.lengthOfMonth()
  val monthDone = calculateMonthDone(habit.id, habitLogs, currentMonth)
  val monthPctVal = if (daysInMonth > 0) (monthDone.toFloat() / daysInMonth * 100f) else 0f
  val monthPct = if (monthDone > 0 && monthPctVal < 1f) 1 else monthPctVal.roundToInt()
  val monthStr = "${monthPct}%"

  val daysInYear = java.time.Year.of(today.year).length()
  val yearDone = calculateYearDone(habit.id, habitLogs, today.year)
  val yearPctVal = if (daysInYear > 0) (yearDone.toFloat() / daysInYear * 100f) else 0f
  val yearPct = if (yearDone > 0 && yearPctVal < 1f) 1 else yearPctVal.roundToInt()
  val yearStr = "${yearPct}%"

  // Shared block calculation (same as HabitsContent)
  val daysSinceStartToday = ChronoUnit.DAYS.between(startDate, today).toInt()
  val blockIndexToday = if (daysSinceStartToday >= 0) daysSinceStartToday / 5 else (daysSinceStartToday - 4) / 5
  val blockEndDate = startDate.plusDays(((blockIndexToday * 5) + 4).toLong())
  val totalDaysInBlock = (ChronoUnit.DAYS.between(startDate, blockEndDate).toInt() + 1).coerceAtLeast(0)

  var doneCountTotal = 0
  habitLogs.forEach { (dateStr, map) ->
    val logDate = try { LocalDate.parse(dateStr) } catch (_: Exception) { null }
    if (logDate != null && !logDate.isBefore(startDate) && map[habit.id] == true) {
      doneCountTotal++
    }
  }
  val percentage = if (totalDaysInBlock > 0) (doneCountTotal.toFloat() / totalDaysInBlock * 100).roundToInt() else 0

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Transparent,
    topBar = {
      Row(
        modifier = Modifier
          .statusBarsPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 4.dp,
          modifier = Modifier
            .size(44.dp)
            .clickable { onBack() }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF1A1A1F),
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Box {
          IconButton(onClick = { showMenu = true }) {
            Icon(
              Icons.Default.MoreVert,
              contentDescription = "More",
              tint = textColor
            )
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Edit") },
              onClick = {
                showMenu = false
                onEdit()
              }
            )
            DropdownMenuItem(
              text = { Text("Remove") },
              onClick = {
                showMenu = false
                showDeleteConfirm = true
              }
            )
            val context = LocalContext.current
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Reminder")
                  Switch(
                    checked = habit.remindersEnabled,
                    onCheckedChange = { enabled ->
                      showMenu = false
                      onUpdateHabit?.invoke(habit.copy(remindersEnabled = enabled))
                    },
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
              },
              onClick = { /* Toggle handled by switch */ }
            )
            DropdownMenuItem(
              text = { Text("Add Screen Widget") },
              onClick = {
                showMenu = false
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, HabitWidgetReceiver::class.java)

                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                  val successCallback = PendingIntent.getBroadcast(
                    context,
                    habit.id.hashCode(),
                    Intent(context, HabitWidgetReceiver::class.java).apply {
                      action = "BIND_HABIT_TO_WIDGET"
                      putExtra("habit_id", habit.id)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                  )
                  appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                }
              }
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(pageBgGradient)
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 140.dp)
    ) {
      // Title Section
      Text(
        text = habit.name,
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 32.sp,
          letterSpacing = (-0.02).em
        ),
        color = textColor
      )
      Text(
        text = "Started $startedDateFormatted",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 15.sp
        ),
        color = secondaryText
      )

      Spacer(modifier = Modifier.height(32.dp))

      // OVERVIEW CARD
      Text(
        text = "OVERVIEW",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.1.em,
          fontSize = 11.sp
        ),
        color = secondaryText,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
      )
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(28.dp)) {
          Text(
            text = "$percentage%",
            style = MaterialTheme.typography.displayLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 64.sp,
              letterSpacing = (-0.02).em
            ),
            color = textColor
          )
          Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
          ) {
            StatItem(label = "Month", value = monthStr, isNightMode = isNightMode)
            StatItem(label = "Year", value = yearStr, isNightMode = isNightMode)
            StatItem(label = "Streak", value = streak.toString(), isNightMode = isNightMode)
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // HISTORY CARD
      Text(
        text = "HISTORY",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.1.em,
          fontSize = 11.sp
        ),
        color = secondaryText,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
      )
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
      ) {
        HistoryCalendar(
          habitId = habit.id,
          startDate = startDate,
          habitLogs = habitLogs,
          isNightMode = isNightMode,
          onToggleDate = onToggleDate
        )
      }
    }

    if (showDeleteConfirm) {
      LockInConfirmationDialog(
        icon = Icons.Outlined.Delete,
        title = "Remove Habit?",
        message = "Are you sure you want to delete '${habit.name}'? This action cannot be undone.",
        confirmButtonText = "Delete",
        isNightMode = isNightMode,
        onDismiss = { showDeleteConfirm = false },
        onConfirm = {
          showDeleteConfirm = false
          onRemove()
        }
      )
    }
  }
}

@Composable
private fun LockInConfirmationDialog(
  icon: ImageVector,
  title: String,
  message: String,
  confirmButtonText: String,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val surfaceBg = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val secondaryText = if (isNightMode) Color(0xFF8E96A3) else Color(0xFF8E96A3)
  val iconBgColor = if (isNightMode) Color(0xFF2D323B) else Color(0xFFF0F2F5)

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
          shape = RoundedCornerShape(28.dp),
          spotColor = Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(28.dp),
      color = surfaceBg,
      shadowElevation = 0.dp
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(iconBgColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = message,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 21.sp
          ),
          color = secondaryText,
          textAlign = TextAlign.Center
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
              containerColor = if (isNightMode) Color(0xFF252731) else Color(0xFFEEF1F6),
              contentColor = textColor
            )
          ) {
            Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }

          Button(
            onClick = onConfirm,
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) Color.White else Color(0xFF1C1E23),
              contentColor = if (isNightMode) Color.Black else Color.White
            )
          ) {
            Text(confirmButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun StatItem(label: String, value: String, isNightMode: Boolean, modifier: Modifier = Modifier) {
  val textColor = if (isNightMode) Color.White else Color(0xFF1C1E23)
  val secondaryText = Color(0xFF8E96A3)
  Column(modifier = modifier) {
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = secondaryText)
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
      color = textColor
    )
  }
}

@Composable
private fun HistoryCalendar(
  habitId: String,
  startDate: LocalDate,
  habitLogs: Map<String, Map<String, Boolean>>,
  isNightMode: Boolean,
  onToggleDate: ((String) -> Unit)? = null
) {
  val context = LocalContext.current
  var currentMonth by remember { mutableStateOf(YearMonth.now()) }
  val textColor = if (isNightMode) Color.White else Color(0xFF1C1E23)
  val secondaryText = Color(0xFF8E96A3)
  val today = LocalDate.now()

  Column(modifier = Modifier.padding(20.dp)) {
    // Month Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = currentMonth.minusMonths(1).month.name.take(3),
        color = secondaryText.copy(alpha = 0.3f),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.clickable { currentMonth = currentMonth.minusMonths(1) }
      )
      Text(
        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = textColor
      )
      Text(
        text = currentMonth.plusMonths(1).month.name.take(3),
        color = secondaryText.copy(alpha = 0.3f),
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.clickable { currentMonth = currentMonth.plusMonths(1) }
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Weekdays
    Row(modifier = Modifier.fillMaxWidth()) {
      listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
        Text(
          text = day,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
          color = secondaryText
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Calendar Grid
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    // 1 (Monday) to 7 (Sunday)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    
    // Calculate previous month padding days
    val prevMonth = currentMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    val paddingDays = firstDayOfWeek - 1

    val days = mutableListOf<CalendarDay>()
    
    // Add prev month days
    for (i in paddingDays - 1 downTo 0) {
      days.add(CalendarDay(prevMonth.atDay(daysInPrevMonth - i), isCurrentMonth = false))
    }
    
    // Add current month days
    for (i in 1..daysInMonth) {
      days.add(CalendarDay(currentMonth.atDay(i), isCurrentMonth = true))
    }
    
    // Add next month days
    val nextMonth = currentMonth.plusMonths(1)
    val remainingDays = 42 - days.size
    for (i in 1..remainingDays) {
      days.add(CalendarDay(nextMonth.atDay(i), isCurrentMonth = false))
    }

    val rows = days.chunked(7)
    rows.forEach { row ->
      Row(modifier = Modifier.fillMaxWidth()) {
        row.forEach { day ->
          Box(
            modifier = Modifier
              .weight(1f)
              .aspectRatio(1f)
              .graphicsLayer {
                alpha = if (day.date == today) 1.0f else 0.6f
              }
              .clickable(
                indication = if (day.date == today) LocalIndication.current else null,
                interactionSource = remember { MutableInteractionSource() }
              ) {
                if (day.date == today) {
                  onToggleDate?.invoke(day.date.toString())
                } else {
                  Toast.makeText(context, "You can only update today's habit", Toast.LENGTH_SHORT).show()
                }
              },
            contentAlignment = Alignment.Center
          ) {
            val isDone = habitLogs[day.date.toString()]?.get(habitId) == true
            val isToday = day.date == today
            val isBeforeStart = day.date.isBefore(startDate)
            val isFuture = day.date.isAfter(today)
            
            if (isToday) {
              if (isDone) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .background(textColor, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = day.date.dayOfMonth.toString(), color = if (isNightMode) DarkCardSurface else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              } else {
                Text(text = day.date.dayOfMonth.toString(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              }
            } else if (isDone) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .border(1.dp, textColor, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(text = day.date.dayOfMonth.toString(), color = textColor, fontSize = 14.sp)
              }
            } else {
              Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                  !day.isCurrentMonth -> secondaryText.copy(alpha = 0.3f)
                  isFuture || isBeforeStart -> secondaryText
                  else -> secondaryText // Missed
                },
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }
  }
}

data class CalendarDay(val date: LocalDate, val isCurrentMonth: Boolean)
