package com.example.shortreals.domain.usecase

import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadedVideosUseCase @Inject constructor(
    private val repository: VideoRepository
) {
    operator fun invoke(): Flow<List<Video>> {
        return repository.getDownloadedVideos()
    }
}
