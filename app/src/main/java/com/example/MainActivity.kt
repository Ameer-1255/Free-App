package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VideoItem
import com.example.downloader.UrlParser
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.admin.AdminPinDialog
import com.example.ui.browser.WebSnifferScreen
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.downloader.DownloaderScreen
import com.example.ui.library.OfflineLibraryScreen
import com.example.ui.player.OfflinePlayerScreen
import com.example.ui.queue.QueueScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingShareIntent(intent)

    setContent {
      MyApplicationTheme(darkTheme = true) {
        MainScreen(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIncomingShareIntent(intent)
  }

  private fun handleIncomingShareIntent(intent: Intent?) {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
      val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
      if (!sharedText.isNullOrBlank()) {
        val extractedUrl = UrlParser.extractFirstUrl(sharedText) ?: sharedText
        viewModel.updateUrlInput(extractedUrl)
        viewModel.analyzeUrl(extractedUrl)
        viewModel.selectTab(AppNavTab.DOWNLOADER)
      }
    }
  }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val playingVideo by viewModel.playingVideo.collectAsStateWithLifecycle()
  val detailsVideo by viewModel.detailsVideo.collectAsStateWithLifecycle()

  val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
  val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
  val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
  val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()
  val selectedFormat by viewModel.selectedFormat.collectAsStateWithLifecycle()

  val completedVideos by viewModel.completedVideos.collectAsStateWithLifecycle()
  val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
  val liveProgressMap by viewModel.liveProgress.collectAsStateWithLifecycle()
  val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
  val isOfflineMode by viewModel.isOfflineModeActive.collectAsStateWithLifecycle()

  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val platformFilter by viewModel.selectedPlatformFilter.collectAsStateWithLifecycle()
  val showAdminPanel by viewModel.showAdminPanel.collectAsStateWithLifecycle()
  val showPinDialog by viewModel.showPinDialog.collectAsStateWithLifecycle()
  val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()

  // Handle hardware back press
  BackHandler(enabled = playingVideo != null || showAdminPanel || currentTab != AppNavTab.DOWNLOADER) {
    when {
      playingVideo != null -> viewModel.closePlayer()
      showAdminPanel -> viewModel.closeAdminPanel()
      currentTab != AppNavTab.DOWNLOADER -> viewModel.selectTab(AppNavTab.DOWNLOADER)
    }
  }

  // If full-screen offline player is active, render it over everything
  if (playingVideo != null) {
    OfflinePlayerScreen(
      video = playingVideo!!,
      onClose = { viewModel.closePlayer() },
      onToggleFavorite = { viewModel.toggleFavorite(it) },
      onShowDetails = { viewModel.showDetails(it) }
    )
  } else if (showAdminPanel) {
    AdminPanelScreen(
      config = adminConfig,
      onSaveConfig = { viewModel.saveAdminConfig(it) },
      videos = completedVideos,
      storageStats = storageStats,
      onDeleteVideo = { viewModel.deleteVideo(it) },
      onReseedDemos = { viewModel.reseedDemoMedia() },
      onPurgeStorage = { viewModel.clearAllOfflineVideos() },
      onClose = { viewModel.closeAdminPanel() }
    )
  } else {
    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      containerColor = DarkBackground,
      bottomBar = {
        NavigationBar(
          containerColor = DarkSurface,
          contentColor = TextPrimary,
          tonalElevation = 8.dp
        ) {
          // Tab 1: Downloader
          NavigationBarItem(
            selected = currentTab == AppNavTab.DOWNLOADER,
            onClick = { viewModel.selectTab(AppNavTab.DOWNLOADER) },
            icon = {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Downloader"
              )
            },
            label = { Text("Downloader", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_downloader")
          )

          // Tab 2: Web Sniffer (Hidden in forced offline mode or shows indicator)
          if (!isOfflineMode) {
            NavigationBarItem(
              selected = currentTab == AppNavTab.BROWSER,
              onClick = { viewModel.selectTab(AppNavTab.BROWSER) },
              icon = {
                Icon(
                  imageVector = Icons.Default.Language,
                  contentDescription = "Web Sniffer"
                )
              },
              label = { Text("Sniffer", fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = NeonCyan,
                indicatorColor = NeonCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
              ),
              modifier = Modifier.testTag("nav_browser")
            )
          }

          // Tab 3: Offline Library
          NavigationBarItem(
            selected = currentTab == AppNavTab.OFFLINE,
            onClick = { viewModel.selectTab(AppNavTab.OFFLINE) },
            icon = {
              BadgedBox(
                badge = {
                  if (completedVideos.isNotEmpty()) {
                    Badge(
                      containerColor = NeonCyan,
                      contentColor = Color.Black
                    ) {
                      Text("${completedVideos.size}")
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.VideoLibrary,
                  contentDescription = "Offline Library"
                )
              }
            },
            label = { Text("Offline", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_offline")
          )

          // Tab 4: Queue / Activity
          NavigationBarItem(
            selected = currentTab == AppNavTab.QUEUE,
            onClick = { viewModel.selectTab(AppNavTab.QUEUE) },
            icon = {
              BadgedBox(
                badge = {
                  if (activeDownloads.isNotEmpty()) {
                    Badge(
                      containerColor = Color(0xFFFF9900),
                      contentColor = Color.Black
                    ) {
                      Text("${activeDownloads.size}")
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.DownloadDone,
                  contentDescription = "Queue"
                )
              }
            },
            label = { Text("Queue", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = NeonCyan,
              indicatorColor = NeonCyan,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_queue")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(DarkBackground)
      ) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "tab_transition"
        ) { tab ->
          when (tab) {
            AppNavTab.DOWNLOADER -> {
              DownloaderScreen(
                urlInput = urlInput,
                onUrlChange = { viewModel.updateUrlInput(it) },
                onClearUrl = { viewModel.clearUrlInput() },
                onAnalyzeUrl = { viewModel.analyzeUrl() },
                isAnalyzing = isAnalyzing,
                analysisResult = analysisResult,
                analysisError = analysisError,
                selectedFormat = selectedFormat,
                onSelectFormat = { viewModel.selectFormat(it) },
                onStartDownload = { viewModel.startDownloadFromAnalysis() },
                onQuickPreset = { viewModel.quickDownloadPreset(it) },
                isOfflineMode = isOfflineMode,
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onOpenAdmin = { viewModel.requestOpenAdmin() }
              )
            }

            AppNavTab.BROWSER -> {
              WebSnifferScreen(
                onMediaDetected = { mediaUrl, title ->
                  viewModel.onSniffedMediaDetected(mediaUrl, title)
                }
              )
            }

            AppNavTab.OFFLINE -> {
              OfflineLibraryScreen(
                videos = completedVideos,
                storageStats = storageStats,
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedPlatform = platformFilter,
                onSelectPlatform = { viewModel.setPlatformFilter(it) },
                onPlayVideo = { viewModel.playVideo(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onShowDetails = { viewModel.showDetails(it) },
                onShareVideo = { shareVideoFile(context, it) },
                onDeleteVideo = { viewModel.deleteVideo(it) },
                onClearStorage = { viewModel.clearAllOfflineVideos() },
                onNavigateToDownloader = { viewModel.selectTab(AppNavTab.DOWNLOADER) }
              )
            }

            AppNavTab.QUEUE -> {
              QueueScreen(
                activeDownloads = activeDownloads,
                liveProgressMap = liveProgressMap,
                onPause = { viewModel.pauseDownload(it) },
                onCancel = { viewModel.cancelDownload(it) },
                onRetry = { viewModel.retryDownload(it) },
                onPlayCompleted = { viewModel.playVideo(it) },
                completedVideos = completedVideos,
                onNavigateToDownloader = { viewModel.selectTab(AppNavTab.DOWNLOADER) }
              )
            }
          }
        }
      }
    }
  }

  // Video Details Dialog
  if (detailsVideo != null) {
    VideoDetailsDialog(
      video = detailsVideo!!,
      onDismiss = { viewModel.showDetails(null) },
      onPlay = {
        viewModel.showDetails(null)
        viewModel.playVideo(it)
      },
      onShare = { shareVideoFile(context, it) },
      onRename = { v, newTitle -> viewModel.renameVideo(v, newTitle) },
      onDelete = { viewModel.deleteVideo(it) }
    )
  }

  // Admin PIN Protection Dialog
  if (showPinDialog) {
    AdminPinDialog(
      onDismiss = { viewModel.dismissPinDialog() },
      onVerify = { viewModel.verifyPin(it) }
    )
  }
}

private fun shareVideoFile(context: Context, video: VideoItem) {
  try {
    val file = File(video.localFilePath)
    if (file.exists()) {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, video.title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
    } else {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.sourceUrl}")
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Video Link"))
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
