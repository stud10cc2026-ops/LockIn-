package com.example.ui.notifications

import java.util.UUID

data class AppNotificationItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val timestampFormatted: String = formatRelativeNotificationTime(timestamp),
  val isRead: Boolean = false
)

fun formatRelativeNotificationTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
  if (timestamp <= 0L) return "Just now"
  val diffMillis = (now - timestamp).coerceAtLeast(0L)
  val diffSeconds = diffMillis / 1000
  val diffMinutes = diffSeconds / 60
  val diffHours = diffMinutes / 60
  val diffDays = diffHours / 24

  return when {
    diffMinutes < 1L -> "Just now"
    diffMinutes == 1L -> "1 minute ago"
    diffMinutes < 60L -> "$diffMinutes minutes ago"
    diffHours == 1L -> "1 hour ago"
    diffHours < 24L -> "$diffHours hours ago"
    diffDays == 1L -> "1 day ago"
    diffDays < 7L -> "$diffDays days ago"
    diffDays < 30L -> {
      val diffWeeks = diffDays / 7
      if (diffWeeks <= 1L) "1 week ago" else "$diffWeeks weeks ago"
    }
    diffDays < 365L -> {
      val diffMonths = diffDays / 30
      if (diffMonths <= 1L) "1 month ago" else "$diffMonths months ago"
    }
    else -> "Long time ago"
  }
}

data class PendingDeletedNotification(
  val item: AppNotificationItem,
  val index: Int
)
