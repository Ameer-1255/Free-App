package com.example.downloader

import android.content.Context
import android.os.Environment
import com.example.data.VideoRepository
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadEngine(
  private val context: Context,
  private val repository: VideoRepository,
  private val scope: CoroutineScope
) {
  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()

  private val activeJobs = ConcurrentHashMap<Long, Job>()

  // Live active download progress map (id -> DownloadProgressState)
  private val _progressMap = MutableStateFlow<Map<Long, DownloadProgressState>>(emptyMap())
  val progressMap = _progressMap.asStateFlow()

  fun startDownload(
    title: String,
    sourceUrl: String,
    downloadUrl: String,
    platform: String,
    thumbnailUrl: String,
    quality: String,
    mimeType: String,
    durationSeconds: Long,
    estimatedBytes: Long
  ) {
    scope.launch {
      // 1. Insert into Room as QUEUED
      val initialItem = VideoItem(
        title = title,
        sourceUrl = sourceUrl,
        platform = platform,
        localFilePath = "",
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        fileSizeBytes = estimatedBytes,
        quality = quality,
        mimeType = mimeType,
        downloadStatus = DownloadStatus.DOWNLOADING.name,
        downloadProgress = 0f,
        downloadSpeed = "Starting...",
        timestamp = System.currentTimeMillis()
      )
      val videoId = repository.insertVideo(initialItem)

      // 2. Launch background download coroutine
      executeDownload(videoId, initialItem.copy(id = videoId), downloadUrl)
    }
  }

  fun retryDownload(video: VideoItem) {
    scope.launch {
      repository.updateProgress(video.id, 0f, DownloadStatus.DOWNLOADING, "Retrying...", video.fileSizeBytes)
      val downloadUrl = if (video.sourceUrl.isNotEmpty()) video.sourceUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
      executeDownload(video.id, video, downloadUrl)
    }
  }

  fun cancelDownload(videoId: Long) {
    activeJobs[videoId]?.cancel()
    activeJobs.remove(videoId)
    scope.launch {
      val video = repository.getVideoById(videoId)
      if (video != null) {
        repository.deleteVideo(video)
      }
      val current = _progressMap.value.toMutableMap()
      current.remove(videoId)
      _progressMap.value = current
    }
  }

  fun pauseDownload(videoId: Long) {
    activeJobs[videoId]?.cancel()
    activeJobs.remove(videoId)
    scope.launch {
      repository.updateProgress(videoId, 0f, DownloadStatus.PAUSED, "Paused", 0L)
      val current = _progressMap.value.toMutableMap()
      current.remove(videoId)
      _progressMap.value = current
    }
  }

  private fun executeDownload(videoId: Long, item: VideoItem, downloadUrl: String) {
    val job = scope.launch(Dispatchers.IO) {
      val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }

      // Safe clean filename
      val safeTitle = item.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(30)
      val ext = if (item.mimeType.contains("mp3")) "mp3" else "mp4"
      val targetFile = File(targetDir, "omnisave_${videoId}_${safeTitle}.$ext")

      try {
        val request = Request.Builder()
          .url(downloadUrl)
          .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
          .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body == null) {
          throw Exception("Download HTTP code: ${response.code}")
        }

        val body = response.body!!
        val contentLength = if (body.contentLength() > 0) body.contentLength() else item.fileSizeBytes.coerceAtLeast(10_000_000L)
        var totalBytesRead = 0L
        var lastUpdateTime = System.currentTimeMillis()
        var bytesSinceLastUpdate = 0L

        body.byteStream().use { input ->
          FileOutputStream(targetFile).use { output ->
            val buffer = ByteArray(16 * 1024)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
              output.write(buffer, 0, bytesRead)
              totalBytesRead += bytesRead
              bytesSinceLastUpdate += bytesRead

              val now = System.currentTimeMillis()
              val elapsed = now - lastUpdateTime
              if (elapsed >= 400) { // update UI throttle
                val progress = ((totalBytesRead.toDouble() / contentLength.toDouble()) * 100.0).toFloat().coerceIn(0f, 99f)
                val speedKbs = (bytesSinceLastUpdate.toDouble() / 1024.0) / (elapsed.toDouble() / 1000.0)
                val speedString = if (speedKbs > 1024) {
                  String.format("%.1f MB/s", speedKbs / 1024.0)
                } else {
                  String.format("%.0f KB/s", speedKbs)
                }

                // Update live in memory progress
                val state = DownloadProgressState(
                  videoId = videoId,
                  progress = progress,
                  downloadedBytes = totalBytesRead,
                  totalBytes = contentLength,
                  speed = speedString,
                  isCompleted = false
                )
                val current = _progressMap.value.toMutableMap()
                current[videoId] = state
                _progressMap.value = current

                // Persist occasionally in db
                repository.updateProgress(videoId, progress, DownloadStatus.DOWNLOADING, speedString, contentLength)

                lastUpdateTime = now
                bytesSinceLastUpdate = 0L
              }
            }
          }
        }

        // Successfully completed!
        val completedState = DownloadProgressState(
          videoId = videoId,
          progress = 100f,
          downloadedBytes = totalBytesRead,
          totalBytes = totalBytesRead,
          speed = "Done",
          isCompleted = true
        )
        val current = _progressMap.value.toMutableMap()
        current.remove(videoId)
        _progressMap.value = current

        repository.updateVideo(
          item.copy(
            localFilePath = targetFile.absolutePath,
            fileSizeBytes = totalBytesRead,
            downloadStatus = DownloadStatus.COMPLETED.name,
            downloadProgress = 100f,
            downloadSpeed = "Completed"
          )
        )

      } catch (e: CancellationException) {
        // Cancelled by user
        if (targetFile.exists()) targetFile.delete()
      } catch (e: Exception) {
        e.printStackTrace()
        val current = _progressMap.value.toMutableMap()
        current.remove(videoId)
        _progressMap.value = current

        repository.updateProgress(videoId, 0f, DownloadStatus.FAILED, "Error: ${e.message?.take(20)}", 0L)
      } finally {
        activeJobs.remove(videoId)
      }
    }
    activeJobs[videoId] = job
  }
}

data class DownloadProgressState(
  val videoId: Long,
  val progress: Float,
  val downloadedBytes: Long,
  val totalBytes: Long,
  val speed: String,
  val isCompleted: Boolean
) {
  val formattedDownloaded: String
    get() = formatBytes(downloadedBytes)

  val formattedTotal: String
    get() = formatBytes(totalBytes)

  private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format("%.1f MB", mb)
  }
}
