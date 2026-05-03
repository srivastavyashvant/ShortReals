package com.example.shortreals.di

import android.content.Context
import androidx.room.Room
import com.example.shortreals.data.local.AppDatabase
import com.example.shortreals.data.local.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "shortreals.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao
    }
}
