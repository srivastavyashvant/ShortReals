package com.example.shortreals.presentation.myreels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shortreals.presentation.components.EmptyStateComposable
import com.example.shortreals.presentation.components.VideoPlayerComposable
import com.example.shortreals.presentation.reels.ReelsViewModel

@Composable
fun MyReelsScreen(
    viewModel: MyReelsViewModel = hiltViewModel(),
    sharedPlayerViewModel: ReelsViewModel = hiltViewModel(),
    isInPipMode: Boolean = false
) {
    val downloadedVideos by viewModel.downloadedVideos.collectAsStateWithLifecycle(initialValue = emptyList())

    if (downloadedVideos.isEmpty()) {
        EmptyStateComposable(
            title = "No reels downloaded yet",
            message = "Go to the Reels tab and download some videos to watch them offline."
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { downloadedVideos.size })

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < downloadedVideos.size) {
            val video = downloadedVideos[pagerState.currentPage]
            sharedPlayerViewModel.playVideo(video)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (pagerState.currentPage < downloadedVideos.size) {
                sharedPlayerViewModel.pauseVideo(downloadedVideos[pagerState.currentPage].id)
            }
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val video = downloadedVideos[page]
        val isCurrentPage = pagerState.currentPage == page

        Box(modifier = Modifier.fillMaxSize()) {
            if (isCurrentPage) {
                VideoPlayerComposable(
                    exoPlayer = sharedPlayerViewModel.exoPlayer,
                    thumbnailUrl = video.thumbnailUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
