package com.datnht.core

import android.net.Uri

sealed class FileSource {
    data class UriSource(val uris: List<Uri>): FileSource()
    data class PathSource(val paths: List<String>): FileSource()
}