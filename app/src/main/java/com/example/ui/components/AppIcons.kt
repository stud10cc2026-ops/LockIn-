package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom vector brand icon container for blocked apps (Instagram, TikTok, YouTube, X, Snapchat, Reddit, Twitch).
 * Draws crisp geometric representations of app logos in a modern minimal outline/solid style.
 */
@Composable
fun AppBrandIcon(
  appName: String,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  backgroundColor: Color = Color(0xFF26282B),
  borderColor: Color = Color(0xFF34373C),
  iconColor: Color = Color.White
) {
  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(12.dp))
      .background(backgroundColor)
      .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.Center
  ) {
    val iconSize = size * 0.54f
    when (appName.lowercase()) {
      "instagram" -> InstagramIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "tiktok" -> TikTokIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "youtube" -> YouTubeIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "whatsapp" -> WhatsAppIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "x", "twitter" -> XIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "snapchat" -> SnapchatIcon(modifier = Modifier.size(iconSize), color = iconColor)
      "reddit" -> RedditIcon(modifier = Modifier.size(iconSize), color = iconColor)
      else -> DefaultAppIcon(modifier = Modifier.size(iconSize), color = iconColor)
    }
  }
}

@Composable
fun InstagramIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    val strokeWidth = w * 0.09f

    // Rounded square body
    drawRoundRect(
      color = color,
      size = Size(w, h),
      cornerRadius = CornerRadius(w * 0.28f, h * 0.28f),
      style = Stroke(width = strokeWidth)
    )
    // Center lens circle
    drawCircle(
      color = color,
      radius = w * 0.24f,
      center = Offset(w / 2, h / 2),
      style = Stroke(width = strokeWidth)
    )
    // Top right flash dot
    drawCircle(
      color = color,
      radius = w * 0.05f,
      center = Offset(w * 0.74f, h * 0.26f)
    )
  }
}

@Composable
fun TikTokIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    val stroke = w * 0.12f

    val path = Path().apply {
      moveTo(w * 0.35f, h * 0.2f)
      lineTo(w * 0.35f, h * 0.65f)
      cubicTo(w * 0.35f, h * 0.8f, w * 0.22f, h * 0.9f, w * 0.1f, h * 0.8f)
      cubicTo(w * 0.0f, h * 0.72f, w * 0.05f, h * 0.52f, w * 0.22f, h * 0.52f)
      lineTo(w * 0.35f, h * 0.52f)
      moveTo(w * 0.35f, h * 0.2f)
      lineTo(w * 0.55f, h * 0.2f)
      cubicTo(w * 0.55f, h * 0.35f, w * 0.75f, h * 0.45f, w * 0.95f, h * 0.45f)
      lineTo(w * 0.95f, h * 0.65f)
      cubicTo(w * 0.78f, h * 0.65f, w * 0.62f, h * 0.55f, w * 0.55f, h * 0.45f)
      lineTo(w * 0.55f, h * 0.58f)
    }

    drawPath(
      path = path,
      color = color,
      style = Stroke(width = stroke)
    )
  }
}

@Composable
fun YouTubeIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height

    // Play rectangle background container
    drawRoundRect(
      color = color,
      size = Size(w, h * 0.72f),
      topLeft = Offset(0f, h * 0.14f),
      cornerRadius = CornerRadius(w * 0.2f, h * 0.2f)
    )

    // Center play triangle
    val playPath = Path().apply {
      moveTo(w * 0.42f, h * 0.34f)
      lineTo(w * 0.66f, h * 0.5f)
      lineTo(w * 0.42f, h * 0.66f)
      close()
    }
    drawPath(path = playPath, color = Color(0xFF1E1E1E))
  }
}

@Composable
fun WhatsAppIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    val stroke = w * 0.09f

    // Speech bubble outline
    drawCircle(
      color = color,
      radius = w * 0.42f,
      center = Offset(w * 0.5f, h * 0.44f),
      style = Stroke(width = stroke)
    )
    val tailPath = Path().apply {
      moveTo(w * 0.2f, h * 0.68f)
      lineTo(w * 0.1f, h * 0.9f)
      lineTo(w * 0.36f, h * 0.82f)
    }
    drawPath(path = tailPath, color = color, style = Stroke(width = stroke))
  }
}

