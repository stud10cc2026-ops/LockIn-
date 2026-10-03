package com.example.ui.habits

import java.time.LocalDate
import java.time.YearMonth

fun calculateCurrentStreak(habitId: String, logs: Map<String, Map<String, Boolean>>, today: LocalDate): Int {
  var consecutiveDays = 0
  var current = today
  
  // If today is NOT done, check yesterday.
  // If yesterday is also NOT done, streak is 0.
  // If today is NOT done but yesterday IS done, streak is whatever it was up to yesterday.
  if (logs[current.toString()]?.get(habitId) != true) {
    current = current.minusDays(1)
  }
  
  while (logs[current.toString()]?.get(habitId) == true) {
    consecutiveDays++
    current = current.minusDays(1)
  }
  
  return if (consecutiveDays != 0 && consecutiveDays % 30 == 0) 30 else consecutiveDays % 30
}

fun calculateMonthDone(habitId: String, logs: Map<String, Map<String, Boolean>>, month: YearMonth): Int {
  var count = 0
  for (day in 1..month.lengthOfMonth()) {
    val date = month.atDay(day).toString()
    if (logs[date]?.get(habitId) == true) {
      count++
    }
  }
  return count
}

fun calculateYearDone(habitId: String, logs: Map<String, Map<String, Boolean>>, year: Int): Int {
  var count = 0
  logs.forEach { (dateStr, map) ->
    try {
      val date = LocalDate.parse(dateStr)
      if (date.year == year && map[habitId] == true) {
        count++
      }
    } catch (_: Exception) {}
  }
  return count
}
