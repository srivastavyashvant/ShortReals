package com.example.shortreals.presentation.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.usecase.GetDownloadedVideosUseCase
import com.example.shortreals.domain.usecase.DownloadVideoUseCase
import com.example.shortreals.domain.usecase.GetVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReelsViewModel @Inject constructor(
    getVideosUseCase: GetVideosUseCase,
    getDownloadedVideosUseCase: GetDownloadedVideosUseCase,
    private val downloadVideoUseCase: DownloadVideoUseCase,
    val exoPlayer: ExoPlayer,
    private val workManager: WorkManager
) : ViewModel() {

    val videos: Flow<PagingData<Video>> = getVideosUseCase()
        .cachedIn(viewModelScope)

    val downloadedVideos: Flow<List<Video>> = getDownloadedVideosUseCase()

    private val playbackPositions = mutableMapOf<String, Long>()

    val downloadProgressMap: StateFlow<Map<String, Float>> = workManager
        .getWorkInfosByTagFlow("download")
        .map { workInfos ->
            val map = mutableMapOf<String, Float>()
            for (info in workInfos) {
                if (info.state == WorkInfo.State.RUNNING) {
                    val progress = info.progress.getFloat("progress", 0f)
                    val videoId = info.progress.getString("videoId")
                    if (videoId != null) {
                        map[videoId] = progress
                    }
                }
            }
            map
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val toastedWorkIds = mutableSetOf<java.util.UUID>()

    init {
        viewModelScope.launch {
            // Mark all currently finished work as "already toasted" to avoid historical toasts
            workManager.getWorkInfosByTag("download").get().forEach { 
                if (it.state.isFinished) toastedWorkIds.add(it.id) 
            }

            workManager.getWorkInfosByTagFlow("download").collect { workInfos ->
                workInfos.forEach { info ->
                    if (info.state.isFinished && !toastedWorkIds.contains(info.id)) {
                        if (info.state == WorkInfo.State.SUCCEEDED) {
                            _events.emit(ReelsEvent.ShowToast("Downloaded Successfully"))
                        } else if (info.state == WorkInfo.State.FAILED) {
                            _events.emit(ReelsEvent.ShowToast("Download Failed"))
                        }
                        toastedWorkIds.add(info.id)
                    }
                }
            }
        }
    }

    private val _events = MutableSharedFlow<ReelsEvent>()
    val events = _events.asSharedFlow()

    sealed class ReelsEvent {
        data class ShowToast(val message: String) : ReelsEvent()
    }

    fun playVideo(video: Video) {
        val uri = video.localUri ?: video.videoUrl
        val mediaItem = MediaItem.fromUri(uri)
        
        if (exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString() != uri) {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        
        val position = playbackPositions[video.id] ?: 0L
        exoPlayer.seekTo(position)
        exoPlayer.play()
    }

    fun pauseVideo(videoId: String) {
        playbackPositions[videoId] = exoPlayer.currentPosition
        exoPlayer.pause()
    }

    fun downloadVideo(video: Video) {
        viewModelScope.launch {
            when (downloadVideoUseCase(video)) {
                is DownloadVideoUseCase.DownloadResult.AlreadyDownloaded -> {
                    _events.emit(ReelsEvent.ShowToast("Already Downloaded"))
                }
                is DownloadVideoUseCase.DownloadResult.Enqueued -> {
                    _events.emit(ReelsEvent.ShowToast("Download Started"))
                }
            }
        }
    }
}
