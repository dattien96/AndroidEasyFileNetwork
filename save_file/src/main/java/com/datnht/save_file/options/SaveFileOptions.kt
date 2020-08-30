package com.datnht.save_file.options

import android.os.Environment
import com.datnht.core.DirectorySaveType
import com.datnht.core.SHARE_STORAGE

data class SaveFileOptions constructor(
    val targetExtensionPath: String? = null,
    val mimeType: String = "image/jpg",
    val environmentDirectory: String = Environment.DIRECTORY_PICTURES,
    @DirectorySaveType val directoryType: Int = SHARE_STORAGE
)