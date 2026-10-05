package com.example

import com.example.data.model.VideoPlatform
import com.example.downloader.UrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPlatformDetection() {
    val yt = UrlParser.detectPlatform("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    assertEquals(VideoPlatform.YOUTUBE, yt)

    val tiktok = UrlParser.detectPlatform("https://www.tiktok.com/@user/video/719283749281")
    assertEquals(VideoPlatform.TIKTOK, tiktok)

    val fb = UrlParser.detectPlatform("https://fb.watch/mX4819/")
    assertEquals(VideoPlatform.FACEBOOK, fb)

    val ig = UrlParser.detectPlatform("https://www.instagram.com/reel/C-test-reel/")
    assertEquals(VideoPlatform.INSTAGRAM, ig)

    val moviebox = UrlParser.detectPlatform("https://moviebox.app/trailer/sample")
    assertEquals(VideoPlatform.MOVIEBOX, moviebox)

    val direct = UrlParser.detectPlatform("https://example.com/video.mp4")
    assertEquals(VideoPlatform.DIRECT, direct)
  }

  @Test
  fun testUrlParsing() {
    val info = UrlParser.parseUrl("Check this video https://www.instagram.com/reel/C-test-reel/")
    assertEquals(VideoPlatform.INSTAGRAM, info.platform)
    assertTrue(info.availableFormats.isNotEmpty())
    assertNotNull(info.resolvedDownloadUrl)

    val tiktokInfo = UrlParser.parseUrl("https://www.tiktok.com/@dancer/video/72381923019")
    assertEquals(VideoPlatform.TIKTOK, tiktokInfo.platform)
    assertTrue(tiktokInfo.availableFormats.any { it.label.contains("No Watermark", ignoreCase = true) })
  }
}
