package com.example.shortreals.data.repository

import com.example.shortreals.data.local.VideoDao
import com.example.shortreals.data.local.VideoEntity
import com.example.shortreals.data.remote.VideoApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VideoRepositoryImplTest {

    private lateinit var apiService: VideoApiService
    private lateinit var videoDao: VideoDao
    private lateinit var repository: VideoRepositoryImpl

    @Before
    fun setup() {
        apiService = mock()
        videoDao = mock()
        repository = VideoRepositoryImpl(apiService, videoDao)
    }

    @Test
    fun `getDownloadedVideos maps entities to domain models correctly`() = runTest {
        val entity = VideoEntity(
            id = "1",
            title = "Test",
            remoteUrl = "url",
            thumbnailUrl = "thumb",
            localUri = "local",
            isDownloaded = true
        )
        whenever(videoDao.getDownloadedVideos()).thenReturn(flowOf(listOf(entity)))

        val result = repository.getDownloadedVideos().first()

        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
        assertEquals("Test", result[0].title)
        assertTrue(result[0].isDownloaded)
    }
}
