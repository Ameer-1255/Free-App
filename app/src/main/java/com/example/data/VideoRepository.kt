package com.example.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(
  private val videoDao: VideoDao,
  private val context: Context
) {
  val allVideos: Flow<List<VideoItem>> = videoDao.getAllVideos()
  val completedVideos: Flow<List<VideoItem>> = videoDao.getCompletedVideos()
  val activeDownloads: Flow<List<VideoItem>> = videoDao.getActiveDownloads()

  fun getVideosByPlatform(platform: String): Flow<List<VideoItem>> {
    return videoDao.getCompletedVideosByPlatform(platform)
  }

  fun getVideoByIdFlow(id: Long): Flow<VideoItem?> {
    return videoDao.getVideoByIdFlow(id)
  }

  suspend fun getVideoById(id: Long): VideoItem? {
    return videoDao.getVideoById(id)
  }

  suspend fun insertVideo(video: VideoItem): Long {
    return videoDao.insertVideo(video)
  }

  suspend fun updateVideo(video: VideoItem) {
    videoDao.updateVideo(video)
  }

  suspend fun updateProgress(id: Long, progress: Float, status: DownloadStatus, speed: String, fileSize: Long) {
    videoDao.updateProgress(id, progress, status.name, speed, fileSize)
  }

  suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
    videoDao.updateFavorite(id, !currentFavorite)
  }

  suspend fun renameVideo(id: Long, newTitle: String) {
    videoDao.updateTitle(id, newTitle)
  }

  suspend fun deleteVideo(video: VideoItem) = withContext(Dispatchers.IO) {
    // Delete local media file if exists
    try {
      if (video.localFilePath.isNotEmpty()) {
        val file = File(video.localFilePath)
        if (file.exists()) {
          file.delete()
        }
      }
    } catch (_: Exception) { }
    videoDao.deleteVideoById(video.id)
  }

  suspend fun clearCompletedVideos() = withContext(Dispatchers.IO) {
    // Clean files
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    dir.listFiles()?.forEach { it.delete() }
    videoDao.deleteAllCompletedVideos()
  }

  fun getStorageInfo(): StorageStats {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    var usedBytes = 0L
    dir.listFiles()?.forEach { file ->
      if (file.isFile) {
        usedBytes += file.length()
      }
    }

    val stat = StatFs(dir.path)
    val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
    val totalBytes = stat.blockCountLong * stat.blockSizeLong

    return StorageStats(
      usedBytes = usedBytes,
      availableBytes = availableBytes,
      totalBytes = totalBytes
    )
  }
}

data class StorageStats(
  val usedBytes: Long,
  val availableBytes: Long,
  val totalBytes: Long
) {
  val formattedUsed: String
    get() = formatBytes(usedBytes)

  val formattedAvailable: String
    get() = formatBytes(availableBytes)

  val usedPercentage: Float
    get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) * 100f else 0f

  companion object {
    fun formatBytes(bytes: Long): String {
      if (bytes <= 0) return "0 B"
      val units = arrayOf("B", "KB", "MB", "GB", "TB")
      val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, 4)
      val formatted = bytes / Math.pow(1024.0, digitGroups.toDouble())
      return String.format("%.1f %s", formatted, units[digitGroups])
    }
  }
}
