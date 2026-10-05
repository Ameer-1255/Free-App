package com.example.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StorageStats
import com.example.data.model.VideoItem
import com.example.ui.downloader.getPlatformColor
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OfflineLibraryScreen(
  videos: List<VideoItem>,
  storageStats: StorageStats,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedPlatform: String,
  onSelectPlatform: (String) -> Unit,
  onPlayVideo: (VideoItem) -> Unit,
  onToggleFavorite: (VideoItem) -> Unit,
  onShowDetails: (VideoItem) -> Unit,
  onShareVideo: (VideoItem) -> Unit,
  onDeleteVideo: (VideoItem) -> Unit,
  onClearStorage: () -> Unit,
  onNavigateToDownloader: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showCleanDialog by remember { mutableStateOf(false) }

  val platforms = listOf("ALL", "TIKTOK", "MOVIEBOX", "YOUTUBE", "INSTAGRAM", "FACEBOOK")

  val filteredVideos = remember(videos, searchQuery, selectedPlatform) {
    videos.filter { video ->
      val matchesPlatform = if (selectedPlatform == "ALL") true else video.platform.equals(selectedPlatform, ignoreCase = true)
      val matchesSearch = if (searchQuery.isBlank()) true else {
        video.title.contains(searchQuery, ignoreCase = true) ||
            video.platform.contains(searchQuery, ignoreCase = true)
      }
      matchesPlatform && matchesSearch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Top Header with Offline Indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Offline Library",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 22.sp,
          color = TextPrimary
        )
        Text(
          text = "Play without Wi-Fi or Cellular Data",
          fontSize = 12.sp,
          color = NeonCyan
        )
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = NeonCyan.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.OfflineBolt,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${videos.size} Videos",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // STORAGE WIDGET CARD
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Device Media Storage",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
          }

          IconButton(
            onClick = { showCleanDialog = true },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CleaningServices,
              contentDescription = "Clean storage",
              tint = TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
          progress = { (storageStats.usedPercentage / 100f).coerceIn(0.02f, 1f) },
          color = NeonCyan,
          trackColor = DarkSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Saved: ${storageStats.formattedUsed}",
            fontSize = 11.sp,
            color = TextSecondary
          )
          Text(
            text = "Free Space: ${storageStats.formattedAvailable}",
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // SEARCH BAR
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      placeholder = { Text("Search offline library...", color = TextMuted, fontSize = 13.sp) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchChange("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = DarkOutline,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp)
        .testTag("library_search_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // PLATFORM FILTER CHIPS
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(platforms) { platform ->
        val isSelected = selectedPlatform.equals(platform, ignoreCase = true)
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = if (isSelected) NeonCyan else DarkSurfaceVariant,
          modifier = Modifier.clickable { onSelectPlatform(platform) }
        ) {
          Text(
            text = if (platform == "ALL") "All Media" else platform,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.Black else TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // VIDEO LIST / EMPTY STATE
    if (filteredVideos.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
          )
          Text(
            text = if (searchQuery.isNotBlank()) "No videos match your search" else "No Offline Videos Yet",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
          )
          Text(
            text = "Save videos from YouTube, MovieBox, Instagram, or Facebook for zero-data offline viewing.",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Button(
            onClick = onNavigateToDownloader,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
          ) {
            Text("Go to Downloader", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("offline_videos_list")
      ) {
        items(filteredVideos, key = { it.id }) { video ->
          VideoCardItem(
            video = video,
            onPlay = { onPlayVideo(video) },
            onFavoriteToggle = { onToggleFavorite(video) },
            onDetails = { onShowDetails(video) },
            onShare = { onShareVideo(video) },
            onDelete = { onDeleteVideo(video) }
          )
        }
        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }

  // Storage Cleanup confirmation dialog
  if (showCleanDialog) {
    AlertDialog(
      onDismissRequest = { showCleanDialog = false },
      containerColor = DarkSurface,
      titleContentColor = TextPrimary,
      textContentColor = TextSecondary,
      title = { Text("Clear All Offline Media?") },
      text = {
        Text("This will permanently delete all downloaded offline video files from your device storage (${storageStats.formattedUsed}).")
      },
      confirmButton = {
        Button(
          onClick = {
            onClearStorage()
            showCleanDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Clear Storage", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCleanDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun VideoCardItem(
  video: VideoItem,
  onPlay: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onDetails: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onPlay() }
      .testTag("video_card_${video.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail with duration badge and play icon overlay
      Box(
        modifier = Modifier
          .size(110.dp, 75.dp)
          .clip(RoundedCornerShape(10.dp))
      ) {
        AsyncImage(
          model = video.thumbnailUrl,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Play button overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(NeonCyan.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Play",
              tint = Color.Black,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Duration badge
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color.Black.copy(alpha = 0.85f),
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(4.dp)
        ) {
          Text(
            text = "${video.durationSeconds / 60}:${String.format("%02d", video.durationSeconds % 60)}",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Content info
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = video.title,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = TextPrimary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Platform Badge
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = getPlatformColor(video.platform).copy(alpha = 0.2f)
          ) {
            Text(
              text = video.platform,
              color = getPlatformColor(video.platform),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Text(
            text = video.quality,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )

          Text(
            text = "•",
            color = TextMuted,
            fontSize = 11.sp
          )

          Text(
            text = StorageStats.formatBytes(video.fileSizeBytes),
            color = TextMuted,
            fontSize = 11.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(video.timestamp)),
          color = TextMuted,
          fontSize = 10.sp
        )
      }

      // Actions Column
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = onFavoriteToggle,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (video.isFavorite) Color(0xFFFF4081) else TextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Options",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DarkSurfaceVariant)
          ) {
            DropdownMenuItem(
              text = { Text("Play Video", color = TextPrimary) },
              leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan) },
              onClick = {
                showMenu = false
                onPlay()
              }
            )
            DropdownMenuItem(
              text = { Text("Details & Codec", color = TextPrimary) },
              leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary) },
              onClick = {
                showMenu = false
                onDetails()
              }
            )
            DropdownMenuItem(
              text = { Text("Share Video", color = TextPrimary) },
              leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary) },
              onClick = {
                showMenu = false
                onShare()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete from Device", color = ErrorRed) },
              leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}
