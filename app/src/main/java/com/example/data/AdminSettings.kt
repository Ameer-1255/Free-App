package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class AdminConfig(
  val maxConcurrentDownloads: Int = 3,
  val defaultResolution: String = "1080p",
  val downloadWifiOnly: Boolean = false,
  val stripTikTokWatermark: Boolean = true,
  val enableYouTube: Boolean = true,
  val enableTikTok: Boolean = true,
  val enableInstagram: Boolean = true,
  val enableFacebook: Boolean = true,
  val enableMovieBox: Boolean = true,
  val snifferMp4: Boolean = true,
  val snifferM3u8: Boolean = true,
  val snifferMp3: Boolean = true,
  val userAgentMode: String = "Mobile Chrome",
  val isPinProtectionEnabled: Boolean = false,
  val adminPin: String = "1234"
)

class AdminPreferences(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("omnisave_admin_prefs", Context.MODE_PRIVATE)

  fun loadConfig(): AdminConfig {
    return AdminConfig(
      maxConcurrentDownloads = prefs.getInt("max_concurrent", 3),
      defaultResolution = prefs.getString("default_resolution", "1080p") ?: "1080p",
      downloadWifiOnly = prefs.getBoolean("wifi_only", false),
      stripTikTokWatermark = prefs.getBoolean("tiktok_watermark", true),
      enableYouTube = prefs.getBoolean("enable_yt", true),
      enableTikTok = prefs.getBoolean("enable_tt", true),
      enableInstagram = prefs.getBoolean("enable_ig", true),
      enableFacebook = prefs.getBoolean("enable_fb", true),
      enableMovieBox = prefs.getBoolean("enable_mb", true),
      snifferMp4 = prefs.getBoolean("sniffer_mp4", true),
      snifferM3u8 = prefs.getBoolean("sniffer_m3u8", true),
      snifferMp3 = prefs.getBoolean("sniffer_mp3", true),
      userAgentMode = prefs.getString("user_agent_mode", "Mobile Chrome") ?: "Mobile Chrome",
      isPinProtectionEnabled = prefs.getBoolean("pin_protection", false),
      adminPin = prefs.getString("admin_pin", "1234") ?: "1234"
    )
  }

  fun saveConfig(config: AdminConfig) {
    prefs.edit()
      .putInt("max_concurrent", config.maxConcurrentDownloads)
      .putString("default_resolution", config.defaultResolution)
      .putBoolean("wifi_only", config.downloadWifiOnly)
      .putBoolean("tiktok_watermark", config.stripTikTokWatermark)
      .putBoolean("enable_yt", config.enableYouTube)
      .putBoolean("enable_tt", config.enableTikTok)
      .putBoolean("enable_ig", config.enableInstagram)
      .putBoolean("enable_fb", config.enableFacebook)
      .putBoolean("enable_mb", config.enableMovieBox)
      .putBoolean("sniffer_mp4", config.snifferMp4)
      .putBoolean("sniffer_m3u8", config.snifferM3u8)
      .putBoolean("sniffer_mp3", config.snifferMp3)
      .putString("user_agent_mode", config.userAgentMode)
      .putBoolean("pin_protection", config.isPinProtectionEnabled)
      .putString("admin_pin", config.adminPin)
      .apply()
  }
}
