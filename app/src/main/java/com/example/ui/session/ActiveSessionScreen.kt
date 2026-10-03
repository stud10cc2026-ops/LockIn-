package com.example.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.SignatureNeonLime
import java.util.Locale

/**
 * Active Focus Session Screen:
 * Clean, minimalist dark/light focus timer screen with three-dot theme menu.
 */
@Composable
fun ActiveSessionScreen(
  durationMinutes: Int,
  remainingSeconds: Int,
  totalSeconds: Int,
  isPaused: Boolean = false,
  blockedAppName: String? = null,
  isNightMode: Boolean = true,
  themeMode: String = "SYSTEM",
  onThemeModeSelected: ((String) -> Unit)? = null,
  onNightModeToggle: ((Boolean) -> Unit)? = null,
  onClearBlockedAppAlert: () -> Unit = {},
  onPauseResumeClick: () -> Unit = {},
  onEndSessionClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val formattedTime = remember(remainingSeconds) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
  }

  val screenAlpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    screenAlpha.animateTo(1f, animationSpec = tween(800))
  }

  var showMenu by remember { mutableStateOf(false) }

  val backgroundColor = if (isNightMode) Color(0xFF15171F) else Color(0xFFF8FAFC)
  val titleTextColor = if (isNightMode) Color(0xFFF1F4F8) else Color(0xFF1E293B)
  val timerTextColor = if (isNightMode) Color.White else Color(0xFF0F172A)
  val toggleIconColor = if (isNightMode) Color(0xFFF1F4F8) else Color(0xFF334155)

  Box(
    modifier = modifier
      .fillMaxSize()
      .graphicsLayer { alpha = screenAlpha.value }
      .background(backgroundColor)
  ) {
    // Main Layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // TOP HEADER WITH TITLE AND THREE-DOT THEME MENU
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Focus Session",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
            letterSpacing = 0.5.sp
          ),
          color = titleTextColor,
          modifier = Modifier.align(Alignment.Center)
        )

        Box(
          modifier = Modifier.align(Alignment.CenterEnd)
        ) {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
              .size(36.dp)
              .testTag("theme_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Theme options",
              tint = toggleIconColor,
              modifier = Modifier.size(20.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier.width(156.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
          ) {
            Column(
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              ThemeMenuItem(
                label = "Light Mode",
                isSelected = themeMode == "LIGHT",
                onClick = {
                  showMenu = false
                  onThemeModeSelected?.invoke("LIGHT")
                  onNightModeToggle?.invoke(false)
                }
              )
              ThemeMenuItem(
                label = "Dark Mode",
                isSelected = themeMode == "DARK",
                onClick = {
                  showMenu = false
                  onThemeModeSelected?.invoke("DARK")
                  onNightModeToggle?.invoke(true)
                }
              )
              ThemeMenuItem(
                label = "System Default",
                isSelected = themeMode == "SYSTEM",
                onClick = {
                  showMenu = false
                  onThemeModeSelected?.invoke("SYSTEM")
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // TIMER (Large & Bold Text)
      Text(
        text = formattedTime,
        style = TextStyle(
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.W700,
          fontSize = 110.sp,
          color = timerTextColor,
          textAlign = TextAlign.Center,
          letterSpacing = (-2).sp
        ),
        modifier = Modifier.testTag("session_timer_text")
      )

      Spacer(modifier = Modifier.weight(1f))

      // CONTROLS
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 60.dp)
      ) {
        // Pause/Play Button
        PrimaryControlSquareButton(
          onClick = { onPauseResumeClick() },
          isPaused = isPaused,
          isNightMode = isNightMode,
          modifier = Modifier.testTag("pause_resume_button")
        )
      }
    }

    // Blocked App Overlay
    BlockedAppOverlay(
      blockedAppName = blockedAppName,
      formattedTime = formattedTime,
      onClearBlockedAppAlert = onClearBlockedAppAlert
    )
  }
}

@Composable
private fun ThemeMenuItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val backgroundColor = if (isSelected) Color(0xFFF1F5F9) else Color.Transparent

  DropdownMenuItem(
    text = {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 13.5.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        ),
        color = Color(0xFF1E293B)
      )
    },
    trailingIcon = if (isSelected) {
      {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selected",
          tint = Color(0xFF0F172A),
          modifier = Modifier.size(15.dp)
        )
      }
    } else null,
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .padding(horizontal = 4.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor),
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
    colors = MenuDefaults.itemColors(
      textColor = Color(0xFF1E293B),
      trailingIconColor = Color(0xFF0F172A)
    )
  )
}

@Composable
private fun PrimaryControlSquareButton(
  onClick: () -> Unit,
  isPaused: Boolean,
  isNightMode: Boolean = true,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    label = "ControlScale"
  )

  val buttonBg = if (isNightMode) Color(0xFFDCE5EE) else Color(0xFF1E293B)
  val iconTint = if (isNightMode) Color(0xFF1A1A1F) else Color(0xFFF8FAFC)

  Box(
    modifier = modifier
      .size(80.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clip(RoundedCornerShape(28.dp))
      .background(buttonBg)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
      contentDescription = if (isPaused) "Resume" else "Pause",
      tint = iconTint,
      modifier = Modifier.size(32.dp)
    )
  }
}

@Composable
private fun BlockedAppOverlay(
  blockedAppName: String?,
  formattedTime: String,
  onClearBlockedAppAlert: () -> Unit
) {
  AnimatedVisibility(
    visible = blockedAppName != null,
    enter = fadeIn(animationSpec = tween(200)),
    exit = fadeOut(animationSpec = tween(150))
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0x99000000)) // 60% opacity black
        .statusBarsPadding(),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
          horizontalAlignment = Alignment.Start
        ) {
          // Drag handle
          Box(
            modifier = Modifier
              .width(32.dp)
              .height(4.dp)
              .clip(CircleShape)
              .background(Color(0xFFE2E8F0))
              .align(Alignment.CenterHorizontally)
          )

          Spacer(modifier = Modifier.height(22.dp))

          Text(
            text = "${blockedAppName ?: "App"} is blocked.",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              letterSpacing = (-0.4).sp
            ),
            color = Color(0xFF0F172A)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Stay focused until the timer ends.",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 14.sp,
              lineHeight = 21.sp,
              fontWeight = FontWeight.Normal
            ),
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(20.dp))

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF1F5F9))
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = formattedTime,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              ),
              color = Color(0xFF0F172A),
              modifier = Modifier.testTag("blocked_app_timer_text")
            )
            Text(
              text = "remaining in focus session",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp
              ),
              color = Color(0xFF64748B)
            )
          }

          Spacer(modifier = Modifier.height(26.dp))

          Button(
            onClick = { onClearBlockedAppAlert() },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("blocked_app_return_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF0B0F19),
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
          ) {
            Text(
              text = "Go Back to Lock In",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              ),
              color = Color.White
            )
          }
        }
      }
    }
  }
}


