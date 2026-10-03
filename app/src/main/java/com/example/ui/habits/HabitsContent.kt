package com.example.ui.habits

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import android.widget.Toast
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.notifications.HabitReminderScheduler
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

private val HABIT_COLORS_HEX = listOf(
  "#8B7FD6", // Purple
  "#4AC1D6", // Sky Blue
  "#F0716B", // Coral
  "#4A9FE0", // Blue
  "#3E9C8F", // Teal
  "#D6B34A", // Mustard
  "#D67F9E"  // Rose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsContent(
  userName: String,
  isNightMode: Boolean,
  selectedTimezoneId: String?,
  activeGoalTitle: String = "",
  onStartLockInForHabit: ((HabitItem) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val habitPrefs = remember { HabitPreferences(context) }

  var habits by remember { mutableStateOf(habitPrefs.getHabits()) }
  var habitLogs by remember { mutableStateOf(habitPrefs.getHabitLogs()) }

  val today = remember(selectedTimezoneId) {
    try {
      if (selectedTimezoneId.isNullOrEmpty() || selectedTimezoneId == "DEVICE_DEFAULT") {
        LocalDate.now()
      } else {
        LocalDate.now(ZoneId.of(selectedTimezoneId))
      }
    } catch (_: Exception) {
      LocalDate.now()
    }
  }

  var showAddDialog by remember { mutableStateOf(false) }
  var editingHabit by remember { mutableStateOf<HabitItem?>(null) }
  var archivingHabit by remember { mutableStateOf<HabitItem?>(null) }
  var deletingHabit by remember { mutableStateOf<HabitItem?>(null) }
  var selectedHabitDetail by remember { mutableStateOf<HabitItem?>(null) }
  val scope = rememberCoroutineScope()

  fun saveAndRefreshHabits(newList: List<HabitItem>) {
    habitPrefs.saveHabits(newList)
    habits = newList
  }

  fun toggleHabitLog(habitId: String, dateStr: String) {
    val currentDone = habitLogs[dateStr]?.get(habitId) == true
    val nextDone = !currentDone
    habitPrefs.saveHabitLog(dateStr, habitId, nextDone)
    habitLogs = habitPrefs.getHabitLogs()
    
    // If marked as done today, schedule reminder for 18h later
    if (nextDone && dateStr == today.toString()) {
      HabitReminderScheduler.scheduleReminder(context, habitId)
    } else if (!nextDone && dateStr == today.toString()) {
      // If unchecked today, we could potentially reschedule based on previous completion, 
      // but the requirement says "marked completed", so we'll just leave it or cancel.
      // Let's re-schedule anyway to be safe, it will check if it's done.
      HabitReminderScheduler.scheduleReminder(context, habitId)
    }

    // Update Widgets
    scope.launch {
      HabitWidget().updateAll(context)
    }
  }

  val activeHabits = habits.filter { !it.isArchived }

  val pageBgGradient = if (isNightMode) {
    Brush.verticalGradient(listOf(DarkBackground, DarkBackground))
  } else {
    Brush.verticalGradient(listOf(Color(0xFFE8ECF3), Color(0xFFFFFFFF)))
  }

  val textColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val mutedTextColor = Color(0xFF8E96A3)

  val sharedBlockDates = remember(activeHabits, today) {
    if (activeHabits.isEmpty()) {
      // If no habits, show 5 days ending today
      (4 downTo 0).map { today.minusDays(it.toLong()) }
    } else {
      // Find the earliest start date among all habits to anchor the shared blocks
      val earliestStartStr = activeHabits.minOf { it.startDateStr }
      val startDate = try { LocalDate.parse(earliestStartStr) } catch (_: Exception) { today }
      
      val daysSinceStart = ChronoUnit.DAYS.between(startDate, today).toInt()
      // Block size is 5. Calculate which 5-day block today falls into.
      // daysSinceStart: 0..4 -> block 0, 5..9 -> block 1
      val blockIndex = if (daysSinceStart >= 0) daysSinceStart / 5 else (daysSinceStart - 4) / 5
      val blockStartOffset = blockIndex * 5
      val blockStartDate = startDate.plusDays(blockStartOffset.toLong())
      (0 until 5).map { blockStartDate.plusDays(it.toLong()) }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    if (selectedHabitDetail != null) {
      HabitDetailContent(
        habit = selectedHabitDetail!!,
        habitLogs = habitLogs,
        isNightMode = isNightMode,
        onBack = { selectedHabitDetail = null },
        onEdit = { editingHabit = selectedHabitDetail },
        onRemove = {
          deletingHabit = selectedHabitDetail
        },
        onToggleDate = { dateStr -> toggleHabitLog(selectedHabitDetail!!.id, dateStr) },
        onUpdateHabit = { updated ->
          saveAndRefreshHabits(habits.map { if (it.id == updated.id) updated else it })
          selectedHabitDetail = updated
          if (updated.remindersEnabled) {
            HabitReminderScheduler.scheduleReminder(context, updated.id)
          } else {
            HabitReminderScheduler.cancelReminder(context, updated.id)
          }
        }
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(pageBgGradient)
          .verticalScroll(rememberScrollState())
          .statusBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 180.dp)
      ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 1. HEADER
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Habits",
              style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.02).em
              ),
              color = textColor
            )
            Text(
              text = "Keep track of your daily rituals.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
              ),
              color = mutedTextColor
            )
          }

          Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
              .size(44.dp)
              .clickable { showAddDialog = true }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF1A1A1F),
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (activeHabits.isNotEmpty()) {
          // Shared Date Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
          ) {
            Row(
              modifier = Modifier.width(160.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              sharedBlockDates.forEach { date ->
                val isToday = date == today
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.width(32.dp)
                ) {
                  Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = if (isToday) textColor else mutedTextColor
                  )
                  Text(
                    text = date.format(java.time.format.DateTimeFormatter.ofPattern("E")).take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.05.em
                    ),
                    color = if (isToday) textColor else mutedTextColor
                  )
                }
              }
            }
          }
        }

        // HABIT ROWS
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp) 
        ) {
          if (activeHabits.isEmpty()) {
            EmptyState(isNightMode) { showAddDialog = true }
          } else {
            activeHabits.forEach { habit ->
              HabitRow(
                habit = habit,
                today = today,
                blockDates = sharedBlockDates,
                habitLogs = habitLogs,
                onToggleDate = { dateStr -> toggleHabitLog(habit.id, dateStr) },
                onEditHabit = { editingHabit = it },
                onViewDetail = { selectedHabitDetail = it },
                isNightMode = isNightMode
              )
            }
          }
        }
      }
    }
  }

  // DIALOGS
  if (showAddDialog) {
    HabitFormDialog(
      title = "Add Habit",
      isNightMode = isNightMode,
      existingNames = habits.map { it.name },
      onDismiss = { showAddDialog = false },
      onSave = { name, colorHex ->
        val newHabit = HabitItem(
          id = "${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(6)}",
          name = name,
          colorHex = colorHex,
          startDateStr = today.toString()
        )
        saveAndRefreshHabits(habits + newHabit)
        HabitReminderScheduler.scheduleReminder(context, newHabit.id)
        showAddDialog = false
      }
    )
  }

  editingHabit?.let { habit ->
    HabitFormDialog(
      title = "Edit Habit",
      habit = habit,
      isNightMode = isNightMode,
      existingNames = habits.filter { it.id != habit.id }.map { it.name },
      onDismiss = { editingHabit = null },
      onSave = { name, colorHex ->
        val updated = habit.copy(name = name, colorHex = colorHex)
        saveAndRefreshHabits(habits.map { if (it.id == habit.id) updated else it })
        if (selectedHabitDetail?.id == habit.id) {
          selectedHabitDetail = updated
        }
        editingHabit = null
      },
      onArchiveRequest = { archivingHabit = habit; editingHabit = null },
      onDeleteRequest = { deletingHabit = habit; editingHabit = null }
    )
  }

  archivingHabit?.let { habit ->
    ConfirmationPopup(
      title = "Archive Habit?",
      message = "Archived habits are hidden from the main list.",
      confirmText = "Archive",
      icon = Icons.Outlined.Archive,
      isNightMode = isNightMode,
      onDismiss = { archivingHabit = null },
      onConfirm = {
        val updated = habit.copy(isArchived = true)
        saveAndRefreshHabits(habits.map { if (it.id == habit.id) updated else it })
        if (selectedHabitDetail?.id == habit.id) {
          selectedHabitDetail = null
        }
        archivingHabit = null
      }
    )
  }

  deletingHabit?.let { habit ->
    ConfirmationPopup(
      title = "Delete Habit?",
      message = "Are you sure you want to delete '${habit.name}'?",
      confirmText = "Delete",
      icon = Icons.Outlined.Delete,
      isNightMode = isNightMode,
      onDismiss = { deletingHabit = null },
      onConfirm = {
        saveAndRefreshHabits(habits.filter { it.id != habit.id })
        HabitReminderScheduler.cancelReminder(context, habit.id)
        if (selectedHabitDetail?.id == habit.id) {
          selectedHabitDetail = null
        }
        deletingHabit = null
      }
    )
  }
}

