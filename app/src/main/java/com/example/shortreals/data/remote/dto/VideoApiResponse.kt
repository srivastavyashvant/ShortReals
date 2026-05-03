package com.example.shortreals.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VideoApiResponse(
    @SerializedName("record") val record: RecordDto
)

data class RecordDto(
    @SerializedName("videos") val videos: List<VideoDto>
)

data class VideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String
)
