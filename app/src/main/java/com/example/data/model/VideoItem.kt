package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VideoPlatform(val displayName: String, val identifier: String) {
  YOUTUBE("YouTube", "youtube"),
  FACEBOOK("Facebook", "facebook"),
  INSTAGRAM("Instagram", "instagram"),
  TIKTOK("TikTok", "tiktok"),
  MOVIEBOX("MovieBox", "moviebox"),
  DIRECT("Direct Web", "direct")
}

enum class DownloadStatus {
  QUEUED,
  DOWNLOADING,
  COMPLETED,
  PAUSED,
  FAILED
}

@Entity(tableName = "videos")
data class VideoItem(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val sourceUrl: String,
  val platform: String, // from VideoPlatform.name
  val localFilePath: String,
  val thumbnailUrl: String,
  val durationSeconds: Long = 0,
  val fileSizeBytes: Long = 0,
  val quality: String = "720p HD",
  val mimeType: String = "video/mp4",
  val downloadStatus: String = DownloadStatus.COMPLETED.name,
  val downloadProgress: Float = 100f,
  val downloadSpeed: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val isFavorite: Boolean = false,
  val notes: String = ""
)
