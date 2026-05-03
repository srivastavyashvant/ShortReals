package com.example.shortreals.data.remote.mapper

import com.example.shortreals.data.remote.dto.VideoDto
import com.example.shortreals.domain.model.Video

fun VideoDto.toDomain(): Video {
    return Video(
        id = id,
        title = title,
        videoUrl = url,
        thumbnailUrl = thumbnailUrl
    )
}
