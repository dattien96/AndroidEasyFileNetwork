package com.datnht.image_compress.core

import android.net.Uri

sealed class ImageSource {
    data class UriSource(val uris: List<Uri>): ImageSource()
    data class PathSource(val paths: List<String>): ImageSource()
}