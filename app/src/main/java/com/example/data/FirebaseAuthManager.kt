package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class FirebaseAuthManager {

  companion object {
    private const val TAG = "FirebaseAuth"
    private const val API_KEY = "AIzaSyCFByIoLPk96jDQt9QGQnRlk-tEaK0SE0o"
    private const val SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$API_KEY"
    private const val SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY"
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  data class AuthResult(
    val success: Boolean,
    val isEmailUnverified: Boolean = false,
    val userId: String? = null,
    val email: String? = null,
    val idToken: String? = null,
    val refreshToken: String? = null,
    val errorMessage: String? = null
  )

  suspend fun signUp(email: String, pass: String): AuthResult = suspendCancellableCoroutine { continuation ->
    Log.i(TAG, "[SIGNUP_START] Creating Firebase account for email: $email using SDK")
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, pass)
      .addOnSuccessListener { result ->
        val user = result.user
        if (user != null) {
          Log.i(TAG, "[SIGNUP_SUCCESS] Firebase account created: ${user.uid}")
          user.sendEmailVerification()
          user.getIdToken(false).addOnSuccessListener { tokenResult ->
             continuation.resume(AuthResult(
                success = true,
                isEmailUnverified = true,
                userId = user.uid,
                email = user.email,
                idToken = tokenResult.token,
                errorMessage = "We have sent you a verification email to ${user.email}. Please verify it and log in."
             ))
          }.addOnFailureListener {
             continuation.resume(AuthResult(success = true, isEmailUnverified = true, userId = user.uid, email = user.email))
          }
        } else {
          continuation.resume(AuthResult(success = false, errorMessage = "Failed to create user"))
        }
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "[SIGNUP_FAILURE] Error: ${e.localizedMessage}")
        continuation.resume(AuthResult(success = false, errorMessage = parseAuthError(e)))
      }
  }

  suspend fun signIn(email: String, pass: String): AuthResult = suspendCancellableCoroutine { continuation ->
    Log.i(TAG, "[LOGIN_START] Signing in Firebase for email: $email using SDK")
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, pass)
      .addOnSuccessListener { result ->
        val user = result.user
        if (user != null) {
          if (!user.isEmailVerified) {
            user.sendEmailVerification()
            Log.i(TAG, "[LOGIN_BLOCKED] Email not verified for ${user.email}")
            continuation.resume(AuthResult(
              success = false,
              isEmailUnverified = true,
              userId = user.uid,
              email = user.email,
              errorMessage = "We have sent you a verification email to ${user.email}. Please verify it and log in."
            ))
          } else {
            user.getIdToken(false).addOnSuccessListener { tokenResult ->
              continuation.resume(AuthResult(
                success = true,
                isEmailUnverified = false,
                userId = user.uid,
                email = user.email,
                idToken = tokenResult.token
              ))
            }.addOnFailureListener {
              continuation.resume(AuthResult(success = false, errorMessage = "Failed to get session token"))
            }
          }
        } else {
          continuation.resume(AuthResult(success = false, errorMessage = "Login failed"))
        }
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "[LOGIN_FAILURE] Error: ${e.localizedMessage}")
        continuation.resume(AuthResult(success = false, errorMessage = parseAuthError(e)))
      }
  }

  private fun parseAuthError(e: Exception): String {
    val msg = e.localizedMessage ?: "Authentication failed"
    return when {
      msg.contains("EMAIL_ALREADY_IN_USE", true) -> "User already exists. Please sign in"
      msg.contains("INVALID_EMAIL", true) -> "Please enter a valid email address"
      msg.contains("WEAK_PASSWORD", true) -> "Password is too weak"
      msg.contains("USER_NOT_FOUND", true) || msg.contains("INVALID_LOGIN_CREDENTIALS", true) -> "Email or password is incorrect"
      msg.contains("WRONG_PASSWORD", true) -> "Email or password is incorrect"
      else -> "Authentication failed. Please try again."
    }
  }

  fun sendEmailVerification(idToken: String): Boolean {
    val sendOobUrl = "https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$API_KEY"
    val jsonBody = JSONObject().apply {
      put("requestType", "VERIFY_EMAIL")
      put("idToken", idToken)
    }
    val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(sendOobUrl)
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    return try {
      client.newCall(request).execute().use { response ->
        response.isSuccessful
      }
    } catch (e: Exception) {
      Log.e(TAG, "[SEND_VERIFY_EXCEPTION] Error: ${e.localizedMessage}", e)
      false
    }
  }

  fun checkEmailVerified(idToken: String): Boolean {
    val lookupUrl = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$API_KEY"
    val jsonBody = JSONObject().apply {
      put("idToken", idToken)
    }
    val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(lookupUrl)
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    return try {
      client.newCall(request).execute().use { response ->
        val bodyString = response.body?.string() ?: ""
        if (response.isSuccessful) {
          val json = JSONObject(bodyString)
          val users = json.optJSONArray("users")
          if (users != null && users.length() > 0) {
            val userObj = users.getJSONObject(0)
            userObj.optBoolean("emailVerified", false)
          } else {
            false
          }
        } else {
          false
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "[LOOKUP_EXCEPTION] Error: ${e.localizedMessage}", e)
      false
    }
  }

  private fun parseSignUpError(responseBody: String): String {
    return try {
      val json = JSONObject(responseBody)
      val errorObj = json.optJSONObject("error")
      val rawMessage = errorObj?.optString("message", "") ?: ""
      if (rawMessage.contains("EMAIL_EXISTS")) {
        "User already exists. Please sign in"
      } else if (rawMessage.contains("INVALID_EMAIL")) {
        "Please enter a valid email address"
      } else {
        "User already exists. Please sign in"
      }
    } catch (_: Exception) {
      "User already exists. Please sign in"
    }
  }

  private fun parseSignInError(responseBody: String): String {
    return try {
      val json = JSONObject(responseBody)
      val errorObj = json.optJSONObject("error")
      val rawMessage = errorObj?.optString("message", "") ?: ""
      if (rawMessage.contains("INVALID_PASSWORD") ||
        rawMessage.contains("EMAIL_NOT_FOUND") ||
        rawMessage.contains("INVALID_LOGIN_CREDENTIALS") ||
        rawMessage.contains("USER_DISABLED") ||
        rawMessage.contains("INVALID_EMAIL")
      ) {
        "Email or password is incorrect"
      } else {
        "Email or password is incorrect"
      }
    } catch (_: Exception) {
      "Email or password is incorrect"
    }
  }

  fun ensureFirebaseInitialized(context: Context) {
    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        val options = FirebaseOptions.Builder()
          .setApiKey(API_KEY)
          .setApplicationId("1:856902017523:android:lockin")
          .setProjectId("lock-in-e6bed")
          .build()
        FirebaseApp.initializeApp(context.applicationContext, options)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing FirebaseApp: ${e.localizedMessage}")
    }
  }

  suspend fun sendPasswordResetEmail(context: Context, email: String): AuthResult =
    suspendCancellableCoroutine { continuation ->
      try {
        ensureFirebaseInitialized(context)
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
          .addOnSuccessListener {
            Log.i(TAG, "[PASSWORD_RESET_SUCCESS] Reset email sent to $email via FirebaseAuth SDK")
            if (continuation.isActive) {
              continuation.resume(
                AuthResult(
                  success = true,
                  errorMessage = "Password reset link sent to $email."
                )
              )
            }
          }
          .addOnFailureListener { exception ->
            Log.e(TAG, "[PASSWORD_RESET_FAILURE] Exception: ${exception.localizedMessage}", exception)
            val friendlyMsg = parsePasswordResetError(exception)
            if (continuation.isActive) {
              continuation.resume(
                AuthResult(
                  success = false,
                  errorMessage = friendlyMsg
                )
              )
            }
          }
      } catch (e: Exception) {
        Log.e(TAG, "[PASSWORD_RESET_EXCEPTION] Exception: ${e.localizedMessage}", e)
        val friendlyMsg = parsePasswordResetError(e)
        if (continuation.isActive) {
          continuation.resume(
            AuthResult(
              success = false,
              errorMessage = friendlyMsg
            )
          )
        }
      }
    }

  private fun parsePasswordResetError(e: Exception): String {
    val message = e.localizedMessage ?: ""
    return when (e) {
      is FirebaseAuthInvalidUserException -> {
        "No account found with this email address."
      }
      is FirebaseAuthInvalidCredentialsException -> {
        "Please enter a valid email address."
      }
      is FirebaseTooManyRequestsException -> {
        "Too many attempts. Please wait a few minutes and try again."
      }
      is FirebaseNetworkException -> {
        "Network error. Please check your internet connection and try again."
      }
      is FirebaseAuthException -> {
        when (e.errorCode) {
          "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
          "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
          "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a few minutes and try again."
          else -> e.localizedMessage ?: "Failed to send password reset email. Please try again."
        }
      }
      else -> {
        if (message.contains("USER_NOT_FOUND", ignoreCase = true) || message.contains("EMAIL_NOT_FOUND", ignoreCase = true) || message.contains("no user record", ignoreCase = true)) {
          "No account found with this email address."
        } else if (message.contains("INVALID_EMAIL", ignoreCase = true) || message.contains("badly formatted", ignoreCase = true)) {
          "Please enter a valid email address."
        } else if (message.contains("TOO_MANY_REQUESTS", ignoreCase = true) || message.contains("too-many-requests", ignoreCase = true)) {
          "Too many attempts. Please wait a few minutes and try again."
        } else if (message.contains("network", ignoreCase = true) || message.contains("connection", ignoreCase = true)) {
          "Network error. Please check your internet connection and try again."
        } else {
          "Failed to send password reset email. Please verify your email address and try again."
        }
      }
    }
  }
}
