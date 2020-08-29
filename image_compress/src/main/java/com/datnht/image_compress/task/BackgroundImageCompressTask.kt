package com.datnht.image_compress.task

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.NonNull
import androidx.work.*
import com.datnht.core.JsonManager
import com.datnht.image_compress.core.CompressOptions
import com.datnht.image_compress.core.ImageSource
import com.datnht.image_compress.utils.SDF
import com.datnht.image_compress.utils.convertBitmapToFile
import com.datnht.image_compress.utils.getBitmap
import com.datnht.image_compress.utils.getCompressed
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class BackgroundImageCompressTask constructor(
    @NonNull private val context: Context,
    @NonNull private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val COMPRESS_WORK_TASK = "COMPRESS_WORK_TASK"
        private const val WORK_IMAGE_SOURCE = "WORK_IMAGE_SOURCE"
        private const val WORK_COMPRESS_OPTION = "WORK_COMPRESS_OPTION"
        private const val WORK_INPUT_URI = "WORK_INPUT_URI"
        const val WORK_COMPRESS_PATH = "WORK_COMPRESS_PATH"
        const val WORK_COMPRESS_PROCESS = "WORK_COMPRESS_PROCESS"

        fun getCompressWorkRequest(
            imageSource: ImageSource,
            compressOptions: CompressOptions?
        ): OneTimeWorkRequest.Builder {
            var isUriInput = false
            if (imageSource is ImageSource.UriSource) {
                isUriInput = true
            }
            return OneTimeWorkRequestBuilder<BackgroundImageCompressTask>().setInputData(
                workDataOf(
                    WORK_IMAGE_SOURCE to convertImageSourceToJson(imageSource),
                    WORK_COMPRESS_OPTION to JsonManager.INSTANCE.objectToJson(compressOptions),
                    WORK_INPUT_URI to isUriInput
                )
            ).addTag(COMPRESS_WORK_TASK)
        }

        private fun convertImageSourceToJson(imageSource: ImageSource): String {
            val result = mutableListOf<String>()
            when (imageSource) {
                is ImageSource.PathSource -> {
                    result.addAll(imageSource.paths)
                }

                is ImageSource.UriSource -> {
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
        val jsonImageSource = inputData.getString(WORK_IMAGE_SOURCE)
        val jsonCompressOptions = inputData.getString(WORK_COMPRESS_OPTION)
        val isUriInput = inputData.getBoolean(WORK_INPUT_URI, false)
        if (jsonImageSource != null) {
            val firstUpdate = workDataOf(WORK_COMPRESS_PROCESS to 0)
            val lastUpdate = workDataOf(WORK_COMPRESS_PROCESS to 100)
            val compressPaths = mutableListOf<String>()
            val compressOptions =
                if (jsonCompressOptions == null) null else JsonManager.INSTANCE.jsonToObject<CompressOptions>(
                    jsonCompressOptions
                )
            setProgress(firstUpdate)
            val jobs = getImageSourcePaths(jsonImageSource, isUriInput).map {
                async {
                    val file: File = getCompressed(context, it, compressOptions) ?: return@async
                    compressPaths.add(file.absolutePath)
                }
            }
            jobs.awaitAll()
            setProgress(lastUpdate)
            Result.success(createWorkOutput(compressPaths))
        } else {
            Result.failure()
        }
    }

    private fun getImageSourcePaths(imageSource: String, isUriInput: Boolean): List<String> {
        val sourcePathsConvert = mutableListOf<String>()
        val listImageSource: List<String> = JsonManager.INSTANCE.jsonToList(imageSource)
        when (isUriInput) {
            false -> {
                sourcePathsConvert.addAll(listImageSource)
            }
            else -> {
                listImageSource.let {
                    var cacheDir: File? = context.externalCacheDir
                    if (cacheDir == null) //fall back
                        cacheDir = context.cacheDir
                    val rootDir: String = cacheDir?.absolutePath?.toString() + "/ImageUriTemp"
                    val root = File(rootDir)
                    if (!root.exists()) root.mkdirs()
                    val compressedTemp = File(root, SDF.format(Date()).toString() + ".jpg")

                    var bitmap: Bitmap?
                    it.forEach { uriString ->
                        bitmap = getBitmap(context, Uri.parse(uriString))
                        convertBitmapToFile(compressedTemp, bitmap ?: return@let)
                        sourcePathsConvert.add(compressedTemp.path)
                    }
                }
            }
        }
        return sourcePathsConvert
    }

    private fun createWorkOutput(compressPaths: List<String>) =
        Data.Builder().putString(WORK_COMPRESS_PATH, JsonManager.INSTANCE.listToJson(compressPaths)).build()
}