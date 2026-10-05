package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.VideoRepository
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import com.example.data.model.VideoPlatform
import com.example.downloader.DownloadEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OmniSaveApp : Application() {
  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  lateinit var database: AppDatabase
    private set
  lateinit var repository: VideoRepository
    private set
  lateinit var downloadEngine: DownloadEngine
    private set

  override fun onCreate() {
    super.onCreate()
    database = AppDatabase.getDatabase(this)
    repository = VideoRepository(database.videoDao(), this)
    downloadEngine = DownloadEngine(this, repository, appScope)

    // Pre-populate with starter sample items if empty
    appScope.launch(Dispatchers.IO) {
      val existing = repository.allVideos.first()
      if (existing.isEmpty()) {
        seedSampleVideos()
      }
    }
  }

  private suspend fun seedSampleVideos() {
    val samples = listOf(
      VideoItem(
        title = "MovieBox: Big Buck Bunny Official 4K Trailer",
        sourceUrl = "https://moviebox.app/trailer/big-buck-bunny",
        platform = VideoPlatform.MOVIEBOX.name,
        localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        thumbnailUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80",
        durationSeconds = 596,
        fileSizeBytes = 45_200_000L,
        quality = "1080p FHD",
        mimeType = "video/mp4",
        downloadStatus = DownloadStatus.COMPLETED.name,
        downloadProgress = 100f,
        downloadSpeed = "Offline Ready",
        timestamp = System.currentTimeMillis() - 86400000L,
        isFavorite = true,
        notes = "Downloaded via MovieBox Downloader. Available for offline playback."
      ),
      VideoItem(
        title = "YouTube: Sintel Animation Film Short (720p HD)",
        sourceUrl = "https://youtube.com/watch?v=eRsGyueVLvQ",
        platform = VideoPlatform.YOUTUBE.name,
        localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
        durationSeconds = 180,
        fileSizeBytes = 28_400_000L,
        quality = "720p HD",
        mimeType = "video/mp4",
        downloadStatus = DownloadStatus.COMPLETED.name,
        downloadProgress = 100f,
        downloadSpeed = "Offline Ready",
        timestamp = System.currentTimeMillis() - 43200000L,
        isFavorite = false,
        notes = "YouTube 720p stream saved for offline viewing."
      ),
      VideoItem(
        title = "Instagram: Cybernetic Reel Clip #ViralTrends",
        sourceUrl = "https://instagram.com/reel/C-t-reel-example",
        platform = VideoPlatform.INSTAGRAM.name,
        localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
        durationSeconds = 60,
        fileSizeBytes = 18_100_000L,
        quality = "1080p 60fps",
        mimeType = "video/mp4",
        downloadStatus = DownloadStatus.COMPLETED.name,
        downloadProgress = 100f,
        downloadSpeed = "Offline Ready",
        timestamp = System.currentTimeMillis() - 14400000L,
        isFavorite = true,
        notes = "Instagram Reel saved offline with zero buffering."
      ),
      VideoItem(
        title = "TikTok: Viral Dance Trends & Beat #ForYou",
        sourceUrl = "https://www.tiktok.com/@trendcreator/video/7391823901",
        platform = VideoPlatform.TIKTOK.name,
        localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        thumbnailUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
        durationSeconds = 45,
        fileSizeBytes = 12_400_000L,
        quality = "1080p HD (No Watermark)",
        mimeType = "video/mp4",
        downloadStatus = DownloadStatus.COMPLETED.name,
        downloadProgress = 100f,
        downloadSpeed = "Offline Ready",
        timestamp = System.currentTimeMillis() - 21600000L,
        isFavorite = true,
        notes = "TikTok video saved in HD without watermark for offline playback."
      ),
      VideoItem(
        title = "Facebook: Nature Wonders HD Highlight",
        sourceUrl = "https://facebook.com/watch/?v=98273641029",
        platform = VideoPlatform.FACEBOOK.name,
        localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        thumbnailUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&auto=format&fit=crop&q=80",
        durationSeconds = 95,
        fileSizeBytes = 15_600_000L,
        quality = "720p HD",
        mimeType = "video/mp4",
        downloadStatus = DownloadStatus.COMPLETED.name,
        downloadProgress = 100f,
        downloadSpeed = "Offline Ready",
        timestamp = System.currentTimeMillis() - 7200000L,
        isFavorite = false,
        notes = "Facebook video saved to local device gallery."
      )
    )

    for (item in samples) {
      repository.insertVideo(item)
    }
  }
}
