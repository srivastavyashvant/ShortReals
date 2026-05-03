package com.example.shortreals.domain.usecase

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.repository.VideoRepository
import com.example.shortreals.worker.VideoDownloadWorker
import javax.inject.Inject

class DownloadVideoUseCase @Inject constructor(
    private val repository: VideoRepository,
    private val workManager: WorkManager
) {
    suspend operator fun invoke(video: Video): DownloadResult {
        val isDownloaded = repository.isVideoDownloaded(video.id)
        if (isDownloaded) return DownloadResult.AlreadyDownloaded

        val inputData = Data.Builder()
            .putString("videoId", video.id)
            .putString("videoUrl", video.videoUrl)
            .putString("title", video.title)
            .putString("thumbnailUrl", video.thumbnailUrl)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("download")
            .addTag("download_${video.id}")
            .build()

        workManager.enqueue(downloadRequest)
        return DownloadResult.Enqueued
    }

    sealed class DownloadResult {
        object AlreadyDownloaded : DownloadResult()
        object Enqueued : DownloadResult()
    }
}
