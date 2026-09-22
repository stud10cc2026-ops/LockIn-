package com.example.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.SignatureNeonLime
import java.util.Locale

/**
 * Active Focus Session Screen:
 * Minimalist dark focus screen with top navigation header, large center timer,
 * and bottom squircle pause button.
 */
@Composable
fun ActiveSessionScreen(
  durationMinutes: Int,
  remainingSeconds: Int,
  totalSeconds: Int,
  isPaused: Boolean = false,
  blockedAppName: String? = null,
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

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF000000))
  ) {
    // Main Layout - Vertical Column with SpaceBetween
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Navigation Bar (Header)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Focus Session",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
          ),
          color = Color(0xFFFFFFFF)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // 2. Timer Text (Placed directly below top header)
      Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 96.sp,
          letterSpacing = (-1.5).sp
        ),
        color = if (isPaused) Color(0xFFFFFFFF).copy(alpha = 0.5f) else Color(0xFFFFFFFF),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("session_timer_text")
      )

      // Flexible space pushing Pause Button to the bottom
      Spacer(modifier = Modifier.weight(1f))

      // 3. Bottom Section (Pause Button)
      Box(
        modifier = Modifier
          .padding(bottom = 48.dp),
        contentAlignment = Alignment.Center
      ) {
        IconButton(
          onClick = { onPauseResumeClick() },
          modifier = Modifier
            .size(width = 72.dp, height = 72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFC0E838))
            .testTag("pause_resume_button")
        ) {
          Icon(
            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = if (isPaused) "Resume session" else "Pause session",
            tint = Color(0xFF000000),
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }

    // Blocked App Overlay Screen — Clean white bottom-sheet popup over dimmed background
    AnimatedVisibility(
      visible = blockedAppName != null,
      enter = fadeIn(animationSpec = tween(200)),
      exit = fadeOut(animationSpec = tween(150))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.65f))
          .statusBarsPadding(),
        contentAlignment = Alignment.BottomCenter
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
          shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
          color = Color.White
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start
          ) {
            // Drag handle pill bar
            Box(
              modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0))
                .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Heading: "YouTube is blocked."
            Text(
              text = "${blockedAppName ?: "App"} is blocked.",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                letterSpacing = (-0.4).sp
              ),
              color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Supporting text
            Text(
              text = "Stay focused until the timer ends.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal
              ),
              color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Timer indicator pill
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Color(0xFF475569),
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

            // Primary action: [ Go Back to Lock In ]
            Button(
              onClick = { onClearBlockedAppAlert() },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("blocked_app_return_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureNeonLime,
                contentColor = DarkButtonCharcoal
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Text(
                text = "Go Back to Lock In",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.5.sp
                ),
                color = DarkButtonCharcoal
              )
            }
          }
        }
      }
    }
  }
}

