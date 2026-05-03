package com.example.shortreals.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.shortreals.data.local.VideoDao
import com.example.shortreals.data.local.toDomain
import com.example.shortreals.data.remote.VideoApiService
import com.example.shortreals.data.remote.VideoPagingSource
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val apiService: VideoApiService,
    private val videoDao: VideoDao
) : VideoRepository {

    override fun getVideosPaged(): Flow<PagingData<Video>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { VideoPagingSource(apiService) }
        ).flow
    }

    override fun getDownloadedVideos(): Flow<List<Video>> {
        return videoDao.getDownloadedVideos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getVideoById(id: String): Video? {
        return videoDao.getVideoById(id)?.toDomain()
    }

    override suspend fun isVideoDownloaded(id: String): Boolean {
        val video = videoDao.getVideoById(id)
        return video?.isDownloaded == true
    }
}
