package com.datnht.image_compress.core

data class CompressOptions constructor(
    val targetExtensionPath: String? = null,
    val quality: Int = 100,
    val compressHeight: Int? = null,
    val compressWidth: Int? = null,
    @DirectoryType val directoryType: Int = CACHE_EXTERNAL_STORAGE
)