package com.datnht.image_compress.listener

interface IImageCompressTaskListener {
    fun onComplete(compressedPaths: List<String>)
    fun onError(error: Throwable?)
}