package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.OmniSaveApp
import com.example.data.AdminConfig
import com.example.data.AdminPreferences
import com.example.data.StorageStats
import com.example.data.VideoRepository
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import com.example.data.model.VideoPlatform
import com.example.downloader.DownloadEngine
import com.example.downloader.DownloadProgressState
import com.example.downloader.ParsedVideoInfo
import com.example.downloader.PresetSample
import com.example.downloader.UrlParser
import com.example.downloader.VideoFormatOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
  DOWNLOADER("Downloader"),
  BROWSER("Web Sniffer"),
  OFFLINE("Offline Library"),
  QUEUE("Queue")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val app = application as OmniSaveApp
  private val repository: VideoRepository = app.repository
  private val downloadEngine: DownloadEngine = app.downloadEngine
  private val adminPreferences = AdminPreferences(application)

  private val _adminConfig = MutableStateFlow(adminPreferences.loadConfig())
  val adminConfig: StateFlow<AdminConfig> = _adminConfig.asStateFlow()

  private val _showAdminPanel = MutableStateFlow(false)
  val showAdminPanel: StateFlow<Boolean> = _showAdminPanel.asStateFlow()

  private val _showPinDialog = MutableStateFlow(false)
  val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

  // Active navigation
  private val _currentTab = MutableStateFlow(AppNavTab.DOWNLOADER)
  val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

  // Offline viewing player state
  private val _playingVideo = MutableStateFlow<VideoItem?>(null)
  val playingVideo: StateFlow<VideoItem?> = _playingVideo.asStateFlow()

  // Offline Mode forced toggle
  private val _isOfflineModeActive = MutableStateFlow(false)
  val isOfflineModeActive: StateFlow<Boolean> = _isOfflineModeActive.asStateFlow()

  // URL Input Bar State
  private val _urlInput = MutableStateFlow("")
  val urlInput: StateFlow<String> = _urlInput.asStateFlow()

  // Analysis State
  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  private val _analysisResult = MutableStateFlow<ParsedVideoInfo?>(null)
  val analysisResult: StateFlow<ParsedVideoInfo?> = _analysisResult.asStateFlow()

  private val _selectedFormat = MutableStateFlow<VideoFormatOption?>(null)
  val selectedFormat: StateFlow<VideoFormatOption?> = _selectedFormat.asStateFlow()

  private val _analysisError = MutableStateFlow<String?>(null)
  val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

  // Video info modal sheet
  private val _detailsVideo = MutableStateFlow<VideoItem?>(null)
  val detailsVideo: StateFlow<VideoItem?> = _detailsVideo.asStateFlow()

  // Search & Filter in Offline Library
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedPlatformFilter = MutableStateFlow("ALL")
  val selectedPlatformFilter: StateFlow<String> = _selectedPlatformFilter.asStateFlow()

  // Repository flows
  val completedVideos: StateFlow<List<VideoItem>> = repository.completedVideos
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeDownloads: StateFlow<List<VideoItem>> = repository.activeDownloads
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val liveProgress: StateFlow<Map<Long, DownloadProgressState>> = downloadEngine.progressMap

  private val _storageStats = MutableStateFlow(repository.getStorageInfo())
  val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

  // Web Sniffer banner state
  private val _sniffedVideoUrl = MutableStateFlow<String?>(null)
  val sniffedVideoUrl: StateFlow<String?> = _sniffedVideoUrl.asStateFlow()

  fun selectTab(tab: AppNavTab) {
    _currentTab.value = tab
  }

  fun toggleOfflineMode() {
    val newMode = !_isOfflineModeActive.value
    _isOfflineModeActive.value = newMode
    if (newMode) {
      _currentTab.value = AppNavTab.OFFLINE
    }
  }

  fun updateUrlInput(url: String) {
    _urlInput.value = url
    _analysisError.value = null
  }

  fun clearUrlInput() {
    _urlInput.value = ""
    _analysisResult.value = null
    _analysisError.value = null
  }

  fun analyzeUrl(customUrl: String? = null) {
    val raw = customUrl ?: _urlInput.value
    val target = raw.trim()
    if (target.isBlank()) {
      _analysisError.value = "Please enter or paste a valid video URL"
      return
    }

    viewModelScope.launch {
      _isAnalyzing.value = true
      _analysisError.value = null
      _analysisResult.value = null

      delay(500) // smooth parsing animation
      try {
        val parsed = UrlParser.parseUrl(target)
        _analysisResult.value = parsed
        _selectedFormat.value = parsed.availableFormats.firstOrNull()
      } catch (e: Exception) {
        _analysisError.value = "Could not parse video metadata: ${e.message}"
      } finally {
        _isAnalyzing.value = false
      }
    }
  }

  fun selectFormat(format: VideoFormatOption) {
    _selectedFormat.value = format
  }

  fun startDownloadFromAnalysis() {
    val info = _analysisResult.value ?: return
    val format = _selectedFormat.value ?: info.availableFormats.first()

    downloadEngine.startDownload(
      title = info.title,
      sourceUrl = info.rawUrl,
      downloadUrl = format.downloadUrl,
      platform = info.platform.name,
      thumbnailUrl = info.thumbnailUrl,
      quality = format.label,
      mimeType = format.mimeType,
      durationSeconds = info.durationSeconds,
      estimatedBytes = format.estimatedSizeBytes
    )

    _analysisResult.value = null
    _urlInput.value = ""
    _currentTab.value = AppNavTab.QUEUE
    refreshStorage()
  }

  fun quickDownloadPreset(preset: PresetSample) {
    _urlInput.value = preset.url
    analyzeUrl(preset.url)
  }

  fun onSniffedMediaDetected(mediaUrl: String, title: String) {
    _sniffedVideoUrl.value = mediaUrl
    _urlInput.value = mediaUrl
    analyzeUrl(mediaUrl)
  }

  fun dismissSniffedBanner() {
    _sniffedVideoUrl.value = null
  }

  fun playVideo(video: VideoItem) {
    _playingVideo.value = video
  }

  fun closePlayer() {
    _playingVideo.value = null
  }

  fun showDetails(video: VideoItem?) {
    _detailsVideo.value = video
  }

  fun toggleFavorite(video: VideoItem) {
    viewModelScope.launch {
      repository.toggleFavorite(video.id, video.isFavorite)
    }
  }

  fun renameVideo(video: VideoItem, newTitle: String) {
    if (newTitle.isNotBlank()) {
      viewModelScope.launch {
        repository.renameVideo(video.id, newTitle.trim())
      }
    }
  }

  fun deleteVideo(video: VideoItem) {
    viewModelScope.launch {
      repository.deleteVideo(video)
      refreshStorage()
    }
  }

  fun retryDownload(video: VideoItem) {
    downloadEngine.retryDownload(video)
  }

  fun cancelDownload(videoId: Long) {
    downloadEngine.cancelDownload(videoId)
    refreshStorage()
  }

  fun pauseDownload(videoId: Long) {
    downloadEngine.pauseDownload(videoId)
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setPlatformFilter(filter: String) {
    _selectedPlatformFilter.value = filter
  }

  fun clearAllOfflineVideos() {
    viewModelScope.launch {
      repository.clearCompletedVideos()
      refreshStorage()
    }
  }

  fun requestOpenAdmin() {
    val cfg = _adminConfig.value
    if (cfg.isPinProtectionEnabled) {
      _showPinDialog.value = true
    } else {
      _showAdminPanel.value = true
    }
  }

  fun dismissPinDialog() {
    _showPinDialog.value = false
  }

  fun verifyPin(pin: String): Boolean {
    val isValid = pin == _adminConfig.value.adminPin
    if (isValid) {
      _showPinDialog.value = false
      _showAdminPanel.value = true
    }
    return isValid
  }

  fun closeAdminPanel() {
    _showAdminPanel.value = false
  }

  fun saveAdminConfig(config: AdminConfig) {
    _adminConfig.value = config
    adminPreferences.saveConfig(config)
  }

  fun reseedDemoMedia() {
    viewModelScope.launch(Dispatchers.IO) {
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
          isFavorite = true
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
          isFavorite = true
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
          timestamp = System.currentTimeMillis() - 43200000L
        )
      )
      for (item in samples) {
        repository.insertVideo(item)
      }
      refreshStorage()
    }
  }

  fun refreshStorage() {
    viewModelScope.launch(Dispatchers.IO) {
      _storageStats.value = repository.getStorageInfo()
    }
  }
}
