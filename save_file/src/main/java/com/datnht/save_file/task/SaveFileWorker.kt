package com.datnht.save_file.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.NonNull
import androidx.work.*
import com.datnht.core.FileSource
import com.datnht.core.JsonManager
import com.datnht.save_file.options.SaveFileOptions
import com.datnht.save_file.utils.decodeImageFromFiles
import com.datnht.save_file.utils.saveFile
import com.datnht.save_file.utils.saveImageToMediaStoreForShareable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class SaveFileWorker constructor(
    @NonNull private val context: Context,
    @NonNull private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val SAVE_WORK_TASK = "SAVE_WORK_TASK"
        private const val WORK_FILE_SOURCE = "WORK_FILE_SOURCE"
        private const val WORK_SAVE_OPTION = "WORK_SAVE_OPTION"
        private const val WORK_INPUT_URI = "WORK_INPUT_URI"

        fun getSaveWorkRequest(
            imageSource: FileSource,
            compressOptions: SaveFileOptions?
        ): OneTimeWorkRequest.Builder {
            var isUriInput = false
            if (imageSource is FileSource.UriSource) {
                isUriInput = true
            }
            return OneTimeWorkRequestBuilder<SaveFileWorker>().setInputData(
                workDataOf(
                    WORK_FILE_SOURCE to convertImageSourceToJson(imageSource),
                    WORK_SAVE_OPTION to JsonManager.INSTANCE.objectToJson(compressOptions),
                    WORK_INPUT_URI to isUriInput
                )
            ).addTag(SAVE_WORK_TASK)
        }

        private fun convertImageSourceToJson(imageSource: FileSource): String {
            val result = mutableListOf<String>()
            when (imageSource) {
                is FileSource.PathSource -> {
                    result.addAll(imageSource.paths)
                }

                is FileSource.UriSource -> {
                    result.addAll(imageSource.uris.map {
                        it.toString()
                    }.toList())
                }
            }
            return JsonManager.INSTANCE.listToJson(result)
        }
    }

    // default run on Dispatcher.Default -> For cal task
    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val jsonFileSource = inputData.getString(WORK_FILE_SOURCE)
        val jsonSaveOptions = inputData.getString(WORK_SAVE_OPTION)
        val isUriInput = inputData.getBoolean(WORK_INPUT_URI, false)
        if (jsonFileSource != null) {
            val saveOptions =
                if (jsonSaveOptions == null) null else JsonManager.INSTANCE.jsonToObject<SaveFileOptions>(
                    jsonSaveOptions
                )
            val jobs = getImageSourceBitmaps(jsonFileSource, isUriInput).map {
                async {
                    saveFile(context, applicationContext.contentResolver, it, saveOptions)
                }
            }
            jobs.awaitAll()
            Result.success()
        } else {
            Result.failure()
        }
    }

    private fun getImageSourceBitmaps(imageSource: String, isUriInput: Boolean): List<Bitmap> {
        val sourceBitmapsConvert = mutableListOf<Bitmap>()
        val listImageSource: List<String> = JsonManager.INSTANCE.jsonToList(imageSource)
        when (isUriInput) {
            false -> {
                listImageSource.forEach {
                    sourceBitmapsConvert.add(
                        decodeImageFromFiles(
                            it
                        )
                    )
                }
            }
            else -> {
                listImageSource.forEach {
                    sourceBitmapsConvert.add(
                        BitmapFactory.decodeStream(
                            applicationContext.contentResolver.openInputStream(Uri.parse(it))
                        )
                    )
                }
            }
        }
        return sourceBitmapsConvert
    }
}