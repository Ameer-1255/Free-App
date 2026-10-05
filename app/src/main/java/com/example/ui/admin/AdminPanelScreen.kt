package com.example.ui.admin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminConfig
import com.example.data.StorageStats
import com.example.data.model.VideoItem
import com.example.ui.downloader.getPlatformColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlatformFacebook
import com.example.ui.theme.PlatformInstagram
import com.example.ui.theme.PlatformMovieBox
import com.example.ui.theme.PlatformTikTok
import com.example.ui.theme.PlatformYouTube
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import java.io.File

@Composable
fun AdminPanelScreen(
  config: AdminConfig,
  onSaveConfig: (AdminConfig) -> Unit,
  videos: List<VideoItem>,
  storageStats: StorageStats,
  onDeleteVideo: (VideoItem) -> Unit,
  onReseedDemos: () -> Unit,
  onPurgeStorage: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  BackHandler { onClose() }

  var currentConfig by remember { mutableStateOf(config) }
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Overview", "Engine", "Platforms", "Maintenance")

  var showResetDialog by remember { mutableStateOf(false) }
  var showPurgeDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("admin_panel_screen")
  ) {
    // TOP ADMIN APPBAR
    Surface(
      color = DarkSurface,
      tonalElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.testTag("admin_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
          modifier = Modifier
            .size(36.dp)
            .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Admin Console",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TextPrimary
          )
          Text(
            text = "OmniSave Engine & Storage Control",
            fontSize = 11.sp,
            color = NeonCyan
          )
        }

        Button(
          onClick = {
            onSaveConfig(currentConfig)
            Toast.makeText(context, "Admin settings updated", Toast.LENGTH_SHORT).show()
          },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          modifier = Modifier.testTag("admin_save_button")
        ) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // TAB ROW
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = DarkSurface,
      contentColor = NeonCyan,
      edgePadding = 16.dp,
      divider = { HorizontalDivider(color = DarkOutline) }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp,
              color = if (selectedTab == index) NeonCyan else TextSecondary
            )
          }
        )
      }
    }

    // TAB CONTENTS
    when (selectedTab) {
      0 -> AdminOverviewTab(videos = videos, storageStats = storageStats)
      1 -> AdminEngineTab(
        config = currentConfig,
        onConfigChange = { currentConfig = it }
      )
      2 -> AdminPlatformsTab(
        config = currentConfig,
        onConfigChange = { currentConfig = it }
      )
      3 -> AdminMaintenanceTab(
        videos = videos,
        config = currentConfig,
        onConfigChange = { currentConfig = it },
        onDeleteVideo = onDeleteVideo,
        onTriggerReseed = { showResetDialog = true },
        onTriggerPurge = { showPurgeDialog = true }
      )
    }
  }

  // Reseed Confirmation Dialog
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      containerColor = DarkSurface,
      title = { Text("Re-seed Sample Media?", color = TextPrimary) },
      text = {
        Text("This will populate starter offline video samples for YouTube, TikTok, Instagram, MovieBox, and Facebook.", color = TextSecondary)
      },
      confirmButton = {
        Button(
          onClick = {
            onReseedDemos()
            showResetDialog = false
            Toast.makeText(context, "Sample media re-seeded", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Re-seed", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }

  // Purge Confirmation Dialog
  if (showPurgeDialog) {
    AlertDialog(
      onDismissRequest = { showPurgeDialog = false },
      containerColor = DarkSurface,
      title = { Text("Purge Media Storage?", color = ErrorRed) },
      text = {
        Text("This will permanently remove all cached and downloaded files from device storage.", color = TextSecondary)
      },
      confirmButton = {
        Button(
          onClick = {
            onPurgeStorage()
            showPurgeDialog = false
            Toast.makeText(context, "Storage purged", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Purge Everything", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showPurgeDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      }
    )
  }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW
// -------------------------------------------------------------
@Composable
private fun AdminOverviewTab(
  videos: List<VideoItem>,
  storageStats: StorageStats
) {
  val platformCounts = remember(videos) {
    mapOf(
      "YouTube" to videos.count { it.platform.equals("YouTube", true) },
      "TikTok" to videos.count { it.platform.equals("TikTok", true) },
      "Instagram" to videos.count { it.platform.equals("Instagram", true) },
      "Facebook" to videos.count { it.platform.equals("Facebook", true) },
      "MovieBox" to videos.count { it.platform.equals("MovieBox", true) },
      "Direct" to videos.count { it.platform.equals("Direct", true) || it.platform.equals("DIRECT WEB", true) }
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Key System Metrics",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // 2x2 KPI GRID
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard(
          title = "Total Media Files",
          value = "${videos.size}",
          subtitle = "In Local Database",
          icon = Icons.Default.VideoLibrary,
          accentColor = NeonCyan,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Storage Consumed",
          value = storageStats.formattedUsed,
          subtitle = "Saved On Disk",
          icon = Icons.Default.Storage,
          accentColor = WarningOrange,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard(
          title = "Free Device Space",
          value = storageStats.formattedAvailable,
          subtitle = "Available Storage",
          icon = Icons.Default.Folder,
          accentColor = SuccessGreen,
          modifier = Modifier.weight(1f)
        )
        KpiCard(
          title = "Downloader Engine",
          value = "Active",
          subtitle = "OkHttp 4.10 Streamer",
          icon = Icons.Default.Speed,
          accentColor = NeonCyan,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // PLATFORM DISTRIBUTION
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Platform Breakdown",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(12.dp))

          platformCounts.forEach { (platform, count) ->
            val color = getPlatformColor(platform)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = platform, color = TextPrimary, fontSize = 13.sp)
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "$count videos",
                  color = color,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // DIRECTORY PATHS INSPECT
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Storage Architecture",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Offline media is safely stored in scoped app storage (zero dangerous permissions required).",
            fontSize = 12.sp,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Directory: .../files/Movies/omnisave_*.mp4\nSecurity: FileProvider Isolated\nFormat Support: MP4, WebM, M3U8, MP3",
              color = TextSecondary,
              fontSize = 11.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 1: ENGINE SETTINGS
// -------------------------------------------------------------
@Composable
private fun AdminEngineTab(
  config: AdminConfig,
  onConfigChange: (AdminConfig) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Download Engine Configuration",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Concurrent Downloads Slider
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Max Concurrent Downloads", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
              Text("Limits parallel thread allocations", fontSize = 11.sp, color = TextMuted)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = NeonCyan.copy(alpha = 0.2f)) {
              Text(
                text = "${config.maxConcurrentDownloads} Tasks",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Slider(
            value = config.maxConcurrentDownloads.toFloat(),
            onValueChange = { onConfigChange(config.copy(maxConcurrentDownloads = it.toInt())) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      }
    }

    // Default Video Resolution
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Default Target Resolution", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Text("Preferred stream quality when analyzing links", fontSize = 11.sp, color = TextMuted)
          Spacer(modifier = Modifier.height(12.dp))

          listOf("1080p", "720p", "480p").forEach { res ->
            val isSelected = config.defaultResolution.startsWith(res)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onConfigChange(config.copy(defaultResolution = res)) }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (res == "1080p") "1080p Full HD (Highest Quality)" else if (res == "720p") "720p HD (Balanced Size)" else "480p SD (Data Saver)",
                color = if (isSelected) NeonCyan else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }

    // Wi-Fi Only Switch
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Download Only on Wi-Fi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text("Pauses download engine when on cellular network", fontSize = 11.sp, color = TextMuted)
          }
          Switch(
            checked = config.downloadWifiOnly,
            onCheckedChange = { onConfigChange(config.copy(downloadWifiOnly = it)) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
          )
        }
      }
    }

    // Web Sniffer User-Agent Mode
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Sniffer User-Agent Spoofing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Text("Custom browser signature to bypass bot filters", fontSize = 11.sp, color = TextMuted)
          Spacer(modifier = Modifier.height(10.dp))

          listOf("Mobile Chrome", "Desktop Chrome", "iOS Safari").forEach { ua ->
            val isSelected = config.userAgentMode == ua
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onConfigChange(config.copy(userAgentMode = ua)) }
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = ua,
                color = if (isSelected) NeonCyan else TextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: PLATFORMS CONFIG
// -------------------------------------------------------------
@Composable
private fun AdminPlatformsTab(
  config: AdminConfig,
  onConfigChange: (AdminConfig) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Platform Modules & Stream Sniffers",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Platform Modules Toggles
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Platform Resolvers", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Spacer(modifier = Modifier.height(8.dp))

          PlatformToggleRow("YouTube Parser", "Shorts, HD Videos & MP3 audio", PlatformYouTube, config.enableYouTube) {
            onConfigChange(config.copy(enableYouTube = it))
          }
          PlatformToggleRow("TikTok Engine", "Watermark stripper & viral sounds", PlatformTikTok, config.enableTikTok) {
            onConfigChange(config.copy(enableTikTok = it))
          }
          PlatformToggleRow("Instagram Sniffer", "Reels, Posts & Story clips", PlatformInstagram, config.enableInstagram) {
            onConfigChange(config.copy(enableInstagram = it))
          }
          PlatformToggleRow("Facebook Watch", "Pages, Reels & Group clips", PlatformFacebook, config.enableFacebook) {
            onConfigChange(config.copy(enableFacebook = it))
          }
          PlatformToggleRow("MovieBox Streamer", "Movies, Series & Trailers", PlatformMovieBox, config.enableMovieBox) {
            onConfigChange(config.copy(enableMovieBox = it))
          }
        }
      }
    }

    // TikTok Specific Feature
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Auto-Strip TikTok Watermark", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text("Downloads high-bitrate clean video stream without logo overlay", fontSize = 11.sp, color = TextMuted)
          }
          Switch(
            checked = config.stripTikTokWatermark,
            onCheckedChange = { onConfigChange(config.copy(stripTikTokWatermark = it)) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = PlatformTikTok)
          )
        }
      }
    }

    // Media Sniffer Format Filters
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("In-App Sniffer Auto-Capture Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Text("Sniff media types as user browses online", fontSize = 11.sp, color = TextMuted)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Capture .MP4 & .WebM", fontSize = 13.sp, color = TextPrimary)
            Switch(
              checked = config.snifferMp4,
              onCheckedChange = { onConfigChange(config.copy(snifferMp4 = it)) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Capture .M3U8 HLS Streams", fontSize = 13.sp, color = TextPrimary)
            Switch(
              checked = config.snifferM3u8,
              onCheckedChange = { onConfigChange(config.copy(snifferM3u8 = it)) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Capture .MP3 Audio Streams", fontSize = 13.sp, color = TextPrimary)
            Switch(
              checked = config.snifferMp3,
              onCheckedChange = { onConfigChange(config.copy(snifferMp3 = it)) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PlatformToggleRow(
  name: String,
  desc: String,
  color: Color,
  enabled: Boolean,
  onToggle: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .background(color, CircleShape)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(text = name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
        Text(text = desc, fontSize = 11.sp, color = TextMuted)
      }
    }
    Switch(
      checked = enabled,
      onCheckedChange = onToggle,
      colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = color)
    )
  }
}

// -------------------------------------------------------------
// TAB 3: MAINTENANCE & DATABASE
// -------------------------------------------------------------
@Composable
private fun AdminMaintenanceTab(
  videos: List<VideoItem>,
  config: AdminConfig,
  onConfigChange: (AdminConfig) -> Unit,
  onDeleteVideo: (VideoItem) -> Unit,
  onTriggerReseed: () -> Unit,
  onTriggerPurge: () -> Unit
) {
  val context = LocalContext.current
  var pinInput by remember { mutableStateOf(config.adminPin) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Maintenance & Database Operations",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // ACTIONS ROW: RESEED & PURGE & EXPORT
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onTriggerReseed,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Re-seed", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                // Export JSON of catalog
                val json = buildCatalogJson(videos)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_SUBJECT, "OmniSave Media Catalog Backup")
                  putExtra(Intent.EXTRA_TEXT, json)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export Catalog Backup"))
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Export JSON", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = onTriggerPurge,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.CleaningServices, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Purge", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // ADMIN PIN LOCK PROTECTION
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Admin PIN Protection", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
              Text("Require 4-digit code to access Admin Console", fontSize = 11.sp, color = TextMuted)
            }
            Switch(
              checked = config.isPinProtectionEnabled,
              onCheckedChange = { onConfigChange(config.copy(isPinProtectionEnabled = it)) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
            )
          }

          if (config.isPinProtectionEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = pinInput,
              onValueChange = {
                if (it.length <= 4) {
                  pinInput = it
                  onConfigChange(config.copy(adminPin = it))
                }
              },
              label = { Text("4-Digit Admin PIN", fontSize = 12.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkOutline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }

    // DATABASE TABLE RECORDS INSPECT
    item {
      Text(
        text = "Raw Database Records (${videos.size})",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary
      )
    }

    items(videos, key = { it.id }) { item ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = DarkSurfaceVariant
          ) {
            Text(
              text = "#${item.id}",
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              color = TextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${item.platform} • ${item.quality} • ${StorageStats.formatBytes(item.fileSizeBytes)}",
              color = TextMuted,
              fontSize = 10.sp
            )
          }

          IconButton(
            onClick = { onDeleteVideo(item) },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun KpiCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary
      )

      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
      )

      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = TextMuted
      )
    }
  }
}

private fun buildCatalogJson(videos: List<VideoItem>): String {
  val sb = StringBuilder()
  sb.append("[\n")
  videos.forEachIndexed { i, v ->
    sb.append("  {\n")
    sb.append("    \"id\": ${v.id},\n")
    sb.append("    \"title\": \"${v.title.replace("\"", "\\\"")}\",\n")
    sb.append("    \"platform\": \"${v.platform}\",\n")
    sb.append("    \"quality\": \"${v.quality}\",\n")
    sb.append("    \"fileSizeBytes\": ${v.fileSizeBytes},\n")
    sb.append("    \"sourceUrl\": \"${v.sourceUrl}\"\n")
    sb.append("  }${if (i < videos.size - 1) "," else ""}\n")
  }
  sb.append("]")
  return sb.toString()
}
