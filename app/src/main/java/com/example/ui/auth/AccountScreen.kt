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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.settings.LogoutConfirmationDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkButtonCharcoal
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkContainerNeutral
import com.example.ui.theme.DarkSubtleBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightCardSurface
import com.example.ui.theme.LightContainerNeutral
import com.example.ui.theme.LightPageBackground
import com.example.ui.theme.LightSubtleBorder
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.SignatureLimeDarkText
import com.example.ui.theme.SignatureNeonLime

@Composable
fun AccountScreen(
  isLoggedIn: Boolean,
  userName: String,
  userEmail: String,
  isNightMode: Boolean,
  onBackClick: () -> Unit,
  onSignUp: (String, String, String) -> String?,
  onUpdatePassword: (String) -> Pair<Boolean, String>,
  onLogin: (String, String) -> String?,
  onSendPasswordReset: suspend (String) -> Pair<Boolean, String>,
  onLogout: () -> Unit,
  onUpdateName: (String) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var isSendingReset by remember { mutableStateOf(false) }
  var currentMode by remember { mutableStateOf("SIGNUP") }
  var nameInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var isPasswordResetSuccess by remember { mutableStateOf(false) }
  var showLogoutConfirmation by remember { mutableStateOf(false) }
  var isEditingName by remember { mutableStateOf(false) }
  var editNameInput by remember { mutableStateOf(userName) }
  var unverifiedEmail by remember { mutableStateOf("") }
  var verificationMsg by remember { mutableStateOf("") }

  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val inputContainer = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val inputFocusedBorder = if (isNightMode) SignatureNeonLime else Color.Black
  val inputFocusedLabel = if (isNightMode) SignatureNeonLime else Color.Black
  val inputTextColor = if (isNightMode) Color.White else Color.Black
  val inputCursorColor = if (isNightMode) SignatureNeonLime else Color.Black
  val inputHandleColor = if (isNightMode) SignatureNeonLime else Color.Black

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = bgColor
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .imePadding()
      ) {
        // TOP APP BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(cardBg)
              .border(1.dp, borderColor, CircleShape)
              .clickable { onBackClick() }
              .testTag("account_back_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = "Back",
              tint = textColor,
              modifier = Modifier.size(18.dp)
            )
          }

          Text(
            text = "Account",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = textColor
          )

          Spacer(modifier = Modifier.width(38.dp))
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
          if (!isLoggedIn || currentMode == "CREATE_PASSWORD") {
            // =========================================
            // LOGGED OUT / SETUP STATE
            // =========================================

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
              shape = RoundedCornerShape(16.dp),
              color = cardBg,
              border = BorderStroke(1.dp, borderColor)
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = when (currentMode) {
                    "SIGNUP" -> "Create Account"
                    "CREATE_PASSWORD" -> "Create New Password"
                    "FORGOT_PASSWORD" -> "Forgot Password?"
                    else -> "Sign In"
                  },
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  ),
                  color = textColor,
                  textAlign = TextAlign.Center
                )

                Text(
                  text = when (currentMode) {
                    "SIGNUP" -> "Create an account to backup your focus stats & sync across devices"
                    "CREATE_PASSWORD" -> "Create a strong password to secure your account."
                    "FORGOT_PASSWORD" -> "Enter the email associated with your Lock In account."
                    else -> "Welcome back! Sign in to continue your focus journey"
                  },
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                  color = mutedColor,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Success message banner
                if (successMessage != null) {
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp)),
                    color = SignatureNeonLime.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SignatureNeonLime.copy(alpha = 0.4f))
                  ) {
                    Text(
                      text = successMessage!!,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = if (isNightMode) SignatureNeonLime else SignatureLimeDarkText,
                      modifier = Modifier.padding(12.dp),
                      textAlign = TextAlign.Center
                    )
                  }
                  Spacer(modifier = Modifier.height(14.dp))
                }

                // Error message banner
                if (errorMessage != null) {
                  Surface(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFFFF0F0),
                    border = BorderStroke(1.dp, Color(0xFFD93838).copy(alpha = 0.3f))
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                          onClick = {
                            currentMode = "LOGIN"
                            errorMessage = null
                            successMessage = null
                          },
                          shape = RoundedCornerShape(8.dp),
                          colors = ButtonDefaults.buttonColors(
                            containerColor = SignatureNeonLime,
                            contentColor = DarkButtonCharcoal
                          ),
                          modifier = Modifier.height(34.dp)
                        ) {
                          Text("Sign In Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  }
                  Spacer(modifier = Modifier.height(14.dp))
                }

                when (currentMode) {
                  "FORGOT_PASSWORD" -> {
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
                        .testTag("account_reset_email_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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
                        .height(46.dp)
                        .testTag("account_send_reset_button"),
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                      )
                    ) {
                      Text(
                        text = if (isSendingReset) "Sending..." else "Send Reset Link",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                      text = "Back to Sign In",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                      color = textColor,
                      modifier = Modifier.clickable {
                        currentMode = "LOGIN"
                        errorMessage = null
                        successMessage = null
                      }
                    )
                  }

                  "SIGNUP" -> {
                    OutlinedTextField(
                      value = nameInput,
                      onValueChange = {
                        nameInput = it
                        errorMessage = null
                        successMessage = null
                      },
                      label = { Text("Name") },
                      leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = mutedColor)
                      },
                      singleLine = true,
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                        .testTag("account_email_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                        .testTag("account_password_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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
                            onBackClick()
                          }
                        }
                      },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("account_continue_button"),
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                      )
                    ) {
                      Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = mutedColor
                      )
                      Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
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
                          .height(46.dp)
                          .testTag("account_login_after_reset_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                          contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                        )
                      ) {
                        Text(
                          text = "Sign In",
                          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                          .testTag("account_new_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                          focusedContainerColor = inputContainer,
                          unfocusedContainerColor = inputContainer,
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

                      Spacer(modifier = Modifier.height(12.dp))

                      PasswordRequirementsChecklist(
                        passwordInput = passwordInput,
                        isNightMode = isNightMode
                      )

                      Spacer(modifier = Modifier.height(20.dp))

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
                          .height(46.dp)
                          .testTag("account_reset_password_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                          contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                        )
                      ) {
                        Text(
                          text = "Reset Password",
                          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                      }
                    }
                  }

                  "VERIFY_EMAIL" -> {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        modifier = Modifier.size(44.dp)
                      )

                      Spacer(modifier = Modifier.height(12.dp))

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
                          .testTag("account_verify_email_message")
                      )

                      Spacer(modifier = Modifier.height(20.dp))

                      Button(
                        onClick = {
                          currentMode = "LOGIN"
                          errorMessage = null
                          successMessage = null
                        },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(46.dp)
                          .testTag("account_verify_email_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                          contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                        )
                      ) {
                        Text(
                          text = "Sign In",
                          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                      }
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
                      label = { Text("Email") },
                      leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = mutedColor)
                      },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                      singleLine = true,
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_email_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                        .testTag("account_password_input"),
                      shape = RoundedCornerShape(12.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor,
                        modifier = Modifier
                          .clickable {
                            currentMode = "FORGOT_PASSWORD"
                            errorMessage = null
                            successMessage = null
                          }
                          .testTag("account_forgot_password_link")
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
                        .height(46.dp)
                        .testTag("account_submit_button"),
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                      )
                    ) {
                      Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = mutedColor
                      )
                      Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = textColor
                      )
                    }
                  }
                }
              }
            }
          } else {
            // =========================================
            // LOGGED IN STATE
            // =========================================

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
              shape = RoundedCornerShape(16.dp),
              color = cardBg,
              border = BorderStroke(1.dp, borderColor)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                // USER PROFILE AVATAR
                val avatarLetter = if (userName.isNotBlank()) userName.trim().take(1).uppercase() else ""
                Box(
                  modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(SignatureNeonLime),
                  contentAlignment = Alignment.Center
                ) {
                  if (avatarLetter.isNotEmpty()) {
                    Text(
                      text = avatarLetter,
                      style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                      ),
                      color = DarkButtonCharcoal
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Outlined.Person,
                      contentDescription = "Profile",
                      modifier = Modifier.size(32.dp),
                      tint = DarkButtonCharcoal
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isEditingName) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = userName,
                      style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                      ),
                      color = textColor
                    )

                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(inputContainer)
                        .clickable {
                          editNameInput = userName
                          isEditingName = true
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Name",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                } else {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    OutlinedTextField(
                      value = editNameInput,
                      onValueChange = { editNameInput = it.take(30) },
                      singleLine = true,
                      modifier = Modifier
                        .weight(1f)
                        .testTag("account_edit_name_input"),
                      shape = RoundedCornerShape(10.dp),
                      colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
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

                    Button(
                      onClick = {
                        if (editNameInput.trim().isNotEmpty()) {
                          onUpdateName(editNameInput.trim())
                        }
                        isEditingName = false
                      },
                      shape = RoundedCornerShape(10.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNightMode) SignatureNeonLime else DarkButtonCharcoal,
                        contentColor = if (isNightMode) DarkButtonCharcoal else Color.White
                      )
                    ) {
                      Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                  }
                }

                if (userEmail.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = mutedColor
                  )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STATUS TAG
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = SignatureNeonLime.copy(alpha = 0.15f),
                  border = BorderStroke(1.dp, SignatureNeonLime.copy(alpha = 0.4f))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.CheckCircle,
                      contentDescription = null,
                      tint = if (isNightMode) SignatureNeonLime else SignatureLimeDarkText,
                      modifier = Modifier.size(14.dp)
                    )
                    Text(
                      text = "Account Active & Synced",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                      color = if (isNightMode) SignatureNeonLime else SignatureLimeDarkText
                    )
                  }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // LOGOUT BUTTON
                Button(
                  onClick = { showLogoutConfirmation = true },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("account_screen_logout_button"),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF0F0),
                    contentColor = Color(0xFFD93838)
                  )
                ) {
                  Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Sign Out",
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(40.dp))
        }
      }

      // Logout Confirmation Dialog
      if (showLogoutConfirmation) {
        LogoutConfirmationDialog(
          isNightMode = isNightMode,
          onDismiss = { showLogoutConfirmation = false },
          onConfirmLogout = {
            showLogoutConfirmation = false
            onLogout()
          }
        )
      }
    }
  }
}
