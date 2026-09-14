package com.example.data

import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class DailyGoal(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "Daily Goal",
  val durationType: String, // "1_WEEK", "2_WEEKS", "1_MONTH", "2_MONTHS", "3_MONTHS", "6_MONTHS", "1_YEAR", "CUSTOM"
  val durationLabel: String, // "1 Week", "2 Weeks", "1 Month", etc.
  val totalDays: Int, // 7 to 365
  val startDateStr: String, // "yyyy-MM-dd"
  val endDateStr: String, // "yyyy-MM-dd"
  val createdAtMillis: Long = System.currentTimeMillis()
) {
  fun getCurrentDay(today: LocalDate = LocalDate.now()): Int {
    return try {
      val start = LocalDate.parse(startDateStr)
      val daysPassed = ChronoUnit.DAYS.between(start, today)
      if (daysPassed < 0) 1 else (daysPassed + 1).coerceAtMost(totalDays.toLong()).toInt()
    } catch (_: Exception) {
      1
    }
  }

  fun getRemainingDays(today: LocalDate = LocalDate.now()): Int {
    return try {
      val current = getCurrentDay(today)
      (totalDays - current).coerceAtLeast(0)
    } catch (_: Exception) {
      0
    }
  }

  fun isCompleted(today: LocalDate = LocalDate.now()): Boolean {
    return try {
      val end = LocalDate.parse(endDateStr)
      !today.isBefore(end) || (getCurrentDay(today) >= totalDays && getRemainingDays(today) == 0)
    } catch (_: Exception) {
      false
    }
  }

  fun getProgressFraction(today: LocalDate = LocalDate.now()): Float {
    if (totalDays <= 0) return 0f
    val current = getCurrentDay(today)
    return (current.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
  }

  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("id", id)
      put("title", title)
      put("durationType", durationType)
      put("durationLabel", durationLabel)
      put("totalDays", totalDays)
      put("startDateStr", startDateStr)
      put("endDateStr", endDateStr)
      put("createdAtMillis", createdAtMillis)
    }
  }

  companion object {
    fun fromJson(json: JSONObject): DailyGoal {
      return DailyGoal(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", "Daily Goal"),
        durationType = json.optString("durationType", "1_MONTH"),
        durationLabel = json.optString("durationLabel", "1 Month"),
        totalDays = json.optInt("totalDays", 30),
        startDateStr = json.optString("startDateStr", LocalDate.now().toString()),
        endDateStr = json.optString("endDateStr", LocalDate.now().plusDays(30).toString()),
        createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis())
      )
    }

    fun create(
      title: String = "Daily Goal",
      totalDaysInput: Int = 30,
      labelInput: String? = null,
      durationTypeInput: String = "CUSTOM"
    ): DailyGoal {
      val validDays = totalDaysInput.coerceIn(7, 365)
      val today = LocalDate.now()
      val endDate = today.plusDays(validDays.toLong())
      val label = labelInput ?: when (validDays) {
        7 -> "1 Week"
        14 -> "2 Weeks"
        30 -> "1 Month"
        60 -> "2 Months"
        90 -> "3 Months"
        180 -> "6 Months"
        365 -> "1 Year"
        else -> if (validDays % 30 == 0) "${validDays / 30} Months" else if (validDays % 7 == 0) "${validDays / 7} Weeks" else "$validDays Days"
      }
      return DailyGoal(
        id = UUID.randomUUID().toString(),
        title = if (title.isNotBlank()) title.trim() else "Daily Goal",
        durationType = durationTypeInput,
        durationLabel = label,
        totalDays = validDays,
        startDateStr = today.toString(),
        endDateStr = endDate.toString(),
        createdAtMillis = System.currentTimeMillis()
      )
    }

    fun create(durationType: String): DailyGoal {
      val (label, days) = when (durationType) {
        "1_WEEK" -> Pair("1 Week", 7)
        "2_WEEKS" -> Pair("2 Weeks", 14)
        "2_MONTHS" -> Pair("2 Months", 60)
        "3_MONTHS" -> Pair("3 Months", 90)
        "6_MONTHS" -> Pair("6 Months", 180)
        "1_YEAR" -> Pair("1 Year", 365)
        else -> Pair("1 Month", 30) // "1_MONTH"
      }
      return create(title = "Daily Goal", totalDaysInput = days, labelInput = label, durationTypeInput = durationType)
    }
  }
}
