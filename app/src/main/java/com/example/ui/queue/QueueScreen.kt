package com.example.ui.queue

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StorageStats
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import com.example.downloader.DownloadProgressState
import com.example.ui.downloader.getPlatformColor
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@Composable
fun QueueScreen(
  activeDownloads: List<VideoItem>,
  liveProgressMap: Map<Long, DownloadProgressState>,
  onPause: (Long) -> Unit,
  onCancel: (Long) -> Unit,
  onRetry: (VideoItem) -> Unit,
  onPlayCompleted: (VideoItem) -> Unit,
  completedVideos: List<VideoItem>,
  onNavigateToDownloader: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val arrowOffset by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrow"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Download Queue",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 22.sp,
          color = TextPrimary
        )
        Text(
          text = "${activeDownloads.size} active download${if (activeDownloads.size == 1) "" else "s"}",
          fontSize = 12.sp,
          color = if (activeDownloads.isNotEmpty()) NeonCyan else TextSecondary
        )
      }

      if (activeDownloads.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = NeonCyan.copy(alpha = 0.15f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier
                .size(16.dp)
                .graphicsLayer { translationY = arrowOffset }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Downloading",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("download_queue_list")
    ) {
      // ACTIVE DOWNLOADS SECTION
      if (activeDownloads.isNotEmpty()) {
        item {
          Text(
            text = "In Progress",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
          )
        }

        items(activeDownloads, key = { it.id }) { video ->
          val live = liveProgressMap[video.id]
          val progress = live?.progress ?: video.downloadProgress
          val speed = live?.speed ?: video.downloadSpeed
          val isPaused = video.downloadStatus == DownloadStatus.PAUSED.name
          val isFailed = video.downloadStatus == DownloadStatus.FAILED.name

          ActiveDownloadCard(
            video = video,
            progress = progress,
            speed = speed,
            isPaused = isPaused,
            isFailed = isFailed,
            downloadedBytes = live?.downloadedBytes ?: 0L,
            totalBytes = live?.totalBytes ?: video.fileSizeBytes,
            onPause = { onPause(video.id) },
            onCancel = { onCancel(video.id) },
            onRetry = { onRetry(video) }
          )
        }
      } else {
        // EMPTY ACTIVE QUEUE STATE
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(28.dp)
                )
              }
              Text(
                text = "Queue is Idle",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
              )
              Text(
                text = "No active downloads currently in progress.",
                fontSize = 12.sp,
                color = TextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              Button(
                onClick = onNavigateToDownloader,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
              ) {
                Text("Add New Video", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // RECENTLY COMPLETED DOWNLOADS
      if (completedVideos.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Completed & Ready Offline",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
          )
        }

        items(completedVideos.take(5), key = { it.id }) { completed ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(60.dp, 45.dp)
                  .clip(RoundedCornerShape(8.dp))
              ) {
                AsyncImage(
                  model = completed.thumbnailUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = completed.title,
                  fontWeight = FontWeight.Medium,
                  fontSize = 13.sp,
                  color = TextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Saved • ${StorageStats.formatBytes(completed.fileSizeBytes)}",
                    fontSize = 11.sp,
                    color = SuccessGreen
                  )
                }
              }

              IconButton(onClick = { onPlayCompleted(completed) }) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Play",
                  tint = NeonCyan
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun ActiveDownloadCard(
  video: VideoItem,
  progress: Float,
  speed: String,
  isPaused: Boolean,
  isFailed: Boolean,
  downloadedBytes: Long,
  totalBytes: Long,
  onPause: () -> Unit,
  onCancel: () -> Unit,
  onRetry: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      when {
        isFailed -> ErrorRed
        isPaused -> WarningOrange
        else -> NeonCyan.copy(alpha = 0.5f)
      }
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(70.dp, 50.dp)
            .clip(RoundedCornerShape(8.dp))
        ) {
          AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = video.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(2.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = getPlatformColor(video.platform).copy(alpha = 0.2f)
            ) {
              Text(
                text = video.platform,
                color = getPlatformColor(video.platform),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
              text = if (isFailed) "Failed" else if (isPaused) "Paused" else speed,
              fontSize = 11.sp,
              color = if (isFailed) ErrorRed else if (isPaused) WarningOrange else NeonCyan,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Actions: Pause / Retry & Cancel
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isFailed) {
            IconButton(onClick = onRetry) {
              Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = NeonCyan)
            }
          } else {
            IconButton(onClick = onPause) {
              Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume" else "Pause",
                tint = TextSecondary
              )
            }
          }

          IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ErrorRed)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { (progress / 100f).coerceIn(0f, 1f) },
        color = when {
          isFailed -> ErrorRed
          isPaused -> WarningOrange
          else -> NeonCyan
        },
        trackColor = DarkSurfaceVariant,
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Progress metrics row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${progress.toInt()}%",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )

        Text(
          text = if (totalBytes > 0) {
            "${StorageStats.formatBytes(downloadedBytes)} / ${StorageStats.formatBytes(totalBytes)}"
          } else {
            StorageStats.formatBytes(downloadedBytes)
          },
          fontSize = 11.sp,
          color = TextMuted
        )
      }
    }
  }
}
