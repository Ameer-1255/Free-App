package com.example.ui.downloader

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.StorageStats
import com.example.downloader.ParsedVideoInfo
import com.example.downloader.PresetSample
import com.example.downloader.UrlParser
import com.example.downloader.VideoFormatOption
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PlatformFacebook
import com.example.ui.theme.PlatformInstagram
import com.example.ui.theme.PlatformMovieBox
import com.example.ui.theme.PlatformTikTok
import com.example.ui.theme.PlatformYouTube
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloaderScreen(
  urlInput: String,
  onUrlChange: (String) -> Unit,
  onClearUrl: () -> Unit,
  onAnalyzeUrl: () -> Unit,
  isAnalyzing: Boolean,
  analysisResult: ParsedVideoInfo?,
  analysisError: String?,
  selectedFormat: VideoFormatOption?,
  onSelectFormat: (VideoFormatOption) -> Unit,
  onStartDownload: () -> Unit,
  onQuickPreset: (PresetSample) -> Unit,
  isOfflineMode: Boolean,
  onToggleOfflineMode: () -> Unit,
  onOpenAdmin: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Top header with Offline Switch and Admin Console
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "OmniSave HD",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = TextPrimary
          )
          Text(
            text = "YouTube • TikTok • Instagram • Facebook • MovieBox",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Offline mode toggle
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isOfflineMode) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isOfflineMode) NeonCyan else DarkOutline
            )
          ) {
            Row(
              modifier = Modifier
                .clickable { onToggleOfflineMode() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .background(if (isOfflineMode) NeonCyan else TextMuted, CircleShape)
              )
              Text(
                text = if (isOfflineMode) "Offline" else "Online",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOfflineMode) NeonCyan else TextSecondary
              )
            }
          }

          // Admin Console Button
          IconButton(
            onClick = onOpenAdmin,
            modifier = Modifier
              .size(36.dp)
              .background(DarkSurfaceVariant, CircleShape)
              .testTag("open_admin_button")
          ) {
            Icon(
              imageVector = Icons.Default.AdminPanelSettings,
              contentDescription = "Admin Console",
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Hero Banner Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.dp,
            Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.6f))),
            RoundedCornerShape(18.dp)
          )
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color(0xFF00363D).copy(alpha = 0.8f),
                  Color(0xFF2E1065).copy(alpha = 0.8f)
                )
              )
            )
            .padding(18.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Fast Offline Video Saver",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Paste any media link or tap a demo clip below to start saving in 1080p Full HD.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(36.dp)
            )
          }
        }
      }
    }

    // URL INPUT BAR
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Video URL / Link",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            placeholder = {
              Text("https://www.youtube.com, instagram, facebook...", color = TextMuted, fontSize = 13.sp)
            },
            singleLine = true,
            trailingIcon = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (urlInput.isNotEmpty()) {
                  IconButton(onClick = onClearUrl) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear",
                      tint = TextMuted,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                IconButton(
                  onClick = {
                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.primaryClip?.let { data ->
                      if (data.itemCount > 0) {
                        val text = data.getItemAt(0).text?.toString() ?: ""
                        onUrlChange(text)
                      }
                    }
                  },
                  modifier = Modifier.testTag("paste_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = DarkOutline,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              cursorColor = NeonCyan
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("video_url_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = onAnalyzeUrl,
            enabled = !isAnalyzing && urlInput.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonCyan,
              disabledContainerColor = DarkSurfaceVariant
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("analyze_video_button")
          ) {
            if (isAnalyzing) {
              CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Resolving Media Stream...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analyze & Get Formats", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }

          if (analysisError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = analysisError,
              color = ErrorRed,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 4.dp)
            )
          }
        }
      }
    }

    // VIDEO ANALYSIS RESULT CARD
    if (analysisResult != null) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("analysis_result_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = getPlatformColor(analysisResult.platform.name).copy(alpha = 0.2f)
              ) {
                Text(
                  text = analysisResult.platform.displayName,
                  color = getPlatformColor(analysisResult.platform.name),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }

              Text(
                text = "${analysisResult.durationSeconds / 60}m ${analysisResult.durationSeconds % 60}s",
                color = TextSecondary,
                fontSize = 12.sp
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thumbnail and Title
            Row(modifier = Modifier.fillMaxWidth()) {
              AsyncImage(
                model = analysisResult.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(90.dp, 65.dp)
                  .clip(RoundedCornerShape(8.dp))
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = analysisResult.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = analysisResult.author,
                  fontSize = 12.sp,
                  color = TextMuted
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "Choose Quality / Resolution:",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Resolution selection chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              analysisResult.availableFormats.forEach { format ->
                val isSelected = selectedFormat == format
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceVariant,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) NeonCyan else DarkOutline
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFormat(format) }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(16.dp)
                          .background(
                            if (isSelected) NeonCyan else Color.Transparent,
                            CircleShape
                          )
                          .border(2.dp, if (isSelected) NeonCyan else DarkOutline, CircleShape)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                        text = format.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else TextPrimary,
                        fontSize = 13.sp
                      )
                    }

                    Text(
                      text = StorageStats.formatBytes(format.estimatedSizeBytes),
                      fontSize = 12.sp,
                      color = TextSecondary
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = onStartDownload,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("confirm_download_button")
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color.Black
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Download for Offline Viewing",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }

    // SUPPORTED PLATFORMS QUICK LAUNCH
    item {
      Text(
        text = "Supported Video Hubs",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PlatformBadgeCard(
          name = "YouTube",
          tag = "Shorts & HD",
          color = PlatformYouTube,
          icon = Icons.Default.PlayCircle,
          onClick = { onUrlChange("https://www.youtube.com/watch?v=eRsGyueVLvQ") },
          modifier = Modifier.weight(1f)
        )
        PlatformBadgeCard(
          name = "TikTok",
          tag = "Clean & No Watermark",
          color = PlatformTikTok,
          icon = Icons.Default.Bolt,
          onClick = { onUrlChange("https://www.tiktok.com/@trendcreator/video/7391823901") },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PlatformBadgeCard(
          name = "Instagram",
          tag = "Reels & Posts",
          color = PlatformInstagram,
          icon = Icons.Default.VideoLibrary,
          onClick = { onUrlChange("https://www.instagram.com/reel/C-t-reel-example") },
          modifier = Modifier.weight(1f)
        )
        PlatformBadgeCard(
          name = "Facebook",
          tag = "Watch & Clips",
          color = PlatformFacebook,
          icon = Icons.Default.PlayCircle,
          onClick = { onUrlChange("https://facebook.com/watch/?v=98273641029") },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PlatformBadgeCard(
          name = "MovieBox",
          tag = "Movies & Series",
          color = PlatformMovieBox,
          icon = Icons.Default.Movie,
          onClick = { onUrlChange("https://moviebox.app/trailer/big-buck-bunny") },
          modifier = Modifier.weight(1f)
        )
        PlatformBadgeCard(
          name = "Direct Stream",
          tag = "MP4 / WebM / M3U8",
          color = NeonCyan,
          icon = Icons.Default.PlayCircle,
          onClick = { onUrlChange("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // QUICK TEST & DEMO PRESETS
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Quick Demo / One-Tap Downloads",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }
      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(UrlParser.PRESET_DEMO_ITEMS) { preset ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
              .width(220.dp)
              .clickable { onQuickPreset(preset) }
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(95.dp)
                  .clip(RoundedCornerShape(8.dp))
              ) {
                AsyncImage(
                  model = preset.thumb,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = getPlatformColor(preset.platform.name).copy(alpha = 0.85f),
                  modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                ) {
                  Text(
                    text = preset.platform.displayName,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = preset.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Tap to load",
                  color = NeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
                Icon(
                  imageVector = Icons.Default.Download,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(14.dp)
                )
              }
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

@Composable
private fun PlatformBadgeCard(
  name: String,
  tag: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = name,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = tag,
          fontSize = 11.sp,
          color = TextMuted
        )
      }
    }
  }
}

fun getPlatformColor(platformName: String): Color {
  return when (platformName.uppercase()) {
    "YOUTUBE" -> PlatformYouTube
    "FACEBOOK" -> PlatformFacebook
    "INSTAGRAM" -> PlatformInstagram
    "TIKTOK" -> PlatformTikTok
    "MOVIEBOX" -> PlatformMovieBox
    else -> NeonCyan
  }
}
