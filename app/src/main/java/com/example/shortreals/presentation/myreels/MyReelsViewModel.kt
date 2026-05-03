package com.example.shortreals.presentation.myreels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shortreals.domain.model.Video
import com.example.shortreals.domain.usecase.GetDownloadedVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyReelsViewModel @Inject constructor(
    getDownloadedVideosUseCase: GetDownloadedVideosUseCase
) : ViewModel() {

    val downloadedVideos: StateFlow<List<Video>> = getDownloadedVideosUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
