package com.example.shortreals.presentation.reels

import androidx.media3.exoplayer.ExoPlayer
import androidx.work.WorkManager
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.usecase.DownloadVideoUseCase
import com.example.shortreals.domain.usecase.GetVideosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

import com.example.shortreals.domain.usecase.GetDownloadedVideosUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ReelsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getVideosUseCase: GetVideosUseCase
    private lateinit var getDownloadedVideosUseCase: GetDownloadedVideosUseCase
    private lateinit var downloadVideoUseCase: DownloadVideoUseCase
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var workManager: WorkManager
    private lateinit var viewModel: ReelsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getVideosUseCase = mock()
        getDownloadedVideosUseCase = mock()
        downloadVideoUseCase = mock()
        exoPlayer = mock()
        workManager = mock()
        
        whenever(getVideosUseCase.invoke()).thenReturn(emptyFlow())
        whenever(getDownloadedVideosUseCase.invoke()).thenReturn(emptyFlow())
        whenever(workManager.getWorkInfosByTagFlow("download")).thenReturn(emptyFlow())
        // Mock getWorkInfosByTag().get() for init block
        val mockFuture: com.google.common.util.concurrent.ListenableFuture<List<androidx.work.WorkInfo>> = mock()
        whenever(workManager.getWorkInfosByTag("download")).thenReturn(mockFuture)
        whenever(mockFuture.get()).thenReturn(emptyList())

        viewModel = ReelsViewModel(
            getVideosUseCase,
            getDownloadedVideosUseCase,
            downloadVideoUseCase,
            exoPlayer,
            workManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `downloadVideo triggers use case`() = runTest {
        val video = Video("1", "Title", "url", "thumb")
        
        viewModel.downloadVideo(video)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(downloadVideoUseCase).invoke(video)
    }
}
