package com.datnht.save_file.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.datnht.core.CACHE_EXTERNAL_STORAGE
import com.datnht.core.CACHE_INTERNAL_STORAGE
import com.datnht.core.INTERNAL_STORAGE
import com.datnht.core.SHARE_STORAGE
import com.datnht.save_file.options.SaveFileOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

fun saveFile(
    context: Context,
    resolver: ContentResolver?,
    bitmap: Bitmap,
    saveFileOptions: SaveFileOptions? = null
): Uri? {
    val isShareStorage = saveFileOptions == null || saveFileOptions.directoryType == SHARE_STORAGE
    return if (isShareStorage) saveImageToMediaStoreForShareable(
        resolver,
        bitmap,
        saveFileOptions
    ) else saveImageToNotShareStorage(context, bitmap, saveFileOptions!!)
}

fun saveImageToNotShareStorage(
    context: Context,
    bitmap: Bitmap,
    saveFileOptions: SaveFileOptions
): Uri? {

    val root = getSaveDirectory(
        context,
        saveFileOptions
    )
    val targetPathExtension = saveFileOptions.targetExtensionPath ?: UUID.randomUUID().toString() + ".jpg"

    //create placeholder for the compressed image file
    val compressedFile = File(root, targetPathExtension)

    //convert the decoded bitmap to stream
    val byteArrayOutputStream = ByteArrayOutputStream()

    bitmap.compress(
        Bitmap.CompressFormat.JPEG,
        100,
        byteArrayOutputStream
    )

    // Right now, we have our bitmap inside byteArrayOutputStream Object,
    // all we need next is to write it to the compressed file we created earlier,
    // java.io.FileOutputStream can help us do just That!
    val fileOutputStream = FileOutputStream(compressedFile)
    fileOutputStream.write(byteArrayOutputStream.toByteArray())
    fileOutputStream.flush()
    fileOutputStream.close()

    //File written, return to the caller. Done!
    return Uri.fromFile(compressedFile)
}

fun getSaveDirectory(context: Context, saveFileOptions: SaveFileOptions): File {
    var targetDir: File? = when (saveFileOptions.directoryType) {
        INTERNAL_STORAGE -> {
            context.filesDir
        }

        else -> {
            context.getExternalFilesDir(saveFileOptions.environmentDirectory)
        }
    }
    val rootDir: String = targetDir?.absolutePath?.toString() + "/MyAppImageSaved"
    val root = File(rootDir)

    //Create ImageCompressor folder if it doesn't already exists.
    if (!root.exists()) root.mkdirs()
    return root
}

fun saveImageToMediaStoreForShareable(
    resolver: ContentResolver?,
    bitmap: Bitmap,
    saveFileOptions: SaveFileOptions? = null
): Uri? {
    if (resolver == null) return null
    var newFile: File?
    var fos: OutputStream? = null
    var finalLocationUri: Uri? = null
    var environmentDir = saveFileOptions?.environmentDirectory ?: Environment.DIRECTORY_PICTURES
    var mimeType = saveFileOptions?.mimeType ?: "image/jpg"
    val targetPathExtension =
        if (saveFileOptions?.targetExtensionPath == null)
            UUID.randomUUID().toString() + ".jpg" else saveFileOptions.targetExtensionPath
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                targetPathExtension
            )
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "$environmentDir/MyAppImageSaved"
            )
            val imageUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            fos = resolver.openOutputStream(imageUri ?: return null)
            finalLocationUri = imageUri
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(environmentDir)

            newFile = File(imagesDir, "/MyAppImageSaved")

            if (!newFile.exists()) {
                newFile.mkdir()
            }

            val image = File(newFile, targetPathExtension)
            finalLocationUri = Uri.fromFile(image)
            fos = FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (ex: Exception) {
        ex.printStackTrace()
    } finally {
        fos?.flush()
        fos?.close()
        return finalLocationUri
    }
}

fun decodeImageFromFiles(path: String?, width: Int? = null, height: Int? = null): Bitmap {
    val scaleOptions = BitmapFactory.Options()
    scaleOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, scaleOptions)
    var scale = 1
    if (width != null && height != null) {
        while (scaleOptions.outWidth / scale / 2 >= width
            && scaleOptions.outHeight / scale / 2 >= height
        ) {
            scale *= 2
        }
    }
    // decode with the sample size
    val outOptions = BitmapFactory.Options()
    outOptions.inSampleSize = scale
    return BitmapFactory.decodeFile(path, outOptions)
}