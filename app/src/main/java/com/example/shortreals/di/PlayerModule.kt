package com.example.shortreals.di

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "media_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100 MB
        val databaseProvider = StandaloneDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    @Provides
    @Singleton
    fun provideCacheDataSourceFactory(
        @ApplicationContext context: Context,
        cache: SimpleCache,
        okHttpClient: OkHttpClient
    ): CacheDataSource.Factory {
        val upstreamFactory = OkHttpDataSource.Factory(okHttpClient)
        val defaultDataSourceFactory = DefaultDataSource.Factory(context, upstreamFactory)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        cacheDataSourceFactory: CacheDataSource.Factory
    ): ExoPlayer {
        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false
            }
    }
}
