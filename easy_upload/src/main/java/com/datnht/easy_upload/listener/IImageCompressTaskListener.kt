package com.datnht.easy_upload.listener

import java.io.File

interface IImageCompressTaskListener {
    fun onComplete(compressed: List<File?>)
    fun onError(error: Throwable?)
}