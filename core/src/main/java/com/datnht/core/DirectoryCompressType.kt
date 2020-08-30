package com.datnht.core

import androidx.annotation.IntDef

const val INTERNAL_STORAGE = 1
const val EXTERNAL_STORAGE = 2
const val CACHE_INTERNAL_STORAGE = 3
const val CACHE_EXTERNAL_STORAGE = 4
const val SHARE_STORAGE = 5

@IntDef(
    INTERNAL_STORAGE,
    CACHE_INTERNAL_STORAGE,
    EXTERNAL_STORAGE,
    CACHE_EXTERNAL_STORAGE
)
@Retention(AnnotationRetention.SOURCE)
annotation class DirectoryCompressType

@IntDef(
    INTERNAL_STORAGE,
    EXTERNAL_STORAGE,
    SHARE_STORAGE
)
@Retention(AnnotationRetention.SOURCE)
annotation class DirectorySaveType