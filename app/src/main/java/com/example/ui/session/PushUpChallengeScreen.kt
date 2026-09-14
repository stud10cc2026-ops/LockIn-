package com.example.ui.session

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraFront
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.SignatureNeonLime
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executors

private data class PoseKeypoint(
  val x: Float,
  val y: Float,
  val likelihood: Float
)

private data class PoseOverlayData(
  val landmarksMap: Map<Int, PoseKeypoint>,
  val imageWidth: Int,
  val imageHeight: Int,
  val rotationDegrees: Int,
  val isFrontCamera: Boolean,
  val pushUpState: String = "UP",
  val elbowAngle: Double? = null,
  val isBodyValid: Boolean = false,
  val guidanceMessage: String = "Move back so your upper body is visible"
)

private data class PushUpProcessResult(
  val reps: Int,
  val stateStr: String,
  val isBodyValid: Boolean,
  val guidanceMessage: String
)

private fun dist(p1: PoseKeypoint, p2: PoseKeypoint): Float {
  val dx = p1.x - p2.x
  val dy = p1.y - p2.y
  return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun calculateAngle(
  first: PoseKeypoint,
  mid: PoseKeypoint,
  last: PoseKeypoint
): Double {
  var result = Math.toDegrees(
    (kotlin.math.atan2(last.y - mid.y, last.x - mid.x) -
     kotlin.math.atan2(first.y - mid.y, first.x - mid.x)).toDouble()
  )
  result = Math.abs(result)
  if (result > 180.0) {
    result = 360.0 - result
  }
  return result
}

private fun calculateElbowAngle(
  first: PoseKeypoint,
  mid: PoseKeypoint,
  last: PoseKeypoint
): Double = calculateAngle(first, mid, last)

private enum class PushUpPhase {
  WAITING_FOR_TOP,
  TOP,
  DESCENDING,
  BOTTOM,
  ASCENDING
}

private class KeypointSmoother(private val alpha: Float = 0.4f) {
  private val smoothedMap = mutableMapOf<Int, PoseKeypoint>()

  fun smooth(rawKeypoints: Map<Int, PoseKeypoint>): Map<Int, PoseKeypoint> {
    val result = mutableMapOf<Int, PoseKeypoint>()
    for ((type, raw) in rawKeypoints) {
      val prev = smoothedMap[type]
      if (prev == null) {
        smoothedMap[type] = raw
        result[type] = raw
      } else {
        val newX = alpha * raw.x + (1f - alpha) * prev.x
        val newY = alpha * raw.y + (1f - alpha) * prev.y
        val smoothed = PoseKeypoint(newX, newY, raw.likelihood)
        smoothedMap[type] = smoothed
        result[type] = smoothed
      }
    }
    smoothedMap.keys.retainAll(rawKeypoints.keys)
    return result
  }

  fun reset() {
    smoothedMap.clear()
  }
}

private class PushUpTracker {
  private var phase = PushUpPhase.WAITING_FOR_TOP
  private val smoother = KeypointSmoother(alpha = 0.4f)

  private var topFrameCount = 0
  private var descendingFrameCount = 0
  private var bottomFrameCount = 0
  private var ascendingFrameCount = 0
  private var missingPoseFrames = 0
  private var invalidPostureFrames = 0
  private var currentRepWasPostureValid = true

  private var totalReps = 0

  private var descentStartTimeMs = 0L
  private var lastRepTimeMs = 0L
  private var lastDebugLogTimeMs = 0L

  var lastSmoothedAngle: Double? = null
    private set

  private var smoothedAngleEMA: Double? = null
  private var baselineTopAngle: Double = 155.0
  private val minConfidence = 0.35f

  fun processPose(rawKeypoints: Map<Int, PoseKeypoint>): PushUpProcessResult {
    // 1. Temporal Smoothing on landmarks (EMA filter)
    val keypoints = smoother.smooth(rawKeypoints)

    val lShoulder = keypoints[PoseLandmark.LEFT_SHOULDER]
    val rShoulder = keypoints[PoseLandmark.RIGHT_SHOULDER]
    val lElbow = keypoints[PoseLandmark.LEFT_ELBOW]
    val rElbow = keypoints[PoseLandmark.RIGHT_ELBOW]
    val lWrist = keypoints[PoseLandmark.LEFT_WRIST]
    val rWrist = keypoints[PoseLandmark.RIGHT_WRIST]

    val lHip = keypoints[PoseLandmark.LEFT_HIP]
    val rHip = keypoints[PoseLandmark.RIGHT_HIP]

    // 2. Validate upper-body landmarks presence (knees/ankles NOT required!)
    val hasLeftArm = lShoulder != null && lElbow != null && lWrist != null &&
        lShoulder.likelihood >= minConfidence && lElbow.likelihood >= minConfidence && lWrist.likelihood >= minConfidence

    val hasRightArm = rShoulder != null && rElbow != null && rWrist != null &&
        rShoulder.likelihood >= minConfidence && rElbow.likelihood >= minConfidence && rWrist.likelihood >= minConfidence

    if (!hasLeftArm && !hasRightArm) {
      missingPoseFrames++
      if (missingPoseFrames >= 25) {
        resetCurrentRep()
      }
      return PushUpProcessResult(totalReps, getDisplayState(), false, "Position upper body in view")
    }
    missingPoseFrames = 0

    // 3. Posture / Torso check (hips & knees are optional!)
    val avgShoulderY = when {
      lShoulder != null && rShoulder != null -> (lShoulder.y + rShoulder.y) / 2f
      lShoulder != null -> lShoulder.y
      else -> rShoulder!!.y
    }
    val avgShoulderX = when {
      lShoulder != null && rShoulder != null -> (lShoulder.x + rShoulder.x) / 2f
      lShoulder != null -> lShoulder.x
      else -> rShoulder!!.x
    }

    val avgWristY = when {
      lWrist != null && rWrist != null -> (lWrist.y + rWrist.y) / 2f
      lWrist != null -> lWrist.y
      else -> rWrist!!.y
    }

    val mainShoulder = lShoulder ?: rShoulder!!
    val mainElbow = lElbow ?: rElbow!!
    val mainWrist = lWrist ?: rWrist!!
    val armSpan = dist(mainShoulder, mainElbow) + dist(mainElbow, mainWrist)
    val armScale = if (armSpan > 1f) armSpan else 100f

    // In Android image coordinates, Y increases downwards.
    // wristShoulderDy measures how high shoulders are elevated above wrists/hands on screen.
    val wristShoulderDy = (avgWristY - avgShoulderY) / armScale

    // Pre-calculate raw elbow angle for posture checks
    val hasLeftArmAngle = hasLeftArm && lShoulder != null && lElbow != null && lWrist != null
    val hasRightArmAngle = hasRightArm && rShoulder != null && rElbow != null && rWrist != null
    val rawLeftAngle = if (hasLeftArmAngle) calculateElbowAngle(lShoulder!!, lElbow!!, lWrist!!) else null
    val rawRightAngle = if (hasRightArmAngle) calculateElbowAngle(rShoulder!!, rElbow!!, rWrist!!) else null
    val rawArmAngle = when {
      rawLeftAngle != null && rawRightAngle != null -> (rawLeftAngle + rawRightAngle) / 2.0
      rawLeftAngle != null -> rawLeftAngle
      rawRightAngle != null -> rawRightAngle
      else -> 180.0
    }

    var isUprightPosture = false
    var isLyingFlatPosture = false

    if (lHip != null && rHip != null && lHip.likelihood >= minConfidence && rHip.likelihood >= minConfidence) {
      val avgHipY = (lHip.y + rHip.y) / 2f
      val avgHipX = (lHip.x + rHip.x) / 2f

      val dx = (avgHipX - avgShoulderX).toDouble()
      val dy = (avgHipY - avgShoulderY).toDouble()

      // Reject upright standing posture where hips are vertically beneath shoulders (dy >> dx)
      if (dy > 0.30 * armScale && kotlin.math.abs(dy) > 1.4 * kotlin.math.abs(dx)) {
        isUprightPosture = true
      }

      // Check optional knee landmarks for sitting / upright kneeling / one-knee support cheat
      val lKnee = keypoints[PoseLandmark.LEFT_KNEE]
      val rKnee = keypoints[PoseLandmark.RIGHT_KNEE]
      if (lKnee != null || rKnee != null) {
        val kneeY = when {
          lKnee != null && rKnee != null -> (lKnee.y + rKnee.y) / 2f
          lKnee != null -> lKnee.y
          else -> rKnee!!.y
        }
        val kneeLikelihood = when {
          lKnee != null && rKnee != null -> kotlin.math.min(lKnee.likelihood, rKnee.likelihood)
          lKnee != null -> lKnee.likelihood
          else -> rKnee!!.likelihood
        }
        if (kneeLikelihood >= minConfidence) {
          // Upright torso with knees vertically lower on screen than hips indicates sitting or floor-kneeling support
          if (dy > 0.25 * armScale && (kneeY - avgHipY) > 0.20 * armScale) {
            isUprightPosture = true
          }
        }
      }

      // Check lying flat prone posture where whole torso rests flat on floor
      val shoulderHipDy = kotlin.math.abs(avgShoulderY - avgHipY) / armScale
      if (shoulderHipDy < 0.22) {
        if (rawArmAngle >= 130.0 && wristShoulderDy < 0.22) {
          // Extended arms without torso elevation above wrists = pushing flat on floor
          isLyingFlatPosture = true
        } else if (wristShoulderDy < 0.08) {
          // Torso collapsed flat on floor
          isLyingFlatPosture = true
        }
      }
    } else {
      // Hips not detected: check if shoulders fail to elevate above wrists when arms are extended
      if (rawArmAngle >= 135.0 && wristShoulderDy < 0.08) {
        isLyingFlatPosture = true
      }
    }

    val isValidPushUpPosture = !isUprightPosture && !isLyingFlatPosture

    if (!isValidPushUpPosture) {
      invalidPostureFrames++
      if (invalidPostureFrames >= 3) {
        currentRepWasPostureValid = false
        if (phase != PushUpPhase.WAITING_FOR_TOP) {
          resetCurrentRep()
        }
        val msg = if (isUprightPosture) "Get into push-up position" else "Elevate body into plank stance"
        return PushUpProcessResult(totalReps, getDisplayState(), false, msg)
      }
    } else {
      invalidPostureFrames = 0
    }

    // 4. Calculate Elbow Angles & Two-Arm Coordination
    val leftAngle = if (hasLeftArm) calculateElbowAngle(lShoulder!!, lElbow!!, lWrist!!) else null
    val rightAngle = if (hasRightArm) calculateElbowAngle(rShoulder!!, rElbow!!, rWrist!!) else null

    val currentRawAngle: Double

    if (leftAngle != null && rightAngle != null) {
      // Both arms detected: Enforce two-arm coordination
      val angleDiff = kotlin.math.abs(leftAngle - rightAngle)
      if (angleDiff > 35.0) {
        // Asymmetric movement (e.g. one arm waving/moving while other is still)
        val stateStr = getDisplayState()
        return PushUpProcessResult(totalReps, stateStr, true, "Move both arms evenly")
      }
      currentRawAngle = (leftAngle + rightAngle) / 2.0
    } else {
      // Single arm detected (e.g. side profile view)
      currentRawAngle = leftAngle ?: rightAngle!!
    }

    // 5. Temporal Angle Smoothing (EMA filter)
    val smoothedAngle = if (smoothedAngleEMA == null) {
      currentRawAngle
    } else {
      0.40 * currentRawAngle + 0.60 * smoothedAngleEMA!!
    }
    smoothedAngleEMA = smoothedAngle
    lastSmoothedAngle = smoothedAngle

    val logNow = System.currentTimeMillis()
    if (logNow - lastDebugLogTimeMs >= 300L) {
      lastDebugLogTimeMs = logNow
      val avgHipYVal = if (lHip != null && rHip != null) (lHip.y + rHip.y) / 2f else null
      val avgHipYStr = if (avgHipYVal != null) String.format("%.1f", avgHipYVal) else "N/A"
      val shoulderHipDyStr = if (avgHipYVal != null) String.format("%.3f", kotlin.math.abs(avgShoulderY - avgHipYVal) / armScale) else "N/A"
      android.util.Log.d(
        "PUSHUP_DEBUG",
        "phase=$phase, smoothedAngle=${String.format("%.1f", smoothedAngle)}, " +
        "shY=${String.format("%.1f", avgShoulderY)}, hipY=$avgHipYStr, wrY=${String.format("%.1f", avgWristY)}, " +
        "scale=${String.format("%.1f", armScale)}, shHipDy=$shoulderHipDyStr, wrShDy=${String.format("%.3f", wristShoulderDy)}, " +
        "lyingFlat=$isLyingFlatPosture, upright=$isUprightPosture, validPosture=$isValidPushUpPosture, " +
        "repPostureValid=$currentRepWasPostureValid"
      )
    }

    // Update adaptive baseline for TOP extended position
    if (phase == PushUpPhase.WAITING_FOR_TOP || phase == PushUpPhase.TOP) {
      if (smoothedAngle >= 135.0) {
        baselineTopAngle = 0.90 * baselineTopAngle + 0.10 * smoothedAngle
      }
    }

    // Dynamic thresholds derived relative to user's TOP baseline
    val targetTopAngle = kotlin.math.max(138.0, baselineTopAngle - 12.0)
    val targetBottomAngle = kotlin.math.min(105.0, baselineTopAngle - 35.0)

    val reachedTopCurrent = smoothedAngle >= targetTopAngle &&
        (leftAngle == null || leftAngle >= targetTopAngle - 10.0) &&
        (rightAngle == null || rightAngle >= targetTopAngle - 10.0)

    val reachedBottomCurrent = smoothedAngle <= targetBottomAngle &&
        (leftAngle == null || leftAngle <= targetBottomAngle + 10.0) &&
        (rightAngle == null || rightAngle <= targetBottomAngle + 10.0)

    val now = System.currentTimeMillis()

    // 6. Strict State Machine: TOP -> DESCENDING -> BOTTOM -> ASCENDING -> TOP (+1)
    when (phase) {
      PushUpPhase.WAITING_FOR_TOP -> {
        if (reachedTopCurrent && isValidPushUpPosture) {
          topFrameCount++
          if (topFrameCount >= 2) {
            phase = PushUpPhase.TOP
            currentRepWasPostureValid = true
            topFrameCount = 0
            descendingFrameCount = 0
            bottomFrameCount = 0
            ascendingFrameCount = 0
          }
        } else {
          topFrameCount = 0
        }
      }

      PushUpPhase.TOP -> {
        // Enforce cooldown after previous repetition to prevent duplicate detection
        if (now - lastRepTimeMs >= 350L) {
          if (smoothedAngle <= baselineTopAngle - 18.0) {
            descendingFrameCount++
            if (descendingFrameCount >= 2) {
              phase = PushUpPhase.DESCENDING
              descentStartTimeMs = now
              descendingFrameCount = 0
              bottomFrameCount = 0
            }
          } else {
            descendingFrameCount = 0
          }
        }
      }

      PushUpPhase.DESCENDING -> {
        if (reachedBottomCurrent) {
          bottomFrameCount++
          if (bottomFrameCount >= 2) {
            phase = PushUpPhase.BOTTOM
            bottomFrameCount = 0
            ascendingFrameCount = 0
          }
        } else if (reachedTopCurrent) {
          // Partial push-up rejected: user rose back up without reaching bottom depth
          topFrameCount++
          if (topFrameCount >= 2) {
            phase = PushUpPhase.TOP
            topFrameCount = 0
            descendingFrameCount = 0
            bottomFrameCount = 0
          }
        } else {
          bottomFrameCount = 0
        }
      }

      PushUpPhase.BOTTOM -> {
        // Holding bottom does NOT increment counter
        if (smoothedAngle >= targetBottomAngle + 15.0) {
          ascendingFrameCount++
          if (ascendingFrameCount >= 2) {
            phase = PushUpPhase.ASCENDING
            ascendingFrameCount = 0
            topFrameCount = 0
          }
        } else {
          ascendingFrameCount = 0
        }
      }

      PushUpPhase.ASCENDING -> {
        if (reachedTopCurrent) {
          topFrameCount++
          if (topFrameCount >= 2) {
            // Full cycle verified: TOP -> DESCENDING -> BOTTOM -> ASCENDING -> TOP!
            val repDuration = now - descentStartTimeMs
            if (currentRepWasPostureValid && repDuration >= 400L && (now - lastRepTimeMs) >= 450L) {
              if (totalReps < 15) {
                totalReps++
              }
              lastRepTimeMs = now
            }
            phase = PushUpPhase.TOP
            currentRepWasPostureValid = true
            topFrameCount = 0
            descendingFrameCount = 0
            bottomFrameCount = 0
            ascendingFrameCount = 0
          }
        } else if (reachedBottomCurrent) {
          // User fell back to bottom before completing ascent
          phase = PushUpPhase.BOTTOM
          bottomFrameCount = 0
          ascendingFrameCount = 0
        } else {
          topFrameCount = 0
        }
      }
    }

    val stateStr = getDisplayState()
    val guidance = when (phase) {
      PushUpPhase.WAITING_FOR_TOP -> "Upper body detected • Get into plank position"
      PushUpPhase.TOP -> "Upper body detected • Push-Up State: UP"
      PushUpPhase.DESCENDING -> "Upper body detected • Push-Up State: DOWN"
      PushUpPhase.BOTTOM -> "Upper body detected • Push-Up State: DOWN"
      PushUpPhase.ASCENDING -> "Upper body detected • Push-Up State: UP"
    }

    return PushUpProcessResult(totalReps, stateStr, true, guidance)
  }

  private fun resetCurrentRep() {
    phase = PushUpPhase.WAITING_FOR_TOP
    topFrameCount = 0
    descendingFrameCount = 0
    bottomFrameCount = 0
    ascendingFrameCount = 0
    invalidPostureFrames = 0
    currentRepWasPostureValid = true
    smoothedAngleEMA = null
    lastSmoothedAngle = null
  }

  private fun getDisplayState(): String {
    return when (phase) {
      PushUpPhase.BOTTOM -> "DOWN"
      PushUpPhase.DESCENDING -> "DOWN"
      else -> "UP"
    }
  }
}

@Composable
fun PushUpChallengeScreen(
  onDismiss: () -> Unit,
  onSkipExit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  BackHandler(enabled = true) {
    onDismiss()
  }

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }
  var isCameraActive by remember { mutableStateOf(false) }
  var permissionDeniedError by remember { mutableStateOf(false) }
  var cameraError by remember { mutableStateOf<String?>(null) }
  var pushUpCount by remember { mutableStateOf(0) }
  var showTutorialOverlay by remember { mutableStateOf(false) }

  LaunchedEffect(pushUpCount) {
    if (pushUpCount >= 15) {
      isCameraActive = false
      onSkipExit()
    }
  }

  val poseDetector = remember {
    val options = PoseDetectorOptions.Builder()
      .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
      .build()
    PoseDetection.getClient(options)
  }
  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
  var poseOverlayData by remember { mutableStateOf<PoseOverlayData?>(null) }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
    if (isGranted) {
      permissionDeniedError = false
      cameraError = null
      isCameraActive = true
    } else {
      isCameraActive = false
      permissionDeniedError = true
    }
  }

  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      hasCameraPermission = true
      permissionDeniedError = false
      cameraError = null
      isCameraActive = true
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  DisposableEffect(lifecycleOwner) {
    onDispose {
      try {
        poseDetector.close()
        cameraExecutor.shutdown()
      } catch (_: Exception) {}
      try {
        val executor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
          try {
            cameraProviderFuture.get().unbindAll()
          } catch (_: Exception) {}
        }, executor)
      } catch (_: Exception) {}
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF111214))
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation / Close & Form Guide
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(36.dp)
            .testTag("pushup_close_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancel push-up challenge",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
          )
        }

        Surface(
          onClick = { showTutorialOverlay = true },
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFF22252A),
          border = BorderStroke(1.dp, Color(0xFF32363E)),
          modifier = Modifier.testTag("pushup_form_guide_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = "Form Guide",
              tint = SignatureNeonLime,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Form Guide",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Earn Your Exit",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp
        ),
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Complete 15 push-ups to end your focus session early.",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 13.sp
        ),
        color = Color.White.copy(alpha = 0.55f),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Counter Badge Display
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1C20),
        border = BorderStroke(1.dp, Color(0xFF282B30))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(SignatureNeonLime)
          )
          Text(
            text = "$pushUpCount / 15",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              letterSpacing = 0.5.sp
            ),
            color = Color.White,
            modifier = Modifier.testTag("pushup_counter_text")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Camera Preview Container
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF17191C))
          .border(1.dp, Color(0xFF26282E), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (cameraError != null) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF262930)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Camera Error",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
              color = Color.White,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = cameraError ?: "Failed to start camera",
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
              color = Color.White.copy(alpha = 0.6f),
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                cameraError = null
                isCameraActive = true
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureNeonLime,
                contentColor = DarkButtonCharcoal
              )
            ) {
              Text("Retry Camera", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        } else if (isCameraActive && hasCameraPermission) {
          AndroidView(
            factory = { ctx ->
              PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FIT_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                  try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { p ->
                      p.setSurfaceProvider(surfaceProvider)
                    }

                    val cameraSelector = when {
                      cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                      cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                      else -> null
                    }

                    if (cameraSelector != null) {
                      val isFront = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                      val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                      val pushUpTracker = PushUpTracker()
                      var completedReps = 0

                      imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        @OptIn(ExperimentalGetImage::class)
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                          val rotation = imageProxy.imageInfo.rotationDegrees
                          val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
                          val imgWidth = imageProxy.width
                          val imgHeight = imageProxy.height

                          poseDetector.process(inputImage)
                            .addOnSuccessListener { pose ->
                              val keypointMap = mutableMapOf<Int, PoseKeypoint>()
                              val targetTypes = intArrayOf(
                                PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
                                PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
                                PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST,
                                PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
                                PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
                                PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
                              )
                              for (t in targetTypes) {
                                val lm = pose.getPoseLandmark(t)
                                if (lm != null && lm.inFrameLikelihood > 0.35f) {
                                  keypointMap[t] = PoseKeypoint(
                                    x = lm.position.x,
                                    y = lm.position.y,
                                    likelihood = lm.inFrameLikelihood
                                  )
                                }
                              }

                              val processResult = pushUpTracker.processPose(keypointMap)

                              if (processResult.reps != completedReps) {
                                completedReps = processResult.reps
                                val newReps = processResult.reps
                                ContextCompat.getMainExecutor(context).execute {
                                  pushUpCount = newReps
                                }
                              }

                              poseOverlayData = PoseOverlayData(
                                landmarksMap = keypointMap,
                                imageWidth = imgWidth,
                                imageHeight = imgHeight,
                                rotationDegrees = rotation,
                                isFrontCamera = isFront,
                                pushUpState = processResult.stateStr,
                                elbowAngle = pushUpTracker.lastSmoothedAngle,
                                isBodyValid = processResult.isBodyValid,
                                guidanceMessage = processResult.guidanceMessage
                              )
                            }
                            .addOnCompleteListener {
                              imageProxy.close()
                            }
                        } else {
                          imageProxy.close()
                        }
                      }

                      cameraProvider.unbindAll()
                      cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                      )
                      cameraError = null
                    } else {
                      cameraError = "No camera hardware available"
                    }
                  } catch (e: Exception) {
                    cameraError = e.localizedMessage ?: "Failed to start camera preview"
                  }
                }, executor)
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          // Live Skeleton Overlay
          val currentPoseData = poseOverlayData
          if (currentPoseData != null && currentPoseData.landmarksMap.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val canvasW = size.width
              val canvasH = size.height

              val isRotated = currentPoseData.rotationDegrees == 90 || currentPoseData.rotationDegrees == 270
              val rotatedW = if (isRotated) currentPoseData.imageHeight.toFloat() else currentPoseData.imageWidth.toFloat()
              val rotatedH = if (isRotated) currentPoseData.imageWidth.toFloat() else currentPoseData.imageHeight.toFloat()

              val scale = minOf(canvasW / rotatedW, canvasH / rotatedH)
              val offsetX = (canvasW - rotatedW * scale) / 2f
              val offsetY = (canvasH - rotatedH * scale) / 2f

              fun transformPoint(kp: PoseKeypoint): Offset {
                val rawX = if (currentPoseData.isFrontCamera) (rotatedW - kp.x) else kp.x
                val screenX = offsetX + rawX * scale
                val screenY = offsetY + kp.y * scale
                return Offset(screenX, screenY)
              }

              val connections = listOf(
                Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
                Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
                Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
                Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
                Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
                Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
                Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
                Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
                Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
                Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
                Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
                Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
              )

              for ((startType, endType) in connections) {
                val p1 = currentPoseData.landmarksMap[startType]
                val p2 = currentPoseData.landmarksMap[endType]
                if (p1 != null && p2 != null) {
                  drawLine(
                    color = Color(0xFF00E5FF),
                    start = transformPoint(p1),
                    end = transformPoint(p2),
                    strokeWidth = 5.dp.toPx()
                  )
                }
              }

              for ((_, kp) in currentPoseData.landmarksMap) {
                val point = transformPoint(kp)
                drawCircle(
                  color = Color.White,
                  radius = 7.dp.toPx(),
                  center = point
                )
                drawCircle(
                  color = Color(0xFF39FF14),
                  radius = 4.5f.dp.toPx(),
                  center = point
                )
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              val currentData = poseOverlayData
              val hasPose = currentData?.landmarksMap?.isNotEmpty() == true
              val isBodyValid = currentData?.isBodyValid == true
              val stateString = currentData?.pushUpState ?: "UP"
              val guidanceText = currentData?.guidanceMessage ?: "Position upper body in view"

              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(
                    if (hasPose && isBodyValid) {
                      if (stateString == "DOWN") Color(0xFF00E5FF) else Color(0xFF39FF14)
                    } else {
                      Color(0xFFFFCC00)
                    }
                  )
              )
              Spacer(modifier = Modifier.size(8.dp))
              Text(
                text = if (hasPose) guidanceText else "Position upper body in view",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                ),
                color = Color.White
              )
            }
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF22252A)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (permissionDeniedError) Icons.Outlined.Warning else Icons.Outlined.CameraFront,
                contentDescription = null,
                tint = if (permissionDeniedError) Color(0xFFEF5350) else SignatureNeonLime,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (permissionDeniedError) {
              Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = Color.White,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Camera access is needed for live push-up detection to verify your early exit.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                      data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                  Text("Open Settings", fontSize = 12.5.sp)
                }
                Button(
                  onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = SignatureNeonLime,
                    contentColor = DarkButtonCharcoal
                  )
                ) {
                  Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
              }
            } else {
              Text(
                text = "Camera Preview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = Color.White,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Position your upper body in view to begin push-up detection.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          if (hasCameraPermission) {
            permissionDeniedError = false
            isCameraActive = true
          } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("start_camera_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isCameraActive) Color(0xFF222428) else SignatureNeonLime,
          contentColor = if (isCameraActive) Color.White else DarkButtonCharcoal
        )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Videocam,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = if (isCameraActive) "Camera Active" else if (permissionDeniedError) "Grant Camera Permission" else "Start Camera",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp
            )
          )
        }
      }
    }

    if (showTutorialOverlay) {
      PushUpTutorialOverlay(
        onDismiss = { showTutorialOverlay = false }
      )
    }
  }
}

