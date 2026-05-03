package com.example.shortreals.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.shortreals.data.local.VideoDao
import com.example.shortreals.data.local.VideoEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class VideoDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val videoDao: VideoDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString("videoId") ?: return@withContext Result.failure()
        val videoUrl = inputData.getString("videoUrl") ?: return@withContext Result.failure()
        val title = inputData.getString("title") ?: return@withContext Result.failure()
        val thumbnailUrl = inputData.getString("thumbnailUrl") ?: return@withContext Result.failure()

        try {
            val request = Request.Builder().url(videoUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure()
            }

            val body = response.body ?: return@withContext Result.failure()
            val contentLength = body.contentLength()

            val downloadsDir = File(applicationContext.filesDir, "downloads").apply { mkdirs() }
            val fileName = videoId.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".mp4"
            val outputFile = File(downloadsDir, fileName)

            body.byteStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesCopied = 0L
                    var bytes = input.read(buffer)
                    var lastReportTime = 0L

                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastReportTime > 500 && contentLength > 0) {
                            val progress = (bytesCopied.toFloat() / contentLength.toFloat()) * 100
                            setProgress(workDataOf("progress" to progress, "videoId" to videoId))
                            lastReportTime = currentTime
                        }
                        
                        bytes = input.read(buffer)
                    }
                }
            }

            val entity = VideoEntity(
                id = videoId,
                title = title,
                remoteUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                localUri = outputFile.absolutePath,
                isDownloaded = true,
                downloadedAt = System.currentTimeMillis()
            )
            videoDao.upsertVideo(entity)

            setProgress(workDataOf("progress" to 100f, "videoId" to videoId))
            Result.success()

        } catch (e: Exception) {
            android.util.Log.e("VideoDownloadWorker", "Download failed for $videoId", e)
            Result.failure()
        }
    }
}
