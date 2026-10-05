package com.example.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.ui.downloader.getPlatformColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun OfflinePlayerScreen(
  video: VideoItem,
  onClose: () -> Unit,
  onToggleFavorite: (VideoItem) -> Unit,
  onShowDetails: (VideoItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  BackHandler {
    onClose()
  }

  // Player UI states
  var isPlaying by remember { mutableStateOf(true) }
  var isBuffering by remember { mutableStateOf(true) }
  var playbackError by remember { mutableStateOf<String?>(null) }
  var currentPosition by remember { mutableLongStateOf(0L) }
  var duration by remember { mutableLongStateOf(video.durationSeconds * 1000L) }
  var bufferedPosition by remember { mutableLongStateOf(0L) }

  var showControls by remember { mutableStateOf(true) }
  var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
  var isLooping by remember { mutableStateOf(false) }
  var isMuted by remember { mutableStateOf(false) }
  var resizeModeIndex by remember { mutableStateOf(0) } // 0: FIT, 1: ZOOM (FILL), 2: FIXED RATIO
  var showSpeedMenu by remember { mutableStateOf(false) }

  val resizeModes = listOf(
    AspectRatioFrameLayout.RESIZE_MODE_FIT to "Fit",
    AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Crop/Fill",
    AspectRatioFrameLayout.RESIZE_MODE_FILL to "Stretch"
  )

  // Initialize Media3 ExoPlayer instance
  val exoPlayer = remember(context) {
    ExoPlayer.Builder(context).build().apply {
      playWhenReady = true
      repeatMode = Player.REPEAT_MODE_OFF

      val targetUri = if (video.localFilePath.isNotEmpty() && File(video.localFilePath).exists()) {
        Uri.fromFile(File(video.localFilePath))
      } else if (video.localFilePath.startsWith("http")) {
        Uri.parse(video.localFilePath)
      } else {
        Uri.parse(video.sourceUrl)
      }

      val mediaItem = MediaItem.fromUri(targetUri)
      setMediaItem(mediaItem)
      prepare()

      addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
          when (playbackState) {
            Player.STATE_BUFFERING -> {
              isBuffering = true
              playbackError = null
            }
            Player.STATE_READY -> {
              isBuffering = false
              playbackError = null
              if (this@apply.duration > 0) {
                duration = this@apply.duration
              }
            }
            Player.STATE_ENDED -> {
              isBuffering = false
              isPlaying = false
              showControls = true
            }
            Player.STATE_IDLE -> {
              isBuffering = false
            }
          }
        }

        override fun onIsPlayingChanged(playing: Boolean) {
          isPlaying = playing
        }

        override fun onPlayerError(error: PlaybackException) {
          isBuffering = false
          playbackError = "Playback error: ${error.errorCodeName} (${error.message?.take(40)})"
        }
      })
    }
  }

  // Auto-hide controls overlay after 4 seconds of inactivity
  LaunchedEffect(showControls, isPlaying) {
    if (showControls && isPlaying) {
      delay(4000)
      showControls = false
    }
  }

  // Periodic position and buffer tracking loop
  LaunchedEffect(exoPlayer) {
    while (true) {
      if (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.isPlaying) {
        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
        bufferedPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
        if (exoPlayer.duration > 0) {
          duration = exoPlayer.duration
        }
      }
      delay(250)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        showControls = !showControls
      }
      .testTag("offline_exoplayer_screen")
  ) {
    // AndroidView embedding ExoPlayer's PlayerView
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          useController = false // Custom Compose Material 3 overlay is used
          player = exoPlayer
          resizeMode = resizeModes[resizeModeIndex].first
          setBackgroundColor(android.graphics.Color.BLACK)
        }
      },
      update = { playerView ->
        playerView.player = exoPlayer
        playerView.resizeMode = resizeModes[resizeModeIndex].first
      },
      modifier = Modifier.fillMaxSize()
    )

    // Buffering Loading Indicator
    if (isBuffering && playbackError == null) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          color = NeonCyan,
          strokeWidth = 3.dp,
          modifier = Modifier.size(54.dp)
        )
      }
    }

    // Playback Error Overlay
    if (playbackError != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(48.dp)
          )
          Text(
            text = "Failed to Play Offline Video",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp
          )
          Text(
            text = playbackError ?: "Unknown error",
            color = TextMuted,
            fontSize = 12.sp
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = NeonCyan,
            modifier = Modifier.clickable {
              playbackError = null
              exoPlayer.prepare()
              exoPlayer.play()
            }
          ) {
            Text(
              text = "Retry",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    // CONTROLS OVERLAY
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.45f))
      ) {
        // TOP HEADER BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
              )
            )
            .padding(horizontal = 14.dp, vertical = 18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("exoplayer_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Close player",
              tint = Color.White
            )
          }

          Column(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 8.dp)
          ) {
            Text(
              text = video.title,
              color = Color.White,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = getPlatformColor(video.platform).copy(alpha = 0.25f)
              ) {
                Text(
                  text = video.platform,
                  color = getPlatformColor(video.platform),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              Text(
                text = "ExoPlayer • ${video.quality} • Offline Ready",
                color = NeonCyan,
                fontSize = 11.sp
              )
            }
          }

          // Favorite button
          IconButton(onClick = { onToggleFavorite(video) }) {
            Icon(
              imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (video.isFavorite) Color(0xFFFF4081) else Color.White
            )
          }

          // Share video
          IconButton(onClick = { shareVideoFile(context, video) }) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = Color.White
            )
          }

          // Details button
          IconButton(onClick = { onShowDetails(video) }) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Details",
              tint = Color.White
            )
          }
        }

        // CENTER CONTROLS: -10s Rewind, Play/Pause, +10s FastForward
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // -10s
          IconButton(
            onClick = {
              val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
              exoPlayer.seekTo(newPos)
              currentPosition = newPos
            },
            modifier = Modifier
              .size(52.dp)
              .background(Color.White.copy(alpha = 0.2f), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.FastRewind,
              contentDescription = "Rewind 10s",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(36.dp))

          // Play / Pause
          IconButton(
            onClick = {
              if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                isPlaying = false
              } else {
                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                  exoPlayer.seekTo(0L)
                }
                exoPlayer.play()
                isPlaying = true
              }
            },
            modifier = Modifier
              .size(74.dp)
              .background(NeonCyan, CircleShape)
              .testTag("exoplayer_play_pause_button")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.Black,
              modifier = Modifier.size(42.dp)
            )
          }

          Spacer(modifier = Modifier.width(36.dp))

          // +10s
          IconButton(
            onClick = {
              val targetMax = if (exoPlayer.duration > 0) exoPlayer.duration else duration
              val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(targetMax)
              exoPlayer.seekTo(newPos)
              currentPosition = newPos
            },
            modifier = Modifier
              .size(52.dp)
              .background(Color.White.copy(alpha = 0.2f), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.FastForward,
              contentDescription = "Forward 10s",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // BOTTOM CONTROLS PANEL
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
              )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          // Time scrubbing slider
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = formatTime(currentPosition),
              color = Color.White,
              fontSize = 12.sp,
              modifier = Modifier.width(46.dp)
            )

            Slider(
              value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
              onValueChange = { fraction ->
                val newPos = (fraction * duration).toLong()
                currentPosition = newPos
                exoPlayer.seekTo(newPos)
              },
              colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
              ),
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
                .testTag("exoplayer_seek_slider")
            )

            Text(
              text = formatTime(duration),
              color = Color.White.copy(alpha = 0.7f),
              fontSize = 12.sp,
              modifier = Modifier.width(46.dp)
            )
          }

          // Options Toolbar: Speed, Aspect Ratio, Loop, Mute/Volume
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Speed Dropdown Button
            Box {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.clickable { showSpeedMenu = true }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Speed",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${playbackSpeed}x",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              DropdownMenu(
                expanded = showSpeedMenu,
                onDismissRequest = { showSpeedMenu = false },
                modifier = Modifier.background(DarkBackground)
              ) {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "${speed}x",
                        color = if (playbackSpeed == speed) NeonCyan else Color.White,
                        fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    onClick = {
                      playbackSpeed = speed
                      showSpeedMenu = false
                      exoPlayer.playbackParameters = PlaybackParameters(speed)
                    }
                  )
                }
              }
            }

            // Aspect Ratio / Resize mode toggle
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color.White.copy(alpha = 0.15f),
              modifier = Modifier.clickable {
                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
              }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.AspectRatio,
                  contentDescription = "Aspect ratio",
                  tint = NeonCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = resizeModes[resizeModeIndex].second,
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            // Repeat Mode Button
            IconButton(
              onClick = {
                isLooping = !isLooping
                exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
              }
            ) {
              Icon(
                imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (isLooping) NeonCyan else Color.White.copy(alpha = 0.6f)
              )
            }

            // Mute / Unmute Volume
            IconButton(
              onClick = {
                isMuted = !isMuted
                exoPlayer.volume = if (isMuted) 0f else 1f
              }
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = if (isMuted) ErrorRed else Color.White.copy(alpha = 0.8f)
              )
            }

            // Quality Badge
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = NeonCyan.copy(alpha = 0.2f)
            ) {
              Text(
                text = video.quality,
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }

  // Lifecycle release
  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSeconds = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format("%02d:%02d", minutes, seconds)
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
