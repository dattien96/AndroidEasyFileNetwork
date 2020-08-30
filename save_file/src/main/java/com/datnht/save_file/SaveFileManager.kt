package com.datnht.save_file

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.datnht.core.FileSource
import com.datnht.save_file.options.SaveFileOptions
import com.datnht.save_file.task.SaveFileWorker

class SaveFileManager private constructor() {
    companion object {
        fun getSaveFileManager() = SaveFileManager()
    }

    fun saveFileWork(
        context: Context,
        workManager: WorkManager,
        fileSource: FileSource,
        options: SaveFileOptions? = null
    ) {

        val workRequest =
            SaveFileWorker.getSaveWorkRequest(fileSource, options).build()

        workManager.enqueueUniqueWork(
            SaveFileWorker.SAVE_WORK_TASK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getSaveWork(workManager: WorkManager) =
        workManager.getWorkInfosByTagLiveData(SaveFileWorker.SAVE_WORK_TASK)

    fun onDestroyTask(workManager: WorkManager) {
        workManager.cancelAllWorkByTag(SaveFileWorker.SAVE_WORK_TASK)
    }
}