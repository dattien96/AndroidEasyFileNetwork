package com.datnht.image_compress.options
import com.datnht.core.CACHE_EXTERNAL_STORAGE
import com.datnht.core.DirectoryCompressType

data class CompressOptions constructor(
    val targetExtensionPath: String? = null,
    val quality: Int = 100,
    val compressHeight: Int? = null,
    val compressWidth: Int? = null,
    @DirectoryCompressType val directoryType: Int = CACHE_EXTERNAL_STORAGE
)