@Composable
private fun HabitRow(
  habit: HabitItem,
  today: LocalDate,
  blockDates: List<LocalDate>,
  habitLogs: Map<String, Map<String, Boolean>>,
  onToggleDate: (String) -> Unit,
  onEditHabit: (HabitItem) -> Unit,
  onViewDetail: (HabitItem) -> Unit,
  isNightMode: Boolean
) {
  val startDate = try { LocalDate.parse(habit.startDateStr) } catch (_: Exception) { today }
  
  // Calculate percentage
  val blockEndDate = blockDates.last()
  val totalDays = (ChronoUnit.DAYS.between(startDate, blockEndDate).toInt() + 1).coerceAtLeast(0)
  
  var doneCount = 0
  habitLogs.forEach { (dateStr, map) ->
    val logDate = try { LocalDate.parse(dateStr) } catch (_: Exception) { null }
    if (logDate != null && !logDate.isBefore(startDate) && map[habit.id] == true) {
      doneCount++
    }
  }
  val percentage = if (totalDays > 0) (doneCount.toFloat() / totalDays * 100).roundToInt() else 0

  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  val textColor = if (isNightMode) Color.White else Color(0xFF1C1E23)
  val mutedTextColor = Color(0xFF8E96A3)
  val badgeBg = if (isNightMode) Color(0xFF2D323B) else Color(0xFFE8ECF3)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .height(76.dp)
      .clickable { onEditHabit(habit) },
    shape = RoundedCornerShape(20.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Line: Badge and Dots
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Percentage Badge
        Surface(
          shape = RoundedCornerShape(100.dp),
          color = badgeBg,
          modifier = Modifier.heightIn(min = 28.dp)
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$percentage%",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              color = textColor
            )
          }
        }

        // 5 Dots
        val context = LocalContext.current
        Row(
          modifier = Modifier.width(160.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          blockDates.forEach { date ->
            val dateStr = date.toString()
            val isDone = habitLogs[dateStr]?.get(habit.id) == true
            val isToday = date == today
            val isBeforeStart = date.isBefore(startDate)

            Box(
              modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                  alpha = if (isToday) 1.0f else 0.6f
                }
                .clickable(
                  indication = if (isToday) LocalIndication.current else null,
                  interactionSource = remember { MutableInteractionSource() }
                ) {
                  if (isToday) {
                    onToggleDate(dateStr)
                  } else {
                    Toast.makeText(context, "You can only update today's habit", Toast.LENGTH_SHORT).show()
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              if (isDone) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Done",
                  tint = textColor,
                  modifier = Modifier.size(16.dp)
                )
              } else if (isBeforeStart) {
                // Locked Dash for days before habit creation
                Text(
                  text = "–",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                  ),
                  color = textColor.copy(alpha = 0.2f)
                )
              } else {
                // Not done dot
                Box(
                  modifier = Modifier
                    .size(13.dp)
                    .border(
                      width = if (isToday) 1.5.dp else 1.dp,
                      color = if (isToday) textColor else textColor.copy(alpha = 0.4f),
                      shape = CircleShape
                    )
                )
              }
            }
          }
        }
      }

      // Second Line: Habit Name and 3-Dots
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = habit.name,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
          ),
          color = textColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
        
        Box(
          modifier = Modifier
            .size(40.dp)
            .clickable { onViewDetail(habit) },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
            contentDescription = "Details",
            tint = mutedTextColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

private fun totalDaysElapsedSinceCreated(habit: HabitItem, today: LocalDate): Int {
  val start = try { LocalDate.parse(habit.startDateStr) } catch (_: Exception) { today }
  return (ChronoUnit.DAYS.between(start, today).toInt() + 1).coerceAtLeast(0)
}

@Composable
private fun EmptyState(
  isNightMode: Boolean,
  onAdd: () -> Unit
) {
  val surfaceColor = if (isNightMode) DarkCardSurface else Color.White
  val borderColor = if (isNightMode) DarkSubtleBorder else Color(0xFFE3E9F0)
  
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = surfaceColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 0.dp
  ) {
    Column(
      modifier = Modifier.padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "No habits yet",
        style = MaterialTheme.typography.bodyLarge,
        color = Color(0xFF8E96A3)
      )
      Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFF1A1A1F),
        modifier = Modifier.clickable { onAdd() }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add habit",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun HabitFormDialog(
  title: String,
  habit: HabitItem? = null,
  isNightMode: Boolean,
  existingNames: List<String>,
  onDismiss: () -> Unit,
  onSave: (String, String) -> Unit,
  onArchiveRequest: (() -> Unit)? = null,
  onDeleteRequest: (() -> Unit)? = null
) {
  var nameInput by remember { mutableStateOf(habit?.name ?: "") }
  var selectedColorHex by remember { mutableStateOf(habit?.colorHex ?: "#8B7FD6") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isValid = nameInput.trim().isNotEmpty()
  val surfaceBg = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val secondaryText = if (isNightMode) Color(0xFF8E96A3) else Color(0xFF8E96A3)
  val fieldBg = if (isNightMode) Color(0xFF252731) else Color(0xFFEEF1F6)
  val fieldBorderFocused = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val fieldBorderUnfocused = if (isNightMode) Color(0xFF3A3C46) else Color(0xFFD5D9E0)

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
          text = if (habit == null) "Give your new habit a name." else "Update your habit details below.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.W400,
            fontSize = 15.sp
          ),
          color = secondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
          value = nameInput,
          onValueChange = {
            if (it.length <= 30) {
              nameInput = it
              errorMessage = null
            }
          },
          label = { Text("Habit name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = textColor,
            focusedBorderColor = fieldBorderFocused,
            unfocusedBorderColor = fieldBorderUnfocused,
            focusedLabelColor = fieldBorderFocused,
            unfocusedLabelColor = secondaryText,
            focusedPlaceholderColor = Color(0xFFA0AEC0),
            unfocusedPlaceholderColor = Color(0xFFA0AEC0)
          ),
          textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
          )
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = errorMessage!!,
            color = Color(0xFFF0716B),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (habit != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = { onArchiveRequest?.invoke() },
              modifier = Modifier
                .weight(1f)
                .height(44.dp),
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(
                containerColor = fieldBg,
                contentColor = secondaryText
              )
            ) {
              Text("Archive", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
              onClick = { onDeleteRequest?.invoke() },
              modifier = Modifier
                .weight(1f)
                .height(44.dp),
              shape = CircleShape,
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x1AF0716B),
                contentColor = Color(0xFFF0716B)
              )
            ) {
              Text("Delete", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }

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
              containerColor = fieldBg,
              contentColor = textColor
            )
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
          }

          Button(
            onClick = {
              val trimmed = nameInput.trim()
              if (trimmed.isEmpty()) {
                errorMessage = "Name cannot be empty"
              } else if (existingNames.any { it.equals(trimmed, true) }) {
                errorMessage = "Name already exists"
              } else {
                onSave(trimmed, selectedColorHex)
              }
            },
            enabled = isValid,
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) Color.White else Color(0xFF1C1E23),
              contentColor = if (isNightMode) Color(0xFF1C1E23) else Color.White
            )
          ) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ConfirmationPopup(
  title: String,
  message: String,
  confirmText: String,
  icon: ImageVector,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val surfaceBg = if (isNightMode) DarkCardSurface else Color.White
  val textColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = surfaceBg,
    icon = { Icon(icon, contentDescription = null, tint = textColor) },
    title = { Text(title, fontWeight = FontWeight.Bold, color = textColor) },
    text = { Text(message, color = textColor.copy(alpha = 0.7f), textAlign = TextAlign.Center) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1F))
      ) {
        Text(confirmText, color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

