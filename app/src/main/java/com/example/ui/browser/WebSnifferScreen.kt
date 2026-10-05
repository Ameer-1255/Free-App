package com.example.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class WebBookmark(val name: String, val url: String)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebSnifferScreen(
  onMediaDetected: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentUrl by remember { mutableStateOf("https://m.youtube.com") }
  var inputUrl by remember { mutableStateOf("https://m.youtube.com") }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  var pageProgress by remember { mutableFloatStateOf(0f) }
  var detectedMediaUrl by remember { mutableStateOf<String?>(null) }
  var detectedMediaTitle by remember { mutableStateOf("Online Video Stream") }

  val keyboardController = LocalSoftwareKeyboardController.current

  val bookmarks = remember {
    listOf(
      WebBookmark("TikTok", "https://www.tiktok.com"),
      WebBookmark("YouTube", "https://m.youtube.com"),
      WebBookmark("Instagram", "https://www.instagram.com"),
      WebBookmark("Facebook", "https://m.facebook.com"),
      WebBookmark("MovieBox Stream", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
      WebBookmark("Archive.org", "https://archive.org/details/movies")
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    // TOP NAVIGATION TOOLBAR
    Surface(
      color = DarkSurface,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { webViewRef?.let { if (it.canGoBack()) it.goBack() } },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = { webViewRef?.let { if (it.canGoForward()) it.goForward() } },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Forward",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = { webViewRef?.reload() },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reload",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          // Address input bar
          OutlinedTextField(
            value = inputUrl,
            onValueChange = { inputUrl = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
              onGo = {
                keyboardController?.hide()
                val target = if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                  "https://$inputUrl"
                } else {
                  inputUrl
                }
                currentUrl = target
                webViewRef?.loadUrl(target)
              }
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = DarkOutline,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              cursorColor = NeonCyan
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .padding(horizontal = 4.dp)
              .testTag("browser_address_bar")
          )

          IconButton(
            onClick = {
              val home = "https://m.youtube.com"
              inputUrl = home
              currentUrl = home
              webViewRef?.loadUrl(home)
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = "Home",
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Bookmark chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(bookmarks) { bm ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = DarkSurfaceVariant,
              modifier = Modifier.clickable {
                inputUrl = bm.url
                currentUrl = bm.url
                webViewRef?.loadUrl(bm.url)
              }
            ) {
              Text(
                text = bm.name,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Loading progress indicator
    if (pageProgress in 0.01f..0.99f) {
      LinearProgressIndicator(
        progress = { pageProgress },
        color = NeonCyan,
        trackColor = DarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth().height(2.dp)
      )
    }

    // WEBVIEW CONTAINER
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              loadWithOverviewMode = true
              useWideViewPort = true
              mediaPlaybackRequiresUserGesture = false
              mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            }

            webChromeClient = object : WebChromeClient() {
              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                pageProgress = newProgress / 100f
              }
            }

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                url?.let {
                  inputUrl = it
                  currentUrl = it
                }
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                view?.title?.let {
                  if (it.isNotBlank()) detectedMediaTitle = it
                }
              }

              override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val reqUrl = request?.url?.toString() ?: ""
                val lower = reqUrl.lowercase()

                // Sniff media stream formats
                if (lower.contains(".mp4") || lower.contains(".webm") || lower.contains(".m3u8") || lower.contains("googlevideo.com/videoplayback")) {
                  if (detectedMediaUrl != reqUrl && !lower.contains(".png") && !lower.contains(".jpg")) {
                    detectedMediaUrl = reqUrl
                  }
                }
                return super.shouldInterceptRequest(view, request)
              }
            }

            loadUrl(currentUrl)
            webViewRef = this
          }
        },
        update = { wv ->
          webViewRef = wv
        },
        modifier = Modifier.fillMaxSize()
      )

      // FLOATING VIDEO SNIFFER DETECTED BANNER
      androidx.compose.animation.AnimatedVisibility(
        visible = detectedMediaUrl != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(16.dp)
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
          elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("sniffer_media_banner")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "🎬 Video Stream Detected!",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
              )
              Text(
                text = detectedMediaTitle,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
              onClick = {
                detectedMediaUrl?.let { media ->
                  onMediaDetected(media, detectedMediaTitle)
                  detectedMediaUrl = null
                }
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              modifier = Modifier.height(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            IconButton(
              onClick = { detectedMediaUrl = null },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
