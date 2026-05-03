package com.example.shortreals.presentation.reels

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.shortreals.domain.model.Video
import com.example.shortreals.presentation.components.VideoPlayerComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelsScreen(
    viewModel: ReelsViewModel = hiltViewModel()
) {
    val videos: LazyPagingItems<Video> = viewModel.videos.collectAsLazyPagingItems()
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    
    // Observe downloaded videos to update UI
    val downloadedVideos by viewModel.downloadedVideos.collectAsState(initial = emptyList())
    val downloadedIds = remember(downloadedVideos) { downloadedVideos.map { it.id }.toSet() }

    // Handle ViewModel events (Toasts)
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ReelsViewModel.ReelsEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (videos.loadState.refresh is LoadState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF8B5CF6))
        }
        return
    }

    if (videos.itemCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No reels available", color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { videos.itemCount })

    // Sync page changes with video playback
    LaunchedEffect(pagerState.currentPage, downloadedIds) {
        val video = if (pagerState.currentPage < videos.itemCount) videos[pagerState.currentPage] else null
        video?.let {
            val isDownloaded = downloadedIds.contains(it.id)
            val videoToPlay = if (isDownloaded) {
                downloadedVideos.find { dv -> dv.id == it.id } ?: it
            } else it
            viewModel.playVideo(videoToPlay)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val video = if (pagerState.currentPage < videos.itemCount) videos[pagerState.currentPage] else null
            video?.let { viewModel.pauseVideo(it.id) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reels", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val video = videos[page]
            val isCurrentPage = pagerState.currentPage == page
            if (video != null) {
                val isDownloaded = downloadedIds.contains(video.id)
                
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isCurrentPage) {
                        VideoPlayerComposable(
                            exoPlayer = viewModel.exoPlayer,
                            thumbnailUrl = video.thumbnailUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Download Button Overlay - Added zIndex and ensured it's above the player
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 120.dp, end = 24.dp)
                                .zIndex(1f)
                        ) {
                            IconButton(
                                onClick = { 
                                    viewModel.downloadVideo(video)
                                },
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = if (isDownloaded) Color.Green else Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
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
    }
}
