package com.example.shortreals.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.shortreals.domain.model.Video

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val remoteUrl: String,
    val thumbnailUrl: String,
    val localUri: String? = null,
    val isDownloaded: Boolean = false,
    val downloadedAt: Long = System.currentTimeMillis()
)

fun VideoEntity.toDomain(): Video {
    return Video(
        id = id,
        title = title,
        videoUrl = localUri ?: remoteUrl,
        thumbnailUrl = thumbnailUrl,
        isDownloaded = isDownloaded,
        localUri = localUri
    )
}

fun Video.toEntity(): VideoEntity {
    return VideoEntity(
        id = id,
        title = title,
        remoteUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        localUri = localUri,
        isDownloaded = isDownloaded
    )
}
