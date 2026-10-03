package com.example.ui.welcome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RefNearBlack
import com.example.ui.theme.RefHeadlineGray
import com.example.ui.theme.RefPageBackgroundTop
import com.example.ui.theme.RefPageBackgroundBottom

@Composable
fun WelcomeIntroScreen(
  isNightMode: Boolean,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  val backgroundBrush = if (isNightMode) {
    Brush.verticalGradient(listOf(Color(0xFF111214), Color(0xFF111214)))
  } else {
    Brush.verticalGradient(
      colors = listOf(RefPageBackgroundTop, RefPageBackgroundBottom)
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundBrush)
  ) {
    // CONTENT
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .statusBarsPadding()
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(32.dp))

      // HEADLINE
      Text(
        text = "Welcome to\nLock In",
        style = MaterialTheme.typography.displayMedium.copy(
          fontSize = 32.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 38.sp,
          letterSpacing = (-0.5).sp
        ),
        color = RefNearBlack,
        modifier = Modifier.testTag("welcome_intro_title")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Take back control of your time.",
        style = MaterialTheme.typography.bodyLarge.copy(
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium, // weight 500
          lineHeight = 22.sp
        ),
        color = RefHeadlineGray,
        modifier = Modifier.testTag("welcome_intro_subtitle")
      )

      Spacer(modifier = Modifier.height(40.dp))

      // FEATURE CARDS
      IntroFeatureCard(
        icon = Icons.Outlined.Shield,
        title = "Focus without distractions",
        description = "Lock In helps you stay focused by limiting distracting apps while you work."
      )

      Spacer(modifier = Modifier.height(16.dp))

      IntroFeatureCard(
        icon = Icons.Outlined.CheckCircleOutline,
        title = "Build better habits",
        description = "Turn your focus time into progress and keep track of your consistency."
      )

      Spacer(modifier = Modifier.height(16.dp))

      IntroFeatureCard(
        icon = Icons.Outlined.Tune,
        title = "Your time, your rules",
        description = "Choose when to focus and use the tools you need to stay on track."
      )

      // Padding for bottom area and fade
      Spacer(modifier = Modifier.height(180.dp))
    }

    // BOTTOM FADE OVERLAY
    val fadeColor = if (isNightMode) Color(0xFF111214) else RefPageBackgroundBottom
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = 100.dp) // Sitting just above the Continue button area
        .height(170.dp)
        .background(
          brush = Brush.verticalGradient(
            0.0f to fadeColor.copy(alpha = 0f),
            0.55f to fadeColor.copy(alpha = 0.7f),
            1.0f to fadeColor
          )
        )
        .graphicsLayer { alpha = 0.99f } // Ensure it's treated as a layer if needed, but mainly for clarity
    )

    // BOTTOM BUTTON AREA
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(if (isNightMode) Color(0xFF111214) else RefPageBackgroundBottom)
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      val interactionSource = remember { MutableInteractionSource() }
      val isPressed by interactionSource.collectIsPressedAsState()
      val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "button_scale"
      )

      Button(
        onClick = onContinue,
        interactionSource = interactionSource,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
          }
          .testTag("welcome_intro_continue_button"),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = RefNearBlack,
          contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
      ) {
        Text(
          text = "Continue",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold, // weight 600
            fontSize = 17.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "By continuing, you agree to our Terms",
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal, // weight 400
          letterSpacing = 0.sp
        ),
        color = Color(0xFFA9B4C2),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("welcome_intro_footer_text")
      )
    }
  }
}

@Composable
private fun IntroFeatureCard(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(28.dp),
    color = Color.White,
    border = BorderStroke(1.dp, Color(0xFFE3E9F0)),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color(0xFFDCE5EE)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color(0xFF1A1A1F),
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold, // weight 600
            fontSize = 17.sp,
            letterSpacing = 0.sp
          ),
          color = Color(0xFF1A1A1F)
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Normal, // weight 400
            fontSize = 15.sp,
            letterSpacing = 0.sp
          ),
          color = Color(0xFF8E96A3)
        )
      }
    }
  }
}
