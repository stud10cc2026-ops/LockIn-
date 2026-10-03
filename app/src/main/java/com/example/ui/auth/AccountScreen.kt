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
  onUpdateName: (String) -> Unit,
  onManualSync: () -> Unit = {},
  modifier: Modifier = Modifier
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
  var showEditNameDialog by remember { mutableStateOf(false) }
  var unverifiedEmail by remember { mutableStateOf("") }
  var verificationMsg by remember { mutableStateOf("") }

  val bgColor = if (isNightMode) DarkBackground else LightPageBackground
  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val inputContainer = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val inputTextColor = if (isNightMode) Color.White else Color.Black
  val inputFocusedBorder = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputFocusedLabel = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputCursorColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputHandleColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)

  Surface(
    modifier = modifier.fillMaxSize(),
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
        // TOP APP BAR (Back Button)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 2.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(if (isNightMode) Color(0xFF282B33) else Color.White)
              .border(1.5.dp, if (isNightMode) Color(0xFF3E4452) else Color(0xFFEEF1F5), CircleShape)
              .clickable { onBackClick() }
              .testTag("account_back_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = "Back",
              tint = if (isNightMode) Color.White else Color(0xFF3A4150),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
        ) {
          // Left-aligned Headline
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp, bottom = 24.dp)
          ) {
            Text(
              text = "Account",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.W600,
                fontSize = 32.sp,
                letterSpacing = (-0.02).sp
              ),
              color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Manage your profile and sign-in.",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W500,
                fontSize = 16.sp
              ),
              color = mutedColor
            )
          }

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
                    color = Color(0xFFDCE5EE),
                    border = BorderStroke(1.dp, Color(0xFFC9D4E0))
                  ) {
                    Text(
                      text = successMessage!!,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = Color(0xFF1A1A1F),
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
                            containerColor = Color(0xFF1A1A1F),
                            contentColor = Color.White
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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

                    AccountPasswordRequirementsChecklist(
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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

                      AccountPasswordRequirementsChecklist(
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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
                        tint = Color(0xFF1A1A1F),
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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
                        containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                        contentColor = if (isNightMode) Color.Black else Color.White
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
                .clip(RoundedCornerShape(28.dp)),
              shape = RoundedCornerShape(28.dp),
              color = cardBg,
              border = BorderStroke(1.dp, borderColor)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                // USER PROFILE AVATAR
                val avatarLetter = if (userName.isNotBlank()) userName.trim().take(1).uppercase() else ""
                Box(
                  modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCE5EE)),
                  contentAlignment = Alignment.Center
                ) {
                  if (avatarLetter.isNotEmpty()) {
                    Text(
                      text = avatarLetter,
                      style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                      ),
                      color = Color(0xFF1A1A1F)
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Outlined.Person,
                      contentDescription = "Profile",
                      modifier = Modifier.size(38.dp),
                      tint = Color(0xFF1A1A1F)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 20.sp
                    ),
                    color = Color(0xFF1A1A1F)
                  )

                  Spacer(modifier = Modifier.width(8.dp))

                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(Color(0xFFEEF1F5))
                      .clickable {
                        showEditNameDialog = true
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Edit,
                      contentDescription = "Edit Name",
                      tint = Color(0xFF1A1A1F),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                if (userEmail.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = 15.sp,
                      fontWeight = FontWeight.W400
                    ),
                    color = Color(0xFF8E96A3),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                  )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STATUS TAG
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = Color(0xFFDCE5EE),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF1A1A1F),
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "Account Active & Synced",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 12.5.sp
                      ),
                      color = Color(0xFF1A1A1F)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // MANUAL SYNC BUTTON
                Button(
                  onClick = { onManualSync() },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("account_manual_sync_button"),
                  shape = CircleShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNightMode) Color(0xFF282B33) else Color(0xFFEEF1F5),
                    contentColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
                  )
                ) {
                  Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.Refresh,
                    contentDescription = "Sync",
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Sync Cloud Data",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.W600,
                      fontSize = 15.sp
                    )
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LOGOUT BUTTON
                Button(
                  onClick = { showLogoutConfirmation = true },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("account_screen_logout_button"),
                  shape = CircleShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
                    contentColor = if (isNightMode) Color(0xFF1A1A1F) else Color.White
                  )
                ) {
                  Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Sign Out",
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.W600,
                      fontSize = 15.sp
                    )
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

      // Edit Name Dialog
      if (showEditNameDialog) {
        EditNameDialog(
          currentName = userName,
          isNightMode = isNightMode,
          onDismiss = { showEditNameDialog = false },
          onSave = { newName ->
            showEditNameDialog = false
            onUpdateName(newName)
          }
        )
      }
    }
  }
}

@Composable
fun AccountPasswordRequirementsChecklist(
  passwordInput: String,
  isNightMode: Boolean
) {
  val reqLength = passwordInput.length >= 8
  val reqLetter = passwordInput.any { it.isLetter() }
  val reqNumber = passwordInput.any { it.isDigit() }

  val activeColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inactiveColor = if (isNightMode) DarkTextSecondary else LightTextSecondary

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, start = 4.dp, end = 4.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
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
      imageVector = if (isFulfilled) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
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

@Composable
private fun EditNameDialog(
  currentName: String,
  isNightMode: Boolean,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var nameInput by remember { mutableStateOf(currentName) }
  val isValid = nameInput.trim().isNotEmpty()

  val cardBg = if (isNightMode) DarkCardSurface else LightCardSurface
  val textColor = if (isNightMode) Color.White else LightTextPrimary
  val mutedColor = if (isNightMode) DarkTextSecondary else LightTextSecondary
  val borderColor = if (isNightMode) DarkSubtleBorder else LightSubtleBorder
  val inputContainer = if (isNightMode) DarkContainerNeutral else LightContainerNeutral
  val inputTextColor = if (isNightMode) Color.White else Color.Black
  val inputFocusedBorder = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputFocusedLabel = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputCursorColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)
  val inputHandleColor = if (isNightMode) Color.White else Color(0xFF1A1A1F)

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
          shape = RoundedCornerShape(24.dp),
          spotColor = if (isNightMode) Color.Transparent else Color(0x2E141E32),
          ambientColor = Color.Transparent
        )
        .clickable(enabled = false) {},
      shape = RoundedCornerShape(24.dp),
      color = cardBg,
      shadowElevation = 0.dp
    ) {
      Column(
        modifier = Modifier.padding(28.dp)
      ) {
        Text(
          text = "Edit Name",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Update your profile name below.",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.W400,
            fontSize = 15.sp
          ),
          color = mutedColor
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it.take(30) },
          label = { Text("Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_name_input"),
          shape = RoundedCornerShape(14.dp),
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

        Spacer(modifier = Modifier.height(24.dp))

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
              containerColor = if (isNightMode) Color(0xFF282B33) else Color(0xFFEEF1F5),
              contentColor = textColor
            )
          ) {
            Text(
              text = "Cancel",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp)
            )
          }

          Button(
            onClick = {
              val trimmed = nameInput.trim()
              if (trimmed.isNotEmpty()) {
                onSave(trimmed)
              }
            },
            enabled = isValid,
            modifier = Modifier
              .weight(1f)
              .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isNightMode) Color.White else Color(0xFF1A1A1F),
              contentColor = if (isNightMode) Color.Black else Color.White
            )
          ) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600, fontSize = 16.sp)
            )
          }
        }
      }
    }
  }
}
