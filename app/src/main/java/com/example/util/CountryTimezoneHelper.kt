package com.example.util

import java.util.TimeZone

data class CountryTimezone(
  val label: String,
  val timezoneId: String
) {
  val formattedOffset: String
    get() {
      if (timezoneId == "DEVICE_DEFAULT") return "Device System Timezone"
      val tz = TimeZone.getTimeZone(timezoneId)
      val offsetMillis = tz.getOffset(System.currentTimeMillis())
      val totalMinutes = offsetMillis / (1000 * 60)
      val hours = totalMinutes / 60
      val minutes = Math.abs(totalMinutes % 60)
      val sign = if (hours >= 0) "+" else "-"
      val absHours = Math.abs(hours)
      val hoursStr = if (absHours < 10) "0$absHours" else "$absHours"
      val minsStr = if (minutes < 10) "0$minutes" else "$minutes"
      return "UTC$sign$hoursStr:$minsStr"
    }
}

data class CountryGroup(
  val countryName: String,
  val timezones: List<CountryTimezone>
)

object CountryTimezoneHelper {
  val ALL_COUNTRIES = listOf(
    CountryGroup("Afghanistan", listOf(CountryTimezone("Kabul", "Asia/Kabul"))),
    CountryGroup("Argentina", listOf(CountryTimezone("Buenos Aires", "America/Argentina/Buenos_Aires"))),
    CountryGroup("Australia", listOf(
      CountryTimezone("Sydney", "Australia/Sydney"),
      CountryTimezone("Melbourne", "Australia/Melbourne"),
      CountryTimezone("Brisbane", "Australia/Brisbane"),
      CountryTimezone("Perth", "Australia/Perth"),
      CountryTimezone("Adelaide", "Australia/Adelaide"),
      CountryTimezone("Darwin", "Australia/Darwin"),
      CountryTimezone("Hobart", "Australia/Hobart")
    )),
    CountryGroup("Bangladesh", listOf(CountryTimezone("Dhaka", "Asia/Dhaka"))),
    CountryGroup("Brazil", listOf(
      CountryTimezone("Sao Paulo", "America/Sao_Paulo"),
      CountryTimezone("Rio de Janeiro", "America/Sao_Paulo"),
      CountryTimezone("Brasilia", "America/Sao_Paulo"),
      CountryTimezone("Manaus", "America/Manaus"),
      CountryTimezone("Cuiaba", "America/Cuiaba")
    )),
    CountryGroup("Canada", listOf(
      CountryTimezone("Eastern Time (Toronto)", "America/Toronto"),
      CountryTimezone("Pacific Time (Vancouver)", "America/Vancouver"),
      CountryTimezone("Mountain Time (Edmonton)", "America/Edmonton"),
      CountryTimezone("Central Time (Winnipeg)", "America/Winnipeg"),
      CountryTimezone("Atlantic Time (Halifax)", "America/Halifax"),
      CountryTimezone("Newfoundland Time (St. John's)", "America/St_Johns")
    )),
    CountryGroup("China", listOf(CountryTimezone("Beijing / Shanghai", "Asia/Shanghai"))),
    CountryGroup("Egypt", listOf(CountryTimezone("Cairo", "Africa/Cairo"))),
    CountryGroup("France", listOf(CountryTimezone("Paris", "Europe/Paris"))),
    CountryGroup("Germany", listOf(CountryTimezone("Berlin", "Europe/Berlin"))),
    CountryGroup("India", listOf(CountryTimezone("Kolkata", "Asia/Kolkata"))),
    CountryGroup("Indonesia", listOf(
      CountryTimezone("Jakarta (Western)", "Asia/Jakarta"),
      CountryTimezone("Makassar (Central)", "Asia/Makassar"),
      CountryTimezone("Jayapura (Eastern)", "Asia/Jayapura")
    )),
    CountryGroup("Italy", listOf(CountryTimezone("Rome", "Europe/Rome"))),
    CountryGroup("Japan", listOf(CountryTimezone("Tokyo", "Asia/Tokyo"))),
    CountryGroup("Mexico", listOf(
      CountryTimezone("Mexico City", "America/Mexico_City"),
      CountryTimezone("Cancun", "America/Cancun"),
      CountryTimezone("Monterrey", "America/Monterrey"),
      CountryTimezone("Tijuana", "America/Tijuana")
    )),
    CountryGroup("Nigeria", listOf(CountryTimezone("Lagos", "Africa/Lagos"))),
    CountryGroup("Pakistan", listOf(CountryTimezone("Karachi", "Asia/Karachi"))),
    CountryGroup("Philippines", listOf(CountryTimezone("Manila", "Asia/Manila"))),
    CountryGroup("Russia", listOf(
      CountryTimezone("Moscow", "Europe/Moscow"),
      CountryTimezone("St. Petersburg", "Europe/Moscow"),
      CountryTimezone("Yekaterinburg", "Asia/Yekaterinburg"),
      CountryTimezone("Novosibirsk", "Asia/Novosibirsk"),
      CountryTimezone("Vladivostok", "Asia/Vladivostok"),
      CountryTimezone("Kaliningrad", "Europe/Kaliningrad")
    )),
    CountryGroup("Saudi Arabia", listOf(CountryTimezone("Riyadh", "Asia/Riyadh"))),
    CountryGroup("Singapore", listOf(CountryTimezone("Singapore", "Asia/Singapore"))),
    CountryGroup("South Africa", listOf(CountryTimezone("Johannesburg", "Africa/Johannesburg"))),
    CountryGroup("South Korea", listOf(CountryTimezone("Seoul", "Asia/Seoul"))),
    CountryGroup("Spain", listOf(CountryTimezone("Madrid", "Europe/Madrid"))),
    CountryGroup("Turkey", listOf(CountryTimezone("Istanbul", "Europe/Istanbul"))),
    CountryGroup("United Arab Emirates", listOf(CountryTimezone("Dubai", "Asia/Dubai"))),
    CountryGroup("United Kingdom", listOf(CountryTimezone("London", "Europe/London"))),
    CountryGroup("United States", listOf(
      CountryTimezone("Eastern Time (New York)", "America/New_York"),
      CountryTimezone("Central Time (Chicago)", "America/Chicago"),
      CountryTimezone("Mountain Time (Denver)", "America/Denver"),
      CountryTimezone("Pacific Time (Los Angeles)", "America/Los_Angeles"),
      CountryTimezone("Alaska Time", "America/Anchorage"),
      CountryTimezone("Hawaii Time", "Pacific/Honolulu")
    )),
    CountryGroup("Vietnam", listOf(CountryTimezone("Ho Chi Minh", "Asia/Ho_Chi_Minh")))
  )
}