@Composable
fun XIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    val stroke = w * 0.1f

    drawLine(
      color = color,
      start = Offset(w * 0.15f, h * 0.15f),
      end = Offset(w * 0.85f, h * 0.85f),
      strokeWidth = stroke
    )
    drawLine(
      color = color,
      start = Offset(w * 0.85f, h * 0.15f),
      end = Offset(w * 0.15f, h * 0.85f),
      strokeWidth = stroke
    )
  }
}

@Composable
fun SnapchatIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    val stroke = w * 0.08f

    val ghostPath = Path().apply {
      moveTo(w * 0.5f, h * 0.15f)
      cubicTo(w * 0.3f, h * 0.15f, w * 0.22f, h * 0.32f, w * 0.22f, h * 0.5f)
      cubicTo(w * 0.1f, h * 0.55f, w * 0.05f, h * 0.65f, w * 0.25f, h * 0.68f)
      cubicTo(w * 0.2f, h * 0.82f, w * 0.38f, h * 0.85f, w * 0.5f, h * 0.85f)
      cubicTo(w * 0.62f, h * 0.85f, w * 0.8f, h * 0.82f, w * 0.75f, h * 0.68f)
      cubicTo(w * 0.95f, h * 0.65f, w * 0.9f, h * 0.55f, w * 0.78f, h * 0.5f)
      cubicTo(w * 0.78f, h * 0.32f, w * 0.7f, h * 0.15f, w * 0.5f, h * 0.15f)
      close()
    }
    drawPath(path = ghostPath, color = color, style = Stroke(width = stroke))
  }
}

@Composable
fun RedditIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height

    drawCircle(
      color = color,
      radius = w * 0.42f,
      center = Offset(w / 2, h / 2),
      style = Stroke(width = w * 0.08f)
    )
    drawCircle(color = color, radius = w * 0.07f, center = Offset(w * 0.38f, h * 0.48f))
    drawCircle(color = color, radius = w * 0.07f, center = Offset(w * 0.62f, h * 0.48f))
  }
}

@Composable
fun DefaultAppIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = this.size.width
    val h = this.size.height
    drawRoundRect(
      color = color,
      size = Size(w, h),
      cornerRadius = CornerRadius(w * 0.25f, h * 0.25f),
      style = Stroke(width = w * 0.09f)
    )
  }
}

/**
 * Minimalist Pushup Exercise Symbol Icon for Early Exit Card.
 * Symbol clearly communicates: PERSON + HANDS ON FLOOR + BODY IN PUSH-UP POSITION
 */
@Composable
fun PushupExerciseIcon(
  modifier: Modifier = Modifier,
  color: Color = Color(0xFF111111)
) {
  Canvas(modifier = modifier.size(26.dp)) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.11f

    // 1. Head (Solid circle at far right)
    drawCircle(
      color = color,
      radius = w * 0.085f,
      center = Offset(w * 0.90f, h * 0.49f)
    )

    // 2. Legs & Torso (Foot -> Knee -> Hips -> Shoulder)
    val bodyPath = Path().apply {
      moveTo(w * 0.08f, h * 0.58f) // Foot on far left
      lineTo(w * 0.28f, h * 0.58f) // Knee / lower leg
      lineTo(w * 0.48f, h * 0.50f) // Hips / waist
      lineTo(w * 0.78f, h * 0.48f) // Chest / Shoulders
    }
    drawPath(
      path = bodyPath,
      color = color,
      style = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )

    // 3. Bent Arm (Shoulder -> High Elbow -> Hand supporting on floor)
    val armPath = Path().apply {
      moveTo(w * 0.76f, h * 0.48f) // Shoulder
      lineTo(w * 0.57f, h * 0.34f) // Elbow bent high back
      lineTo(w * 0.61f, h * 0.64f) // Forearm & Hand to floor
    }
    drawPath(
      path = armPath,
      color = color,
      style = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )
  }
}
