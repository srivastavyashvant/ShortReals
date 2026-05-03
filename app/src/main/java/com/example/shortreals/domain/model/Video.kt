package com.example.shortreals.domain.model

data class Video(
    val id: String,
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val isDownloaded: Boolean = false,
    val localUri: String? = null
)
