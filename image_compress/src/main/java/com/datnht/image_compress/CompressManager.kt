package com.datnht.image_compress

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.datnht.image_compress.options.CompressOptions
import com.datnht.core.FileSource
import com.datnht.core.JsonManager
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
        fileSource: FileSource,
        options: CompressOptions? = null,
        iImageCompressTaskListener: IImageCompressTaskListener? = null
    ) {
        executorService.execute(
            ForegroundImageCompressTask(
                context,
                fileSource,
                options,
                iImageCompressTaskListener
            )
        )
    }

    fun backgroundCompressWork(
        context: Context,
        workManager: WorkManager,
        fileSource: FileSource,
        options: CompressOptions? = null
    ) {

        val workRequest =
            BackgroundImageCompressTask.getCompressWorkRequest(fileSource, options).build()

        workManager.enqueueUniqueWork(
            BackgroundImageCompressTask.COMPRESS_WORK_TASK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getCompressWork(workManager: WorkManager) =
        workManager.getWorkInfosByTagLiveData(BackgroundImageCompressTask.COMPRESS_WORK_TASK)

    fun onDestroyTask(workManager: WorkManager) {
        executorService?.shutdown()
        workManager.cancelAllWorkByTag(BackgroundImageCompressTask.COMPRESS_WORK_TASK)
    }
}