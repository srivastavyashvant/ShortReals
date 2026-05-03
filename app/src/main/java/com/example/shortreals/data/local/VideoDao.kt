package com.example.shortreals.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isDownloaded = 1 ORDER BY downloadedAt DESC")
    fun getDownloadedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: VideoEntity): Unit

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String): Unit
}