@Composable
private fun PushUpTutorialOverlay(
  onDismiss: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.85f))
      .clickable(onClick = onDismiss)
      .padding(20.dp)
      .testTag("pushup_tutorial_overlay"),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      onClick = {},
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF1B1D21),
      border = BorderStroke(1.dp, Color(0xFF2C3038)),
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SignatureNeonLime.copy(alpha = 0.15f))
            .border(1.dp, SignatureNeonLime.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = SignatureNeonLime,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Push-Up Form Guide",
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = Color.White,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Follow these guidelines for accurate AI rep tracking (15 reps total):",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp
          ),
          color = Color.White.copy(alpha = 0.65f),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          TutorialStepItem(
            stepNumber = "1",
            title = "Position Your Device",
            description = "Place phone facing you on the floor or leaned against an object. Ensure head, shoulders, and arms are visible."
          )
          TutorialStepItem(
            stepNumber = "2",
            title = "Horizontal Plank Posture",
            description = "Get into a horizontal plank. The AI detector filters out upright, sitting, or resting posture."
          )
          TutorialStepItem(
            stepNumber = "3",
            title = "Lower Chest Fully (DOWN)",
            description = "Bend your elbows and lower your torso until the live status badge changes to 'DOWN'."
          )
          TutorialStepItem(
            stepNumber = "4",
            title = "Extend Arms Fully (UP)",
            description = "Push back up until arms extend completely. Complete 15 reps (15/15) to earn early exit!"
          )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("pushup_tutorial_got_it_button"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = SignatureNeonLime,
            contentColor = DarkButtonCharcoal
          )
        ) {
          Text(
            text = "Got It, Let's Start",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun TutorialStepItem(
  stepNumber: String,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(26.dp)
        .clip(CircleShape)
        .background(Color(0xFF262930))
        .border(1.dp, SignatureNeonLime.copy(alpha = 0.5f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stepNumber,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        ),
        color = SignatureNeonLime
      )
    }

    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 13.5.sp
        ),
        color = Color.White
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 12.sp,
          lineHeight = 16.sp
        ),
        color = Color.White.copy(alpha = 0.6f)
      )
    }
  }
}
