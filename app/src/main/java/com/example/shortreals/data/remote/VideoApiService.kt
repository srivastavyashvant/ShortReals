package com.example.shortreals.data.remote

import com.example.shortreals.data.remote.dto.VideoApiResponse
import retrofit2.http.GET

interface VideoApiService {
    @GET("v3/b/69f6026baaba882197625327")
    suspend fun getVideos(): VideoApiResponse
}
