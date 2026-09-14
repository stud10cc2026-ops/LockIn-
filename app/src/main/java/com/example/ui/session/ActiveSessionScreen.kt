package com.example.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
 * Clean, distraction-free focus screen with timer display, locked state indicator,
 * pause/resume controls, and early exit challenge prompt.
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
  onEndSessionClick: () -> Unit,
  onPushUpChallengeCompleted: () -> Unit = onEndSessionClick,
  modifier: Modifier = Modifier
) {
  var showExitConfirmation by remember { mutableStateOf(false) }
  var showPushUpChallenge by remember { mutableStateOf(false) }

  val formattedTime = remember(remainingSeconds) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
  }

  val progress = remember(remainingSeconds, totalSeconds) {
    if (totalSeconds > 0) {
      remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF111214))
  ) {
    // Main Layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Bar with Exit Button & Lock Status
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp)
      ) {
        IconButton(
          onClick = { showExitConfirmation = true },
          modifier = Modifier
            .size(40.dp)
            .align(Alignment.CenterStart)
            .testTag("session_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Leave session",
            tint = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.size(20.dp)
          )
        }

        // Lock status on the right
        Row(
          modifier = Modifier.align(Alignment.CenterEnd),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = if (isPaused) Color(0xFFE5A93C) else SignatureNeonLime,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = if (isPaused) "PAUSED" else "APPS BLOCKED",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              letterSpacing = 1.2.sp
            ),
            color = Color.White.copy(alpha = 0.5f)
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Main Center Focus Content
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Main Heading
        Text(
          text = if (isPaused) "SESSION PAUSED" else "LOCKED IN",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = 2.sp
          ),
          color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Remaining Time Display
        Text(
          text = formattedTime,
          style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 68.sp,
            letterSpacing = (-1.5).sp
          ),
          color = if (isPaused) Color.White.copy(alpha = 0.5f) else Color.White,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("session_timer_text")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Simple Subtitle Status
        Text(
          text = if (isPaused) "Take a breath and resume when ready." else "Stay focused.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          ),
          color = Color.White.copy(alpha = 0.55f),
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.weight(1.2f))

      // Bottom Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Pause / Resume Button
        Button(
          onClick = { onPauseResumeClick() },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("pause_resume_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SignatureNeonLime,
            contentColor = DarkButtonCharcoal
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume focus session" else "Pause focus session",
              tint = DarkButtonCharcoal,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = if (isPaused) "Resume Session" else "Pause Session",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = DarkButtonCharcoal
            )
          }
        }

        // Early Exit Button
        Button(
          onClick = { showExitConfirmation = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("end_session_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1C1C1F),
            contentColor = Color.White.copy(alpha = 0.7f)
          ),
          border = BorderStroke(1.dp, Color(0xFF28282C)),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Text(
            text = "Early Exit",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            ),
            color = Color.White.copy(alpha = 0.7f)
          )
        }
      }
    }

    // Exit Confirmation Dialog
    AnimatedVisibility(
      visible = showExitConfirmation,
      enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.96f, animationSpec = tween(200)),
      exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.96f, animationSpec = tween(150))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.65f))
          .clickable { showExitConfirmation = false },
        contentAlignment = Alignment.Center
      ) {
        Surface(
          modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clickable(enabled = false) {},
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF1E2024),
          border = BorderStroke(1.dp, Color(0xFF2C2F34))
        ) {
          Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2D32)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = SignatureNeonLime,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "End Focus Session?",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              ),
              color = Color.White,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Complete 15 push-ups to earn your early exit, or end the session now.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp
              ),
              color = Color.White.copy(alpha = 0.65f),
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Push-Up Challenge Option directly inside Early Exit popup
            Button(
              onClick = {
                showExitConfirmation = false
                showPushUpChallenge = true
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("take_pushup_challenge_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureNeonLime,
                contentColor = DarkButtonCharcoal
              )
            ) {
              Text(
                text = "Take 15 Push-Ups Challenge",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
              onClick = { showExitConfirmation = false },
              modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .testTag("keep_focusing_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2C2F34),
                contentColor = Color.White
              )
            ) {
              Text(
                text = "Keep Focusing",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
              )
            }
          }
        }
      }
    }

    // Push-Up Early Exit Challenge View
    AnimatedVisibility(
      visible = showPushUpChallenge,
      enter = fadeIn(animationSpec = tween(200)),
      exit = fadeOut(animationSpec = tween(150))
    ) {
      PushUpChallengeScreen(
        onDismiss = { showPushUpChallenge = false },
        onSkipExit = {
          showPushUpChallenge = false
          onPushUpChallengeCompleted()
        }
      )
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
              text = "To leave your focus session early, complete 15 push-ups or stay focused until the timer ends.",
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

            // Primary action: [ Do 15 Push-Ups ]
            Button(
              onClick = {
                onClearBlockedAppAlert()
                showPushUpChallenge = true
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("blocked_app_pushup_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureNeonLime,
                contentColor = DarkButtonCharcoal
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Text(
                text = "Do 15 Push-Ups",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.5.sp
                ),
                color = DarkButtonCharcoal
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary option: [ Go Back to Lock In ]
            Button(
              onClick = { onClearBlockedAppAlert() },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("blocked_app_return_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF0F172A)
              ),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
              Text(
                text = "Go Back to Lock In",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp
                ),
                color = Color(0xFF0F172A)
              )
            }
          }
        }
      }
    }
  }
}
