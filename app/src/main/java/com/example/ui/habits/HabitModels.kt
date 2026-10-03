package com.example.ui.habits

import org.json.JSONObject
import java.util.UUID
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

data class HabitItem(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val colorHex: String = "#8B7FD6",
  val startDateStr: String,
  val isArchived: Boolean = false,
  val remindersEnabled: Boolean = true,
  val createdAtMillis: Long = System.currentTimeMillis()
) {
  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("id", id)
      put("name", name)
      put("colorHex", colorHex)
      put("startDateStr", startDateStr)
      put("isArchived", isArchived)
      put("remindersEnabled", remindersEnabled)
      put("createdAtMillis", createdAtMillis)
    }
  }

  companion object {
    private val APPROVED_COLORS = listOf(
      "#8B7FD6", "#4AC1D6", "#F0716B", "#4A9FE0", "#3E9C8F", "#D6B34A", "#D67F9E"
    )

    fun fromJson(json: JSONObject): HabitItem {
      val now = System.currentTimeMillis()
      // If the habit already has an ID but no creation date, it's an old habit that migrated.
      // We default it to 30 days ago so the 5-day window shows marks instead of being blank.
      val createdAt = if (json.has("createdAtMillis")) {
        json.getLong("createdAtMillis")
      } else if (json.has("id")) {
        now - (30L * 24 * 60 * 60 * 1000)
      } else {
        now
      }
      
      val defaultStart = java.time.Instant.ofEpochMilli(createdAt)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()

      var color = json.optString("colorHex", "#8B7FD6").uppercase()
      if (!APPROVED_COLORS.contains(color)) {
        // Migration: map old colors to closest new ones
        color = when (color) {
          "#22C55E", "#C8D23A", "#4ADE80" -> "#D67F9E" // Greenish to Rose
          "#3B82F6", "#60A5FA" -> "#4A9FE0"           // Blues to Sky Blue
          "#EF4444", "#F87171" -> "#F0716B"           // Reds to Coral
          "#A855F7", "#C084FC" -> "#8B7FD6"           // Purples
          "#F59E0B", "#FBBF24" -> "#D6B34A"           // Yellows
          else -> "#8B7FD6"                           // Default to Purple
        }
      }

      return HabitItem(
        id = json.optString("id", UUID.randomUUID().toString()),
        name = json.optString("name", "New Habit"),
        colorHex = color,
        startDateStr = json.optString("startDateStr", "").ifBlank { defaultStart },
        isArchived = json.optBoolean("isArchived", false),
        remindersEnabled = json.optBoolean("remindersEnabled", true),
        createdAtMillis = createdAt
      )
    }
  }
}
