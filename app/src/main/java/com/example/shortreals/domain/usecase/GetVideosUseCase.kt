package com.example.shortreals.domain.usecase

import androidx.paging.PagingData
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    operator fun invoke(): Flow<PagingData<Video>> {
        return repository.getVideosPaged()
    }
}
