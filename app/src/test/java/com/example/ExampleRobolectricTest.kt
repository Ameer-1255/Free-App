package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.DownloadStatus
import com.example.data.model.VideoItem
import com.example.data.model.VideoPlatform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OmniSave", appName)
  }

  @Test
  fun `insert and retrieve video in database`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val dao = db.videoDao()

    val item = VideoItem(
      title = "Test Offline Video",
      sourceUrl = "https://example.com/test.mp4",
      platform = VideoPlatform.YOUTUBE.name,
      localFilePath = "/storage/test.mp4",
      thumbnailUrl = "https://example.com/thumb.jpg",
      durationSeconds = 120,
      fileSizeBytes = 5_000_000L,
      quality = "1080p",
      downloadStatus = DownloadStatus.COMPLETED.name
    )
    val id = dao.insertVideo(item)
    assertTrue(id > 0)

    val videos = dao.getAllVideos().first()
    assertTrue(videos.any { it.title == "Test Offline Video" })
  }
}
