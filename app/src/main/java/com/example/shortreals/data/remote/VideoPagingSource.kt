package com.example.shortreals.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.shortreals.data.remote.mapper.toDomain
import com.example.shortreals.domain.model.Video

class VideoPagingSource(
    private val apiService: VideoApiService
) : PagingSource<Int, Video>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        return try {
            val response = apiService.getVideos()
            val videos = response.record.videos.map { it.toDomain() }
            
            LoadResult.Page(
                data = videos,
                prevKey = null,
                nextKey = null // Single page for this API
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Video>): Int? {
        return null
    }
}
