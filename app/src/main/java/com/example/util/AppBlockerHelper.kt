package com.example.util

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.data.AppPreferences

object AppBlockerHelper {

  fun isUsageStatsPermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
      )
    } else {
      @Suppress("DEPRECATION")
      appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
      )
    }
    return mode == AppOpsManager.MODE_ALLOWED
  }

  fun isOverlayPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Settings.canDrawOverlays(context)
    } else {
      true
    }
  }

  fun openUsageAccessSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val intent = Intent(Settings.ACTION_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
    }
  }

  fun openOverlaySettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        val intent = Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:${context.packageName}")
        ).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
      } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
      }
    }
  }

  fun getForegroundPackageName(context: Context): String? {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 10000L

    val events = usageStatsManager.queryEvents(startTime, endTime)
    var lastForegroundPackage: String? = null
    var maxEventTime = 0L

    if (events != null) {
      val event = UsageEvents.Event()
      while (events.hasNextEvent()) {
        events.getNextEvent(event)
        val type = event.eventType
        if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
          if (event.timeStamp >= maxEventTime) {
            maxEventTime = event.timeStamp
            lastForegroundPackage = event.packageName
          }
        }
      }
    }

    if (!lastForegroundPackage.isNullOrEmpty()) {
      return lastForegroundPackage
    }

    try {
      val statsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        endTime - 60000L,
        endTime
      )
      if (!statsList.isNullOrEmpty()) {
        val mostRecent = statsList.maxByOrNull { it.lastTimeUsed }
        if (mostRecent != null && (endTime - mostRecent.lastTimeUsed) < 60000L) {
          return mostRecent.packageName
        }
      }
    } catch (_: Exception) {}

    return null
  }

  fun isLockInInForeground(context: Context): Boolean {
    if (com.example.MainActivity.isResumedState) {
      return true
    }
    val fgPkg = getForegroundPackageName(context)?.lowercase() ?: return false
    val pkgName = context.packageName.lowercase()
    return fgPkg == pkgName || fgPkg.startsWith("com.example") || fgPkg.contains("lockin")
  }

  /**
   * Checks if the given foreground package should be blocked under strict Focus Session whitelist.
   * Allowed: Lock In app itself, OS essential/launcher components, Phone/Dialer, WhatsApp, imo, and Android Settings.
   * Blocked: All other non-whitelisted apps (Chrome, YouTube, Instagram, TikTok, Facebook, Snapchat, Reddit, Twitter, Gallery, Games, etc.)
   * Returns the app's display name if blocked, or null if allowed.
   */
  fun getBlockedAppName(context: Context, packageName: String): String? {
    val pkg = packageName.lowercase()

    // 1. Always allow Lock In app itself
    if (pkg == context.packageName.lowercase() || pkg.startsWith("com.example") || pkg.contains("lockin")) {
      return null
    }

    // 2. Always allow critical system/launcher components for Android safety
    if (isSystemOrLauncherPackage(pkg)) {
      return null
    }

    // 3. STRICT WHITELIST: Allow Phone / Dialer, WhatsApp, imo, and Android Settings
    val isWhitelisted = when {
      // Phone / Dialer / Calls / Contacts
      pkg.contains("dialer") ||
      pkg.contains("phone") ||
      pkg.contains("telecom") ||
      pkg.contains("telephony") ||
      pkg.contains("incallui") -> true

      // WhatsApp
      pkg.contains("whatsapp") -> true

      // imo
      pkg.contains("imo") || pkg.contains("imoim") -> true

      // Android Settings (com.android.settings, com.google.android.settings, etc.)
      pkg.contains("settings") ||
      pkg.contains("permissioncontroller") ||
      pkg == "com.android.settings" ||
      pkg == "com.google.android.settings" -> true

      else -> false
    }

    if (isWhitelisted) {
      return null // App is allowed during Focus Session
    }

    // 4. Everything else is BLOCKED during Focus Session
    return when {
      pkg.contains("chrome") -> "Google Chrome"
      pkg.contains("youtube") -> "YouTube"
      pkg.contains("instagram") -> "Instagram"
      pkg.contains("musically") || pkg.contains("tiktok") || pkg.contains("trill") -> "TikTok"
      pkg.contains("facebook") || pkg.contains("katana") -> "Facebook"
      pkg.contains("snapchat") -> "Snapchat"
      pkg.contains("reddit") -> "Reddit"
      pkg.contains("twitter") || pkg.contains("x.android") -> "X (Twitter)"
      pkg.contains("gallery") || pkg.contains("photos") -> "Gallery / Photos"
      pkg.contains("twitch") -> "Twitch"
      pkg.contains("pinterest") -> "Pinterest"
      pkg.contains("browser") -> "Web Browser"
      else -> {
        try {
          val pm = context.packageManager
          val info = pm.getApplicationInfo(packageName, 0)
          pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
          "App"
        }
      }
    }
  }

  private fun isSystemOrLauncherPackage(pkg: String): Boolean {
    return pkg.contains("inputmethod") ||
           pkg.contains("keyboard") ||
           pkg.contains("systemui") ||
           pkg.contains("launcher") ||
           pkg.contains("nexuslauncher") ||
           pkg.contains("trebuchet") ||
           pkg.contains("pixellauncher") ||
           pkg.contains("settings") ||
           pkg.contains("permissioncontroller") ||
           pkg.contains("com.android.systemui") ||
           pkg.contains("home") ||
           pkg.contains("recents") ||
           pkg.contains("quickstep") ||
           pkg.contains("googlequicksearchbox") ||
           pkg.contains("overview") ||
           pkg == "android"
  }
}
