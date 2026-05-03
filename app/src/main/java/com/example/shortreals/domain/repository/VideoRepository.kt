package com.example.shortreals.domain.repository

import androidx.paging.PagingData
import com.example.shortreals.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getVideosPaged(): Flow<PagingData<Video>>
    fun getDownloadedVideos(): Flow<List<Video>>
    suspend fun getVideoById(id: String): Video?
    suspend fun isVideoDownloaded(id: String): Boolean
}
