package com.datnht.image_compress.task

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.datnht.image_compress.options.CompressOptions
import com.datnht.core.FileSource
import com.datnht.image_compress.listener.IImageCompressTaskListener
import com.datnht.image_compress.utils.SDF
import com.datnht.image_compress.utils.convertBitmapToFile
import com.datnht.image_compress.utils.getBitmap
import com.datnht.image_compress.utils.getCompressed
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ForegroundImageCompressTask constructor(
    private val context: Context,
    private var fileSource: FileSource,
    private var compressOptions: CompressOptions?,
    private val iImageCompressTaskListener: IImageCompressTaskListener? = null
) : Runnable {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val result: MutableList<String> = ArrayList()

    override fun run() {
        val originalPaths = mutableListOf<String>()
        try {
            when(fileSource) {
                is FileSource.UriSource -> {
                    (fileSource as? FileSource.UriSource)?.uris?.let {
                        var cacheDir: File? = context.externalCacheDir
                        if (cacheDir == null) //fall back
                            cacheDir = context.cacheDir
                        val rootDir: String = cacheDir?.absolutePath?.toString() + "/ImageUriTemp"
                        val root = File(rootDir)
                        if (!root.exists()) root.mkdirs()
                        val compressedTemp = File(root, SDF.format(Date()).toString() + ".jpg")

                        var bitmap: Bitmap?
                        it.forEach { uri ->
                            bitmap = getBitmap(
                                context,
                                uri
                            )
                            convertBitmapToFile(
                                compressedTemp,
                                bitmap ?: return@let
                            )
                            originalPaths.add(compressedTemp.path)
                        }
                    }
                }
                is FileSource.PathSource -> {
                    originalPaths.addAll((fileSource as FileSource.PathSource).paths)
                }
            }
            for (path in originalPaths) {
                val file: File = getCompressed(
                    context,
                    path,
                    compressOptions
                ) ?: return
                result.add(file.path)
            }
            //use Handler to post the result back to the main Thread
            handler.post {
                iImageCompressTaskListener?.onComplete(
                    result
                )
            }
        } catch (ex: IOException) {
            //There was an error, report the error back through the callback
            handler.post {
                iImageCompressTaskListener?.onError(
                    ex
                )
            }
        }
    }
}