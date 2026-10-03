package com.example.data

import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
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
  val createdAtMillis: Long = System.currentTimeMillis(),
  val ratings: Map<Int, Int> = emptyMap(), // DayIndex (1-based) to Rating (0: Bad, 1: Average, 2: Excellent)
  val weights: Map<String, Double> = emptyMap() // "yyyy-MM-dd" to weight value
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

  fun getStartDate(): LocalDate = try { LocalDate.parse(startDateStr) } catch (_: Exception) { LocalDate.now() }
  fun getEndDate(): LocalDate = try { LocalDate.parse(endDateStr) } catch (_: Exception) { LocalDate.now().plusDays(totalDays.toLong()) }

  fun isDateWithinGoal(date: LocalDate): Boolean {
    val start = getStartDate()
    val end = getEndDate()
    return !date.isBefore(start) && !date.isAfter(end)
  }

  fun isDatePassed(date: LocalDate, today: LocalDate = LocalDate.now()): Boolean {
    // If date is today or before today, it's considered "passed" for the visual grid
    return !date.isAfter(today)
  }

  fun getGridMonths(): List<YearMonth> {
    val start = getStartDate().withDayOfMonth(1)
    val end = getEndDate().withDayOfMonth(1)
    val months = mutableListOf<YearMonth>()
    var current = start
    while (!current.isAfter(end)) {
      months.add(YearMonth.from(current))
      current = current.plusMonths(1)
    }
    return months
  }

  fun withRating(dayIndex: Int, rating: Int): DailyGoal {
    val newRatings = ratings.toMutableMap()
    newRatings[dayIndex] = rating
    return this.copy(ratings = newRatings)
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
      
      val ratingsObj = JSONObject()
      ratings.forEach { (day, rating) ->
        ratingsObj.put(day.toString(), rating)
      }
      put("ratings", ratingsObj)

      val weightsObj = JSONObject()
      weights.forEach { (date, weight) ->
        weightsObj.put(date, weight)
      }
      put("weights", weightsObj)
    }
  }

  companion object {
    fun fromJson(json: JSONObject): DailyGoal {
      val ratings = mutableMapOf<Int, Int>()
      val ratingsObj = json.optJSONObject("ratings")
      if (ratingsObj != null) {
        val keys = ratingsObj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          ratings[key.toInt()] = ratingsObj.getInt(key)
        }
      }

      val weights = mutableMapOf<String, Double>()
      val weightsObj = json.optJSONObject("weights")
      if (weightsObj != null) {
        val keys = weightsObj.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          weights[key] = weightsObj.getDouble(key)
        }
      }

      return DailyGoal(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", "Daily Goal"),
        durationType = json.optString("durationType", "1_MONTH"),
        durationLabel = json.optString("durationLabel", "1 Month"),
        totalDays = json.optInt("totalDays", 30),
        startDateStr = json.optString("startDateStr", LocalDate.now().toString()),
        endDateStr = json.optString("endDateStr", LocalDate.now().plusDays(30).toString()),
        createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis()),
        ratings = ratings,
        weights = weights
      )
    }

    fun create(
      title: String = "Daily Goal",
      totalDaysInput: Int? = null,
      months: Int? = null,
      weeks: Int? = null,
      labelInput: String? = null,
      durationTypeInput: String = "CUSTOM",
      today: LocalDate = LocalDate.now()
    ): DailyGoal {
      val endDate = when {
        months != null -> {
          var totalDaysInPeriod = 0L
          var tempDate = today
          repeat(months) {
            totalDaysInPeriod += tempDate.lengthOfMonth()
            tempDate = tempDate.plusMonths(1)
          }
          today.plusDays(totalDaysInPeriod)
        }
        weeks != null -> today.plusWeeks(weeks.toLong())
        totalDaysInput != null -> today.plusDays(totalDaysInput.toLong())
        else -> today.plusDays(30)
      }
      val totalDays = ChronoUnit.DAYS.between(today, endDate).toInt().coerceIn(1, 365)
      
      val label = labelInput ?: when {
        months != null -> if (months == 1) "1 Month" else "$months Months"
        weeks != null -> if (weeks == 1) "1 Week" else "$weeks Weeks"
        else -> "$totalDays Days"
      }

      return DailyGoal(
        id = UUID.randomUUID().toString(),
        title = if (title.isNotBlank()) title.trim() else "Daily Goal",
        durationType = durationTypeInput,
        durationLabel = label,
        totalDays = totalDays,
        startDateStr = today.toString(),
        endDateStr = endDate.toString(),
        createdAtMillis = System.currentTimeMillis()
      )
    }

    fun create(durationType: String, today: LocalDate = LocalDate.now()): DailyGoal {
      return when (durationType) {
        "1_WEEK" -> create(weeks = 1, durationTypeInput = durationType, today = today)
        "2_WEEKS" -> create(weeks = 2, durationTypeInput = durationType, today = today)
        "1_MONTH" -> create(months = 1, durationTypeInput = durationType, today = today)
        "2_MONTHS" -> create(months = 2, durationTypeInput = durationType, today = today)
        "3_MONTHS" -> create(months = 3, durationTypeInput = durationType, today = today)
        "4_MONTHS" -> create(months = 4, durationTypeInput = durationType, today = today)
        "6_MONTHS" -> create(months = 6, durationTypeInput = durationType, today = today)
        "1_YEAR" -> create(months = 12, durationTypeInput = durationType, today = today)
        else -> create(months = 1, durationTypeInput = "1_MONTH", today = today)
      }
    }
  }
}
