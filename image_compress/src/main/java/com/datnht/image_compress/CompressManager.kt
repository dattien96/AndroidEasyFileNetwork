package com.datnht.image_compress

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.datnht.image_compress.core.CompressOptions
import com.datnht.image_compress.core.ImageSource
import com.datnht.image_compress.listener.IImageCompressTaskListener
import com.datnht.image_compress.task.BackgroundImageCompressTask
import com.datnht.image_compress.task.ForegroundImageCompressTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CompressManager private constructor() {
    companion object {
        fun getCompressManager() = CompressManager()
    }

    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)

    fun foregroundCompress(
        context: Context,
        imageSource: ImageSource,
        options: CompressOptions? = null,
        iImageCompressTaskListener: IImageCompressTaskListener? = null
    ) {
        executorService.execute(
            ForegroundImageCompressTask(
                context,
                imageSource,
                options,
                iImageCompressTaskListener
            )
        )
    }

    fun backgroundCompressWork(
        context: Context,
        workManager: WorkManager,
        imageSource: ImageSource,
        options: CompressOptions? = null
    ) {

        val workRequest =
            BackgroundImageCompressTask.getCompressWorkRequest(imageSource, options).build()

        workManager.enqueueUniqueWork(
            BackgroundImageCompressTask.COMPRESS_WORK_TASK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getCompressWork(workManager: WorkManager) =
        workManager.getWorkInfosByTagLiveData(BackgroundImageCompressTask.COMPRESS_WORK_TASK)

    fun onDestroyTask() {
        executorService.shutdown()
    }
}