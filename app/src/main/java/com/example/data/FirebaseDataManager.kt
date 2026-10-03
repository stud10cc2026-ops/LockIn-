package com.example.data

import android.util.Log
import com.example.ui.home.AppBlockItem
import com.example.ui.home.FocusSessionHistoryItem
import com.example.ui.home.HomeUiState
import com.example.ui.notifications.AppNotificationItem
import com.example.ui.habits.HabitItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class FirebaseDataManager {
    private val db by lazy { Firebase.firestore }

    init {
        // We will configure settings when first accessed or in init if Firebase is ready
    }

    private fun ensureSettings() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "FirebaseDataManager"
    }

    data class UserCloudData(
        val userName: String? = null,
        val userEmail: String? = null,
        val historyItems: List<FocusSessionHistoryItem>? = null,
        val habits: List<HabitItem>? = null,
        val habitLogs: Map<String, Map<String, Boolean>>? = null,
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
    fun saveUserData(userId: String, idToken: String?, state: HomeUiState, habits: List<HabitItem>, habitLogs: Map<String, Map<String, Boolean>>) {
        if (userId.isBlank()) return
        ensureSettings()
        val userRef = db.collection("users").document(userId)

        // 1. Profile and Settings
        val profileData = mapOf(
            "userName" to state.userName,
            "userEmail" to state.userEmail,
            "selectedDurationMinutes" to state.selectedDurationMinutes,
            "isCustomDuration" to state.isCustomDuration,
            "customMinutes" to state.customMinutes,
            "isNightMode" to state.isNightMode,
            "defaultDurationMinutes" to state.defaultDurationMinutes,
            "allowPause" to state.allowPause,
            "focusReminders" to state.focusReminders,
            "notificationsEnabled" to state.notificationsEnabled,
            "updatedAt" to System.currentTimeMillis()
        )
        userRef.set(profileData, SetOptions.merge())

        // 2. Habits (Subcollection)
        habits.forEach { habit ->
            val habitLogsForThisHabit = mutableListOf<String>()
            habitLogs.forEach { (date, map) ->
                if (map[habit.id] == true) habitLogsForThisHabit.add(date)
            }
            
            val habitData = mapOf(
                "name" to habit.name,
                "colorHex" to habit.colorHex,
                "startDateStr" to habit.startDateStr,
                "isArchived" to habit.isArchived,
                "remindersEnabled" to habit.remindersEnabled,
                "createdAtMillis" to habit.createdAtMillis,
                "completedDates" to habitLogsForThisHabit,
                "updatedAt" to System.currentTimeMillis()
            )
            userRef.collection("habits").document(habit.id).set(habitData, SetOptions.merge())
        }

        // 3. Sessions (Subcollection)
        state.historyItems.forEach { session ->
            val sessionData = mapOf(
                "durationMinutes" to session.durationMinutes,
                "selectedDurationMinutes" to session.selectedDurationMinutes,
                "sessionType" to session.sessionType,
                "timestampFormatted" to session.timestampFormatted,
                "dateGroup" to session.dateGroup,
                "isCompleted" to session.isCompleted,
                "timestampMillis" to session.timestampMillis
            )
            userRef.collection("sessions").document(session.id).set(sessionData, SetOptions.merge())
        }

        // 4. Stats Summary
        val summaryData = mapOf(
            "totalFocusTime" to state.todayFocusFormatted, // This is a bit simplified, but requested
            "sessionsCount" to state.todaySessionsCount,
            "updatedAt" to System.currentTimeMillis()
        )
        userRef.collection("stats").document("summary").set(summaryData, SetOptions.merge())
    }

    /**
     * Migrate local guest data to Firestore after Sign Up
     */
    suspend fun migrateGuestData(userId: String, idToken: String?, state: HomeUiState, habits: List<HabitItem>, habitLogs: Map<String, Map<String, Boolean>>) {
        Log.i(TAG, "Migrating guest data to UID: $userId")
        saveUserData(userId, idToken, state, habits, habitLogs)
    }

    /**
     * Fetch user data from Firestore
     */
    suspend fun loadUserData(userId: String, idToken: String?): UserCloudData? {
        if (userId.isBlank()) return null
        ensureSettings()
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        Log.d(TAG, "Attempting to load user data for UID: $userId (Auth UID: ${authUser?.uid})")
        
        try {
            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                Log.i(TAG, "No Firestore document found for user: $userId")
                return null
            }

            val profile = userSnap.data ?: emptyMap()
            Log.d(TAG, "User document found. Loading subcollections for $userId")
            
            // Load Habits
            val habitsSnap = userRef.collection("habits").get().await()
            val habitsList = mutableListOf<HabitItem>()
            val habitLogsMap = mutableMapOf<String, MutableMap<String, Boolean>>()
            
            habitsSnap.documents.forEach { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: "Unnamed"
                val colorHex = doc.getString("colorHex") ?: "#8B7FD6"
                val startDateStr = doc.getString("startDateStr") ?: ""
                val isArchived = doc.getBoolean("isArchived") ?: false
                val remindersEnabled = doc.getBoolean("remindersEnabled") ?: true
                val createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                
                habitsList.add(HabitItem(id, name, colorHex, startDateStr, isArchived, remindersEnabled, createdAtMillis))
                
                val completedDates = doc.get("completedDates") as? List<String> ?: emptyList()
                completedDates.forEach { date ->
                    val dateMap = habitLogsMap.getOrPut(date) { mutableMapOf() }
                    dateMap[id] = true
                }
            }

            // Load Sessions
            val sessionsSnap = userRef.collection("sessions").get().await()
            val historyItems = sessionsSnap.documents.map { doc ->
                FocusSessionHistoryItem(
                    id = doc.id,
                    durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 0,
                    selectedDurationMinutes = doc.getLong("selectedDurationMinutes")?.toInt() ?: 0,
                    sessionType = doc.getString("sessionType") ?: "Focus session",
                    timestampFormatted = doc.getString("timestampFormatted") ?: "",
                    dateGroup = doc.getString("dateGroup") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: true,
                    timestampMillis = doc.getLong("timestampMillis") ?: 0L
                )
            }.sortedByDescending { it.timestampMillis }

            Log.i(TAG, "Successfully loaded cloud data for $userId: ${habitsList.size} habits, ${historyItems.size} sessions")

            return UserCloudData(
                userName = profile["userName"] as? String,
                userEmail = profile["userEmail"] as? String,
                historyItems = historyItems,
                habits = habitsList,
                habitLogs = habitLogsMap,
                selectedDurationMinutes = (profile["selectedDurationMinutes"] as? Long)?.toInt(),
                isCustomDuration = profile["isCustomDuration"] as? Boolean,
                customMinutes = (profile["customMinutes"] as? Long)?.toInt(),
                isNightMode = profile["isNightMode"] as? Boolean,
                defaultDurationMinutes = (profile["defaultDurationMinutes"] as? Long)?.toInt(),
                allowPause = profile["allowPause"] as? Boolean,
                focusReminders = profile["focusReminders"] as? Boolean,
                notificationsEnabled = profile["notificationsEnabled"] as? Boolean
            )
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Unknown Firestore error"
            Log.e(TAG, "CRITICAL: Error loading user data for $userId: $errorMsg")
            if (errorMsg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                // Re-throw or return a specific error indicator so HomeViewModel knows NOT to overwrite
                throw e
            }
            return null
        }
    }
}
