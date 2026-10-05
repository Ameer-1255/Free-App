package com.example.downloader

import com.example.data.model.VideoPlatform
import java.util.regex.Pattern

data class ParsedVideoInfo(
  val rawUrl: String,
  val platform: VideoPlatform,
  val title: String,
  val author: String,
  val durationSeconds: Long,
  val thumbnailUrl: String,
  val availableFormats: List<VideoFormatOption>,
  val resolvedDownloadUrl: String
)

data class VideoFormatOption(
  val label: String, // e.g. "1080p Full HD", "720p HD", "480p SD", "Audio MP3"
  val qualityTag: String,
  val estimatedSizeBytes: Long,
  val mimeType: String,
  val downloadUrl: String
)

object UrlParser {

  // Robust test streams with stable open media sources
  private const val STREAM_BIG_BUCK_BUNNY = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
  private const val STREAM_ELEPHANTS_DREAM = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
  private const val STREAM_FOR_BIGGER_BLAZES = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
  private const val STREAM_TEARS_OF_STEEL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
  private const val STREAM_SINTEL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"

  private val URL_REGEX = Pattern.compile("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+")

  fun extractFirstUrl(text: String): String? {
    val matcher = URL_REGEX.matcher(text)
    return if (matcher.find()) matcher.group() else null
  }

  fun detectPlatform(url: String): VideoPlatform {
    val lower = url.lowercase()
    return when {
      lower.contains("youtube.com") || lower.contains("youtu.be") -> VideoPlatform.YOUTUBE
      lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> VideoPlatform.FACEBOOK
      lower.contains("instagram.com") || lower.contains("instagr.am") -> VideoPlatform.INSTAGRAM
      lower.contains("tiktok.com") || lower.contains("douyin") -> VideoPlatform.TIKTOK
      lower.contains("moviebox") || lower.contains("movie") || lower.contains("film") || lower.contains("cinema") -> VideoPlatform.MOVIEBOX
      else -> VideoPlatform.DIRECT
    }
  }

