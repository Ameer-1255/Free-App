package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
  @Query("SELECT * FROM videos ORDER BY timestamp DESC")
  fun getAllVideos(): Flow<List<VideoItem>>

  @Query("SELECT * FROM videos WHERE downloadStatus = 'COMPLETED' ORDER BY timestamp DESC")
  fun getCompletedVideos(): Flow<List<VideoItem>>

  @Query("SELECT * FROM videos WHERE downloadStatus = 'DOWNLOADING' OR downloadStatus = 'QUEUED' OR downloadStatus = 'PAUSED' ORDER BY timestamp DESC")
  fun getActiveDownloads(): Flow<List<VideoItem>>

  @Query("SELECT * FROM videos WHERE platform = :platform AND downloadStatus = 'COMPLETED' ORDER BY timestamp DESC")
  fun getCompletedVideosByPlatform(platform: String): Flow<List<VideoItem>>

  @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
  fun getVideoByIdFlow(id: Long): Flow<VideoItem?>

  @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
  suspend fun getVideoById(id: Long): VideoItem?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVideo(video: VideoItem): Long

  @Update
  suspend fun updateVideo(video: VideoItem)

  @Query("UPDATE videos SET downloadProgress = :progress, downloadStatus = :status, downloadSpeed = :speed, fileSizeBytes = :fileSize WHERE id = :id")
  suspend fun updateProgress(id: Long, progress: Float, status: String, speed: String, fileSize: Long)

  @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun updateFavorite(id: Long, isFavorite: Boolean)

  @Query("UPDATE videos SET title = :newTitle WHERE id = :id")
  suspend fun updateTitle(id: Long, newTitle: String)

  @Query("DELETE FROM videos WHERE id = :id")
  suspend fun deleteVideoById(id: Long)

  @Query("DELETE FROM videos WHERE downloadStatus = 'COMPLETED'")
  suspend fun deleteAllCompletedVideos()
}
