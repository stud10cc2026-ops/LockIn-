package com.example.ui.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.MutedTextSecondary
import com.example.ui.theme.SignatureLimeAccent
import com.example.ui.theme.SignatureLimeDarkText
import com.example.ui.theme.SubtleBorder

@Composable
fun AuthModalOverlay(
  isOpen: Boolean,
  initialMode: String, // "LOGIN", "SIGNUP", or "FORGOT_PASSWORD"
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onSignUp: (String, String, String) -> String?,
  onUpdatePassword: (String) -> Pair<Boolean, String>,
  onLogin: (String, String) -> String?,
  onSendPasswordReset: suspend (String) -> Pair<Boolean, String>
) {
  if (!isOpen) return

  val coroutineScope = rememberCoroutineScope()
  var isSendingReset by remember { mutableStateOf(false) }
  var currentMode by remember(initialMode) { mutableStateOf(initialMode) }
  var nameInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var isPasswordResetSuccess by remember { mutableStateOf(false) }
  var unverifiedEmail by remember { mutableStateOf("") }
  var verificationMsg by remember { mutableStateOf("") }

  val cardBg = if (isNightMode) DarkCardSurface else CardWhite
  val textColor = if (isNightMode) Color.White else DarkTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else MutedTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else SubtleBorder
  val inputFocusedBorder = if (isNightMode) SignatureLimeAccent else Color.Black
  val inputFocusedLabel = if (isNightMode) SignatureLimeAccent else Color.Black
  val inputTextColor = if (isNightMode) Color.White else Color.Black
  val inputCursorColor = if (isNightMode) SignatureLimeAccent else Color.Black
  val inputHandleColor = if (isNightMode) SignatureLimeAccent else Color.Black

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.70f))
      .clickable(enabled = currentMode != "CREATE_PASSWORD") { onDismiss() },
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .shadow(24.dp, RoundedCornerShape(28.dp))
        .clickable { /* prevent dismissal on dialog click */ }
        .testTag("auth_dialog_surface"),
      shape = RoundedCornerShape(28.dp),
      color = cardBg,
      border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header Row with Title / Close
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = when (currentMode) {
              "SIGNUP" -> "Create Account"
              "CREATE_PASSWORD" -> "Create New Password"
              "FORGOT_PASSWORD" -> "Forgot Password?"
              "VERIFY_EMAIL" -> "Verify Your Email"
              else -> "Sign In"
            },
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            ),
            color = textColor
          )

          if (currentMode != "CREATE_PASSWORD") {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isNightMode) Color(0xFF2C2F34) else Color(0xFFF2F2F2))
                .clickable { onDismiss() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = mutedColor,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        if (currentMode == "CREATE_PASSWORD") {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Create a strong password. You'll use this password whenever you sign in.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = mutedColor,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Success message banner
        if (successMessage != null) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            color = SignatureLimeAccent.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SignatureLimeAccent)
          ) {
            Text(
              text = successMessage!!,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = if (isNightMode) SignatureLimeAccent else SignatureLimeDarkText,
              modifier = Modifier.padding(12.dp),
              textAlign = TextAlign.Center
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Error message banner
        if (errorMessage != null) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFFFFF0F0),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCCCC))
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFD93838),
                textAlign = TextAlign.Center
              )
              if (errorMessage!!.contains("Account already exists", ignoreCase = true) || errorMessage!!.contains("Log In", ignoreCase = true) || errorMessage!!.contains("Sign In", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    currentMode = "LOGIN"
                    errorMessage = null
                    successMessage = null
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = SignatureLimeAccent,
                    contentColor = SignatureLimeDarkText
                  ),
                  modifier = Modifier.height(36.dp)
                ) {
                  Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        when (currentMode) {
          "FORGOT_PASSWORD" -> {
            Text(
              text = "Enter the email associated with your Lock In account.",
              style = MaterialTheme.typography.bodySmall,
              color = mutedColor,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
              value = emailInput,
              onValueChange = {
                emailInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Email") },
              leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = mutedColor)
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_reset_email_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              enabled = !isSendingReset,
              onClick = {
                errorMessage = null
                successMessage = null
                val trimmedEmail = emailInput.trim()
                if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                  errorMessage = "Please enter a valid email address."
                } else {
                  isSendingReset = true
                  coroutineScope.launch {
                    val (ok, msg) = onSendPasswordReset(trimmedEmail)
                    isSendingReset = false
                    if (ok) {
                      successMessage = msg.ifEmpty { "Password reset link sent to $trimmedEmail.\nCheck your email to continue." }
                    } else {
                      errorMessage = msg
                    }
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_send_reset_button"),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureLimeAccent,
                contentColor = SignatureLimeDarkText
              )
            ) {
              Text(
                text = if (isSendingReset) "Sending..." else "Send Reset Link",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "Back to Sign In",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = textColor,
              modifier = Modifier.clickable {
                currentMode = "LOGIN"
                errorMessage = null
                successMessage = null
              }
            )
          }

          "SIGNUP" -> {
            // STEP 1 — CREATE ACCOUNT (Name, Email, Password -> Continue)
            OutlinedTextField(
              value = nameInput,
              onValueChange = {
                nameInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Full Name") },
              leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = mutedColor)
              },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_name_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = emailInput,
              onValueChange = {
                emailInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Email Address") },
              leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = mutedColor)
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = passwordInput,
              onValueChange = {
                passwordInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Password") },
              leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = mutedColor)
              },
              trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                  Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    tint = mutedColor
                  )
                }
              },
              visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            PasswordRequirementsChecklist(
              passwordInput = passwordInput,
              isNightMode = isNightMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                errorMessage = null
                val trimmedName = nameInput.trim()
                val trimmedEmail = emailInput.trim()

                if (trimmedName.isEmpty()) {
                  errorMessage = "Please enter your name."
                } else if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                  errorMessage = "Please enter a valid email address."
                } else if (passwordInput.isEmpty()) {
                  errorMessage = "Please enter a password."
                } else if (passwordInput.length < 8) {
                  errorMessage = "Password must be at least 8 characters long."
                } else if (!passwordInput.any { it.isLetter() }) {
                  errorMessage = "Password must contain at least 1 letter."
                } else if (!passwordInput.any { it.isDigit() }) {
                  errorMessage = "Password must contain at least 1 number."
                } else {
                  val result = onSignUp(trimmedName, trimmedEmail, passwordInput)
                  if (result != null) {
                    if (result.startsWith("VERIFY_EMAIL:")) {
                      unverifiedEmail = trimmedEmail
                      verificationMsg = result.removePrefix("VERIFY_EMAIL:")
                      currentMode = "VERIFY_EMAIL"
                      errorMessage = null
                      successMessage = null
                    } else {
                      errorMessage = result
                    }
                  } else {
                    errorMessage = null
                    successMessage = null
                    onDismiss()
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_continue_button"),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureLimeAccent,
                contentColor = SignatureLimeDarkText
              )
            ) {
              Text(
                text = "Create Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.clickable {
                currentMode = "LOGIN"
                errorMessage = null
                successMessage = null
              },
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedColor
              )
              Text(
                text = "Sign In",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
              )
            }
          }

          "CREATE_PASSWORD" -> {
            if (isPasswordResetSuccess) {
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  currentMode = "LOGIN"
                  errorMessage = null
                  successMessage = null
                  isPasswordResetSuccess = false
                  passwordInput = ""
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("auth_login_after_reset_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SignatureLimeAccent,
                  contentColor = SignatureLimeDarkText
                )
              ) {
                Text(
                  text = "Sign In",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
            } else {
              OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                  passwordInput = it
                  errorMessage = null
                  successMessage = null
                },
                label = { Text("New Password") },
                leadingIcon = {
                  Icon(Icons.Outlined.Lock, contentDescription = null, tint = mutedColor)
                },
                trailingIcon = {
                  IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                      imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                      contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                      tint = mutedColor
                    )
                  }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("auth_new_password_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = inputFocusedBorder,
                  unfocusedBorderColor = borderColor,
                  focusedLabelColor = inputFocusedLabel,
                  unfocusedLabelColor = mutedColor,
                  focusedTextColor = inputTextColor,
                  unfocusedTextColor = inputTextColor,
                  cursorColor = inputCursorColor,
                  selectionColors = TextSelectionColors(
                    handleColor = inputHandleColor,
                    backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                  )
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              PasswordRequirementsChecklist(
                passwordInput = passwordInput,
                isNightMode = isNightMode
              )

              Spacer(modifier = Modifier.height(24.dp))

              Button(
                onClick = {
                  errorMessage = null
                  val trimmedPass = passwordInput.trim()
                  if (trimmedPass.isEmpty()) {
                    errorMessage = "Please enter a new password."
                  } else if (trimmedPass.length < 8) {
                    errorMessage = "Password must be at least 8 characters long."
                  } else if (!trimmedPass.any { it.isLetter() }) {
                    errorMessage = "Password must contain at least 1 letter."
                  } else if (!trimmedPass.any { it.isDigit() }) {
                    errorMessage = "Password must contain at least 1 number."
                  } else {
                    val (ok, msg) = onUpdatePassword(trimmedPass)
                    if (ok) {
                      successMessage = "Password updated successfully."
                      isPasswordResetSuccess = true
                    } else {
                      errorMessage = msg
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("auth_reset_password_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SignatureLimeAccent,
                  contentColor = SignatureLimeDarkText
                )
              ) {
                Text(
                  text = "Reset Password",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }

          "VERIFY_EMAIL" -> {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = null,
              tint = SignatureLimeAccent,
              modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = verificationMsg.ifEmpty {
                "We have sent you a verification email to $unverifiedEmail. Please verify it and sign in."
              },
              style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
              ),
              color = textColor,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("verify_email_message")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
              onClick = {
                currentMode = "LOGIN"
                errorMessage = null
                successMessage = null
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_email_login_button"),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureLimeAccent,
                contentColor = SignatureLimeDarkText
              )
            ) {
              Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
          }

          else -> { // "LOGIN"
            OutlinedTextField(
              value = emailInput,
              onValueChange = {
                emailInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Email Address") },
              leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = mutedColor)
              },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = passwordInput,
              onValueChange = {
                passwordInput = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("Password") },
              leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = mutedColor)
              },
              trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                  Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    tint = mutedColor
                  )
                }
              },
              visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input"),
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = inputFocusedBorder,
                unfocusedBorderColor = borderColor,
                focusedLabelColor = inputFocusedLabel,
                unfocusedLabelColor = mutedColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                cursorColor = inputCursorColor,
                selectionColors = TextSelectionColors(
                  handleColor = inputHandleColor,
                  backgroundColor = inputHandleColor.copy(alpha = 0.2f)
                )
              )
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
              horizontalArrangement = Arrangement.End
            ) {
              Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                modifier = Modifier
                  .clickable {
                    currentMode = "FORGOT_PASSWORD"
                    errorMessage = null
                    successMessage = null
                  }
                  .testTag("auth_forgot_password_link")
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                errorMessage = null
                successMessage = null
                val result = onLogin(emailInput, passwordInput)
                if (result != null) {
                  if (result.startsWith("VERIFY_EMAIL:")) {
                    unverifiedEmail = emailInput.trim()
                    verificationMsg = result.removePrefix("VERIFY_EMAIL:")
                    currentMode = "VERIFY_EMAIL"
                    errorMessage = null
                    successMessage = null
                  } else {
                    errorMessage = result
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_submit_button"),
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = SignatureLimeAccent,
                contentColor = SignatureLimeDarkText
              )
            ) {
              Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.clickable {
                currentMode = "SIGNUP"
                errorMessage = null
                successMessage = null
              },
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedColor
              )
              Text(
                text = "Create Account",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun PasswordRequirementsChecklist(
  passwordInput: String,
  isNightMode: Boolean
) {
  val reqLength = passwordInput.length >= 8
  val reqLetter = passwordInput.any { it.isLetter() }
  val reqNumber = passwordInput.any { it.isDigit() }

  val activeColor = SignatureLimeAccent
  val inactiveColor = if (isNightMode) DarkTextSecondary else MutedTextSecondary

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 6.dp, start = 4.dp, end = 4.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    RequirementRow(label = "At least 8 characters", isFulfilled = reqLength, activeColor = activeColor, inactiveColor = inactiveColor)
    RequirementRow(label = "At least 1 letter", isFulfilled = reqLetter, activeColor = activeColor, inactiveColor = inactiveColor)
    RequirementRow(label = "At least 1 number", isFulfilled = reqNumber, activeColor = activeColor, inactiveColor = inactiveColor)
  }
}

@Composable
private fun RequirementRow(
  label: String,
  isFulfilled: Boolean,
  activeColor: Color,
  inactiveColor: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = if (isFulfilled) Icons.Outlined.Check else Icons.Outlined.RadioButtonUnchecked,
      contentDescription = null,
      tint = if (isFulfilled) activeColor else inactiveColor,
      modifier = Modifier.size(16.dp)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
      color = if (isFulfilled) activeColor else inactiveColor
    )
  }
}