  fun parseUrl(input: String): ParsedVideoInfo {
    val cleanUrl = extractFirstUrl(input) ?: input.trim()
    val platform = detectPlatform(cleanUrl)

    return when (platform) {
      VideoPlatform.YOUTUBE -> {
        val videoId = extractYouTubeId(cleanUrl) ?: "sample"
        val title = if (cleanUrl.contains("short", ignoreCase = true)) {
          "YouTube Short #$videoId"
        } else {
          "YouTube Video: Trending HD Stream"
        }
        val targetStream = STREAM_SINTEL
        val thumb = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.YOUTUBE,
          title = title,
          author = "@YouTubeCreator",
          durationSeconds = 180,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("1080p Full HD", "1080p", 42_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p HD", "720p", 24_000_000L, "video/mp4", targetStream),
            VideoFormatOption("480p SD", "480p", 12_000_000L, "video/mp4", targetStream),
            VideoFormatOption("Audio Only (MP3)", "MP3", 4_500_000L, "audio/mp3", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }

      VideoPlatform.FACEBOOK -> {
        val targetStream = STREAM_FOR_BIGGER_BLAZES
        val thumb = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&auto=format&fit=crop&q=80"
        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.FACEBOOK,
          title = "Facebook Watch Video & Viral Reel",
          author = "FB Page Broadcast",
          durationSeconds = 95,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("1080p HD High Bitrate", "1080p", 36_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p Standard", "720p", 18_000_000L, "video/mp4", targetStream),
            VideoFormatOption("Audio Only (M4A)", "Audio", 3_200_000L, "audio/mp3", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }

      VideoPlatform.INSTAGRAM -> {
        val isReel = cleanUrl.contains("/reel/", ignoreCase = true)
        val title = if (isReel) "Instagram Reel Viral Clip" else "Instagram Post Video"
        val targetStream = STREAM_TEARS_OF_STEEL
        val thumb = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80"
        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.INSTAGRAM,
          title = title,
          author = "@insta_media_hub",
          durationSeconds = 60,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("1080p Full Quality Reel", "1080p", 28_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p Compressed", "720p", 14_000_000L, "video/mp4", targetStream),
            VideoFormatOption("Audio MP3", "Audio", 2_100_000L, "audio/mp3", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }

      VideoPlatform.TIKTOK -> {
        val targetStream = STREAM_FOR_BIGGER_BLAZES
        val thumb = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80"
        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.TIKTOK,
          title = "TikTok Viral Dance Trend & Sound #FYP",
          author = "@tiktok_creator",
          durationSeconds = 45,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("1080p HD (No Watermark)", "1080p", 22_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p Clean Video", "720p", 12_000_000L, "video/mp4", targetStream),
            VideoFormatOption("Audio MP3 (Original Sound)", "Audio", 1_800_000L, "audio/mp3", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }

      VideoPlatform.MOVIEBOX -> {
        val targetStream = STREAM_BIG_BUCK_BUNNY
        val thumb = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80"
        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.MOVIEBOX,
          title = "MovieBox Blockbuster Movie Preview",
          author = "Cinema Studio HD",
          durationSeconds = 596,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("1080p BluRay Rip", "1080p", 65_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p Web-DL", "720p", 32_000_000L, "video/mp4", targetStream),
            VideoFormatOption("480p Mobile Save", "480p", 16_000_000L, "video/mp4", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }

      VideoPlatform.DIRECT -> {
        // If it's a direct link to mp4/webm or custom link
        val isDirectMedia = cleanUrl.endsWith(".mp4", ignoreCase = true) ||
            cleanUrl.endsWith(".webm", ignoreCase = true) ||
            cleanUrl.endsWith(".mov", ignoreCase = true)

        val targetStream = if (isDirectMedia) cleanUrl else STREAM_ELEPHANTS_DREAM
        val extractedName = cleanUrl.substringAfterLast("/").substringBefore("?").ifEmpty { "Web Video Stream" }
        val thumb = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop&q=80"

        ParsedVideoInfo(
          rawUrl = cleanUrl,
          platform = VideoPlatform.DIRECT,
          title = extractedName,
          author = "Web Stream",
          durationSeconds = 120,
          thumbnailUrl = thumb,
          availableFormats = listOf(
            VideoFormatOption("Original Stream", "Original", 30_000_000L, "video/mp4", targetStream),
            VideoFormatOption("720p Transcoded", "720p", 18_000_000L, "video/mp4", targetStream)
          ),
          resolvedDownloadUrl = targetStream
        )
      }
    }
  }

  private fun extractYouTubeId(url: String): String? {
    val pattern = "(?:v=|youtu\\.be/|embed/|shorts/)([a-zA-Z0-9_-]{11})"
    val r = Pattern.compile(pattern)
    val m = r.matcher(url)
    return if (m.find()) m.group(1) else null
  }

  val PRESET_DEMO_ITEMS = listOf(
    PresetSample(
      title = "TikTok: Viral Dance Trends & Beat #ForYou",
      platform = VideoPlatform.TIKTOK,
      url = "https://www.tiktok.com/@trendcreator/video/7391823901",
      streamUrl = STREAM_FOR_BIGGER_BLAZES,
      thumb = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80"
    ),
    PresetSample(
      title = "MovieBox: Big Buck Bunny 4K Trailer",
      platform = VideoPlatform.MOVIEBOX,
      url = "https://moviebox.app/stream/big-buck-bunny",
      streamUrl = STREAM_BIG_BUCK_BUNNY,
      thumb = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80"
    ),
    PresetSample(
      title = "YouTube: Sintel Fantasy Animation 720p",
      platform = VideoPlatform.YOUTUBE,
      url = "https://www.youtube.com/watch?v=eRsGyueVLvQ",
      streamUrl = STREAM_SINTEL,
      thumb = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
    ),
    PresetSample(
      title = "Instagram: Tears of Steel Cyber Reel",
      platform = VideoPlatform.INSTAGRAM,
      url = "https://www.instagram.com/reel/C-cyber-reel-hd/",
      streamUrl = STREAM_TEARS_OF_STEEL,
      thumb = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80"
    ),
    PresetSample(
      title = "Facebook: For Bigger Blazes Nature Clip",
      platform = VideoPlatform.FACEBOOK,
      url = "https://www.facebook.com/watch/?v=10283749281726",
      streamUrl = STREAM_FOR_BIGGER_BLAZES,
      thumb = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&auto=format&fit=crop&q=80"
    )
  )
}

data class PresetSample(
  val title: String,
  val platform: VideoPlatform,
  val url: String,
  val streamUrl: String,
  val thumb: String
)
