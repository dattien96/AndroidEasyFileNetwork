package com.datnht.easy_upload.task

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.datnht.easy_upload.listener.IImageCompressTaskListener
import com.datnht.easy_upload.utils.getCompressed
import java.io.File
import java.io.IOException


class ImageCompressTask constructor(
    private val context: Context,
    private var originalPaths: List<String>,
    private val iImageCompressTaskListener: IImageCompressTaskListener? = null
) : Runnable {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val result: MutableList<File?> = ArrayList()

    override fun run() {
        try {

            for (path in originalPaths) {
                val file: File = getCompressed(context, path, quality = 10) ?: return
                result.add(file)
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