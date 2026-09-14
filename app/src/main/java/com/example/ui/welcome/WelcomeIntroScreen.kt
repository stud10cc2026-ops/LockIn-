package com.example.ui.welcome

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkTextOffWhite
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureLimeAccent
import com.example.ui.theme.SignatureLimeDarkText

@Composable
fun WelcomeIntroScreen(
  isNightMode: Boolean,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val primaryTextColor = if (isNightMode) DarkTextOffWhite else LightTextPrimary
  val secondaryTextColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 24.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title: Welcome to / Lock In
        Text(
          text = "Welcome to\nLock In",
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp
          ),
          color = primaryTextColor,
          modifier = Modifier.testTag("welcome_intro_title")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle: Take back control of your time.
        Text(
          text = "Take back control of your time.",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 22.sp
          ),
          color = secondaryTextColor,
          modifier = Modifier.testTag("welcome_intro_subtitle")
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Section 1
        IntroFeatureRow(
          icon = Icons.Outlined.Shield,
          title = "Focus without distractions",
          description = "Lock In helps you stay focused by limiting distracting apps while you work.",
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Section 2
        IntroFeatureRow(
          icon = Icons.Outlined.CheckCircleOutline,
          title = "Build better habits",
          description = "Turn your focus time into progress and keep track of your consistency.",
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Section 3
        IntroFeatureRow(
          icon = Icons.Outlined.Tune,
          title = "Your time, your rules",
          description = "Choose when to focus and use the tools you need to stay on track.",
          primaryTextColor = primaryTextColor,
          secondaryTextColor = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(32.dp))
      }

      // Bottom Section: Button and footer caption
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          onClick = onContinue,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("welcome_intro_continue_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SignatureLimeAccent,
            contentColor = SignatureLimeDarkText
          ),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
          Text(
            text = "Continue",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Let's make your time count.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp
          ),
          color = secondaryTextColor,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("welcome_intro_footer_text")
        )
      }
    }
  }
}

@Composable
private fun IntroFeatureRow(
  icon: ImageVector,
  title: String,
  description: String,
  primaryTextColor: androidx.compose.ui.graphics.Color,
  secondaryTextColor: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = primaryTextColor,
      modifier = Modifier
        .size(28.dp)
        .padding(top = 2.dp)
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          lineHeight = 22.sp
        ),
        color = primaryTextColor
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 14.sp,
          lineHeight = 20.sp
        ),
        color = secondaryTextColor
      )
    }
  }
}
