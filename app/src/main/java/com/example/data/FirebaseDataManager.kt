package com.example.data

import android.util.Log
import com.example.ui.home.AppBlockItem
import com.example.ui.home.FocusSessionHistoryItem
import com.example.ui.home.HomeUiState
import com.example.ui.notifications.AppNotificationItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FirebaseDataManager {

  companion object {
    private const val TAG = "FirebaseDataManager"
    private const val API_KEY = "AIzaSyCFByIoLPk96jDQt9QGQnRlk-tEaK0SE0o"
    private const val PROJECT_ID = "lock-in-e6bed"

    private const val FIRESTORE_BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
    private const val RTDB_BASE_URL = "https://$PROJECT_ID-default-rtdb.firebaseio.com"
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  data class UserCloudData(
    val userName: String? = null,
    val userEmail: String? = null,
    val historyItems: List<FocusSessionHistoryItem>? = null,
    val appNotifications: List<AppNotificationItem>? = null,
    val selectedDurationMinutes: Int? = null,
    val isCustomDuration: Boolean? = null,
    val customMinutes: Int? = null,
    val isNightMode: Boolean? = null,
    val defaultDurationMinutes: Int? = null,
    val allowPause: Boolean? = null,
    val focusReminders: Boolean? = null,
    val notificationsEnabled: Boolean? = null,
    val apps: List<AppBlockItem>? = null,
    val dailyGoal: DailyGoal? = null
  )

  /**
   * Save user-specific data to Firebase under users/{uid}
   */
  fun saveUserData(userId: String, idToken: String?, state: HomeUiState): Boolean {
    if (userId.isBlank()) return false

    val profileObj = JSONObject().apply {
      put("userName", state.userName)
      put("userEmail", state.userEmail)
    }

    val historyArray = JSONArray().apply {
      state.historyItems.forEach { item ->
        put(JSONObject().apply {
          put("id", item.id)
          put("durationMinutes", item.durationMinutes)
          put("selectedDurationMinutes", item.selectedDurationMinutes)
          put("sessionType", item.sessionType)
          put("timestampFormatted", item.timestampFormatted)
          put("dateGroup", item.dateGroup)
          put("isCompleted", item.isCompleted)
          put("timestampMillis", item.timestampMillis)
        })
      }
    }

    val notificationsArray = JSONArray().apply {
      state.appNotifications.forEach { notif ->
        put(JSONObject().apply {
          put("id", notif.id)
          put("title", notif.title)
          put("message", notif.message)
          put("timestamp", notif.timestamp)
          put("isRead", notif.isRead)
        })
      }
    }

    val settingsObj = JSONObject().apply {
      put("selectedDurationMinutes", state.selectedDurationMinutes)
      put("isCustomDuration", state.isCustomDuration)
      put("customMinutes", state.customMinutes)
      put("isNightMode", state.isNightMode)
      put("defaultDurationMinutes", state.defaultDurationMinutes)
      put("allowPause", state.allowPause)
      put("focusReminders", state.focusReminders)
      put("notificationsEnabled", state.notificationsEnabled)
    }

    val dailyGoalJson = state.dailyGoal?.toJson()?.toString()

    val appsArray = JSONArray().apply {
      state.apps.forEach { app ->
        put(JSONObject().apply {
          put("id", app.id)
          put("name", app.name)
          put("category", app.category)
          put("isBlocked", app.isBlocked)
        })
      }
    }

    // Try Firestore REST
    val firestoreOk = saveToFirestore(userId, idToken, profileObj, historyArray, notificationsArray, settingsObj, appsArray, dailyGoalJson)
    if (firestoreOk) return true

    // Fallback: Realtime DB REST
    return saveToRealtimeDb(userId, idToken, profileObj, historyArray, notificationsArray, settingsObj, appsArray, dailyGoalJson)
  }

  private fun saveToFirestore(
    userId: String,
    idToken: String?,
    profileObj: JSONObject,
    historyArray: JSONArray,
    notificationsArray: JSONArray,
    settingsObj: JSONObject,
    appsArray: JSONArray,
    dailyGoalJson: String?
  ): Boolean {
    val url = "$FIRESTORE_BASE_URL/users/$userId?key=$API_KEY"

    val fieldsObj = JSONObject().apply {
      put("userName", JSONObject().put("stringValue", profileObj.optString("userName")))
      put("userEmail", JSONObject().put("stringValue", profileObj.optString("userEmail")))
      put("historyJson", JSONObject().put("stringValue", historyArray.toString()))
      put("notificationsJson", JSONObject().put("stringValue", notificationsArray.toString()))
      put("settingsJson", JSONObject().put("stringValue", settingsObj.toString()))
      put("appsJson", JSONObject().put("stringValue", appsArray.toString()))
      if (dailyGoalJson != null) {
        put("dailyGoalJson", JSONObject().put("stringValue", dailyGoalJson))
      } else {
        // To clear the goal in Firestore, we can use a null value or just remove it if using PATCH with updateMask
        // But the current pattern doesn't seem to use updateMask. 
        // We'll use a special string or null if supported by their API implementation.
        // Actually, simple way is to store empty string if null.
        put("dailyGoalJson", JSONObject().put("stringValue", ""))
      }
      put("updatedAt", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
    }

    val bodyObj = JSONObject().apply {
      put("fields", fieldsObj)
    }

    val reqBuilder = Request.Builder()
      .url(url)
      .patch(bodyObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))

    if (!idToken.isNullOrEmpty()) {
      reqBuilder.addHeader("Authorization", "Bearer $idToken")
    }

    return try {
      client.newCall(reqBuilder.build()).execute().use { response ->
        if (response.isSuccessful) {
          Log.i(TAG, "[FIRESTORE_SAVE_SUCCESS] Saved user data for UID: $userId")
          true
        } else {
          Log.w(TAG, "[FIRESTORE_SAVE_WARN] Code ${response.code}: ${response.body?.string()}")
          false
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "[FIRESTORE_SAVE_ERROR] Exception: ${e.localizedMessage}")
      false
    }
  }

  private fun saveToRealtimeDb(
    userId: String,
    idToken: String?,
    profileObj: JSONObject,
    historyArray: JSONArray,
    notificationsArray: JSONArray,
    settingsObj: JSONObject,
    appsArray: JSONArray,
    dailyGoalJson: String?
  ): Boolean {
    val authParam = if (!idToken.isNullOrEmpty()) "?auth=$idToken" else "?key=$API_KEY"
    val url = "$RTDB_BASE_URL/users/$userId.json$authParam"

    val payload = JSONObject().apply {
      put("profile", profileObj)
      put("history", historyArray)
      put("notifications", notificationsArray)
      put("settings", settingsObj)
      put("apps", appsArray)
      put("dailyGoalJson", dailyGoalJson ?: "")
      put("updatedAt", System.currentTimeMillis())
    }

    val req = Request.Builder()
      .url(url)
      .put(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
      .build()

    return try {
      client.newCall(req).execute().use { response ->
        if (response.isSuccessful) {
          Log.i(TAG, "[RTDB_SAVE_SUCCESS] Saved user data for UID: $userId")
          true
        } else {
          Log.w(TAG, "[RTDB_SAVE_WARN] Code ${response.code}: ${response.body?.string()}")
          false
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "[RTDB_SAVE_ERROR] Exception: ${e.localizedMessage}")
      false
    }
  }

  /**
   * Load user-specific data from Firebase for users/{uid}
   */
  fun loadUserData(userId: String, idToken: String?): UserCloudData? {
    if (userId.isBlank()) return null

    // Try Firestore REST
    val firestoreData = loadFromFirestore(userId, idToken)
    if (firestoreData != null) return firestoreData

    // Fallback: Realtime DB REST
    return loadFromRealtimeDb(userId, idToken)
  }

  private fun loadFromFirestore(userId: String, idToken: String?): UserCloudData? {
    val url = "$FIRESTORE_BASE_URL/users/$userId?key=$API_KEY"
    val reqBuilder = Request.Builder().url(url).get()
    if (!idToken.isNullOrEmpty()) {
      reqBuilder.addHeader("Authorization", "Bearer $idToken")
    }

    return try {
      client.newCall(reqBuilder.build()).execute().use { response ->
        if (!response.isSuccessful) {
          Log.w(TAG, "[FIRESTORE_LOAD_WARN] Code ${response.code}")
          return null
        }
        val bodyStr = response.body?.string() ?: return null
        val json = JSONObject(bodyStr)
        val fields = json.optJSONObject("fields") ?: return null

        val userName = fields.optJSONObject("userName")?.optString("stringValue")
        val userEmail = fields.optJSONObject("userEmail")?.optString("stringValue")

        val historyItems = mutableListOf<FocusSessionHistoryItem>()
        val historyJsonStr = fields.optJSONObject("historyJson")?.optString("stringValue")
        if (!historyJsonStr.isNullOrEmpty()) {
          try {
            val arr = JSONArray(historyJsonStr)
            for (i in 0 until arr.length()) {
              val obj = arr.getJSONObject(i)
              historyItems.add(
                FocusSessionHistoryItem(
                  id = obj.getString("id"),
                  durationMinutes = obj.getInt("durationMinutes"),
                  selectedDurationMinutes = obj.optInt("selectedDurationMinutes", obj.getInt("durationMinutes")),
                  sessionType = obj.optString("sessionType", "Focus session"),
                  timestampFormatted = obj.getString("timestampFormatted"),
                  dateGroup = obj.getString("dateGroup"),
                  isCompleted = obj.getBoolean("isCompleted"),
                  timestampMillis = obj.optLong("timestampMillis", 0L)
                )
              )
            }
          } catch (e: Exception) {
            Log.e(TAG, "Error parsing historyJson: ${e.localizedMessage}")
          }
        }

        val appNotifications = mutableListOf<AppNotificationItem>()
        val notifJsonStr = fields.optJSONObject("notificationsJson")?.optString("stringValue")
        if (!notifJsonStr.isNullOrEmpty()) {
          try {
            val arr = JSONArray(notifJsonStr)
            for (i in 0 until arr.length()) {
              val obj = arr.getJSONObject(i)
              val ts = obj.optLong("timestamp", System.currentTimeMillis())
              appNotifications.add(
                AppNotificationItem(
                  id = obj.getString("id"),
                  title = obj.getString("title"),
                  message = obj.getString("message"),
                  timestamp = ts,
                  timestampFormatted = "",
                  isRead = obj.optBoolean("isRead", false)
                )
              )
            }
          } catch (e: Exception) {
            Log.e(TAG, "Error parsing notificationsJson: ${e.localizedMessage}")
          }
        }

        var selDur: Int? = null
        var isCust: Boolean? = null
        var custMins: Int? = null
        var nightMode: Boolean? = null
        var defDur: Int? = null
        var allowP: Boolean? = null
        var focusRem: Boolean? = null
        var notifEnabled: Boolean? = null

        val settingsJsonStr = fields.optJSONObject("settingsJson")?.optString("stringValue")
        if (!settingsJsonStr.isNullOrEmpty()) {
          try {
            val sObj = JSONObject(settingsJsonStr)
            if (sObj.has("selectedDurationMinutes")) selDur = sObj.getInt("selectedDurationMinutes")
            if (sObj.has("isCustomDuration")) isCust = sObj.getBoolean("isCustomDuration")
            if (sObj.has("customMinutes")) custMins = sObj.getInt("customMinutes")
            if (sObj.has("isNightMode")) nightMode = sObj.getBoolean("isNightMode")
            if (sObj.has("defaultDurationMinutes")) defDur = sObj.getInt("defaultDurationMinutes")
            if (sObj.has("allowPause")) allowP = sObj.getBoolean("allowPause")
            if (sObj.has("focusReminders")) focusRem = sObj.getBoolean("focusReminders")
            if (sObj.has("notificationsEnabled")) notifEnabled = sObj.getBoolean("notificationsEnabled")
          } catch (_: Exception) {}
        }

        val appsList = mutableListOf<AppBlockItem>()
        val appsJsonStr = fields.optJSONObject("appsJson")?.optString("stringValue")
        if (!appsJsonStr.isNullOrEmpty()) {
          try {
            val arr = JSONArray(appsJsonStr)
            for (i in 0 until arr.length()) {
              val obj = arr.getJSONObject(i)
              appsList.add(
                AppBlockItem(
                  id = obj.getString("id"),
                  name = obj.getString("name"),
                  category = obj.getString("category"),
                  isBlocked = obj.getBoolean("isBlocked")
                )
              )
            }
          } catch (_: Exception) {}
        }
        
        var dailyGoal: DailyGoal? = null
        val dailyGoalJsonStr = fields.optJSONObject("dailyGoalJson")?.optString("stringValue")
        if (!dailyGoalJsonStr.isNullOrEmpty()) {
          try {
            dailyGoal = DailyGoal.fromJson(JSONObject(dailyGoalJsonStr))
          } catch (_: Exception) {}
        }

        Log.i(TAG, "[FIRESTORE_LOAD_SUCCESS] Loaded data for UID: $userId")
        UserCloudData(
          userName = userName,
          userEmail = userEmail,
          historyItems = historyItems,
          appNotifications = appNotifications,
          selectedDurationMinutes = selDur,
          isCustomDuration = isCust,
          customMinutes = custMins,
          isNightMode = nightMode,
          defaultDurationMinutes = defDur,
          allowPause = allowP,
          focusReminders = focusRem,
          notificationsEnabled = notifEnabled,
          apps = if (appsList.isNotEmpty()) appsList else null,
          dailyGoal = dailyGoal
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "[FIRESTORE_LOAD_ERROR] Exception: ${e.localizedMessage}")
      null
    }
  }

  private fun loadFromRealtimeDb(userId: String, idToken: String?): UserCloudData? {
    val authParam = if (!idToken.isNullOrEmpty()) "?auth=$idToken" else "?key=$API_KEY"
    val url = "$RTDB_BASE_URL/users/$userId.json$authParam"

    val req = Request.Builder().url(url).get().build()

    return try {
      client.newCall(req).execute().use { response ->
        if (!response.isSuccessful) {
          Log.w(TAG, "[RTDB_LOAD_WARN] Code ${response.code}")
          return null
        }
        val bodyStr = response.body?.string() ?: return null
        if (bodyStr.trim() == "null") return null

        val json = JSONObject(bodyStr)
        val profileObj = json.optJSONObject("profile")
        val userName = profileObj?.optString("userName")
        val userEmail = profileObj?.optString("userEmail")

        val historyItems = mutableListOf<FocusSessionHistoryItem>()
        val historyArr = json.optJSONArray("history")
        if (historyArr != null) {
          for (i in 0 until historyArr.length()) {
            val obj = historyArr.getJSONObject(i)
            historyItems.add(
              FocusSessionHistoryItem(
                id = obj.getString("id"),
                durationMinutes = obj.getInt("durationMinutes"),
                selectedDurationMinutes = obj.optInt("selectedDurationMinutes", obj.getInt("durationMinutes")),
                sessionType = obj.optString("sessionType", "Focus session"),
                timestampFormatted = obj.getString("timestampFormatted"),
                dateGroup = obj.getString("dateGroup"),
                isCompleted = obj.getBoolean("isCompleted"),
                timestampMillis = obj.optLong("timestampMillis", 0L)
              )
            )
          }
        }

        val appNotifications = mutableListOf<AppNotificationItem>()
        val notifArr = json.optJSONArray("notifications")
        if (notifArr != null) {
          for (i in 0 until notifArr.length()) {
            val obj = notifArr.getJSONObject(i)
            val ts = obj.optLong("timestamp", System.currentTimeMillis())
            appNotifications.add(
              AppNotificationItem(
                id = obj.getString("id"),
                title = obj.getString("title"),
                message = obj.getString("message"),
                timestamp = ts,
                timestampFormatted = "",
                isRead = obj.optBoolean("isRead", false)
              )
            )
          }
        }

        var selDur: Int? = null
        var isCust: Boolean? = null
        var custMins: Int? = null
        var nightMode: Boolean? = null
        var defDur: Int? = null
        var allowP: Boolean? = null
        var focusRem: Boolean? = null
        var notifEnabled: Boolean? = null

        val sObj = json.optJSONObject("settings")
        if (sObj != null) {
          if (sObj.has("selectedDurationMinutes")) selDur = sObj.getInt("selectedDurationMinutes")
          if (sObj.has("isCustomDuration")) isCust = sObj.getBoolean("isCustomDuration")
          if (sObj.has("customMinutes")) custMins = sObj.getInt("customMinutes")
          if (sObj.has("isNightMode")) nightMode = sObj.getBoolean("isNightMode")
          if (sObj.has("defaultDurationMinutes")) defDur = sObj.getInt("defaultDurationMinutes")
          if (sObj.has("allowPause")) allowP = sObj.getBoolean("allowPause")
          if (sObj.has("focusReminders")) focusRem = sObj.getBoolean("focusReminders")
          if (sObj.has("notificationsEnabled")) notifEnabled = sObj.getBoolean("notificationsEnabled")
        }

        val appsList = mutableListOf<AppBlockItem>()
        val appsArr = json.optJSONArray("apps")
        if (appsArr != null) {
          for (i in 0 until appsArr.length()) {
            val obj = appsArr.getJSONObject(i)
            appsList.add(
              AppBlockItem(
                id = obj.getString("id"),
                name = obj.getString("name"),
                category = obj.getString("category"),
                isBlocked = obj.getBoolean("isBlocked")
              )
            )
          }
        }

        var dailyGoal: DailyGoal? = null
        val dailyGoalJsonStr = json.optString("dailyGoalJson")
        if (!dailyGoalJsonStr.isNullOrEmpty()) {
          try {
            dailyGoal = DailyGoal.fromJson(JSONObject(dailyGoalJsonStr))
          } catch (_: Exception) {}
        }

        Log.i(TAG, "[RTDB_LOAD_SUCCESS] Loaded data for UID: $userId")
        UserCloudData(
          userName = userName,
          userEmail = userEmail,
          historyItems = historyItems,
          appNotifications = appNotifications,
          selectedDurationMinutes = selDur,
          isCustomDuration = isCust,
          customMinutes = custMins,
          isNightMode = nightMode,
          defaultDurationMinutes = defDur,
          allowPause = allowP,
          focusReminders = focusRem,
          notificationsEnabled = notifEnabled,
          apps = if (appsList.isNotEmpty()) appsList else null,
          dailyGoal = dailyGoal
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "[RTDB_LOAD_ERROR] Exception: ${e.localizedMessage}")
      null
    }
  }
}
