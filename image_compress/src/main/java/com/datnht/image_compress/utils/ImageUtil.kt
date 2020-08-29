package com.datnht.image_compress.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.datnht.image_compress.core.CACHE_EXTERNAL_STORAGE
import com.datnht.image_compress.core.CACHE_INTERNAL_STORAGE
import com.datnht.image_compress.core.CompressOptions
import com.datnht.image_compress.core.INTERNAL_STORAGE
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


//SDF to generate a unique name for our compress file.
val SDF: SimpleDateFormat = SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault())

/**
compress the file/photo from @param <b>path</b> to a private location on the current device and return the compressed file.
@param path = The original image path
@param context = Current android Context
 */
@Throws(IOException::class)
fun getCompressed(
    context: Context?,
    path: String?,
    compressOptions: CompressOptions? = null
): File? {
    if (context == null) throw NullPointerException("Context must not be null.")

    val root = getCompressDirectory(context, compressOptions)
    val targetPathExtension =
        if (compressOptions?.targetExtensionPath == null)
            SDF.format(Date()).toString() + ".jpg" else compressOptions.targetExtensionPath

    //decode and resize the original bitmap from @param path.
    val bitmap =
        decodeImageFromFiles(
            path,  /* your desired width*/
            compressOptions?.compressWidth,  /*your desired height*/
            compressOptions?.compressHeight
        )

    //create placeholder for the compressed image file
    val compressed = File(root, targetPathExtension)

    //convert the decoded bitmap to stream
    val byteArrayOutputStream = ByteArrayOutputStream()

    // compress bitmap into byteArrayOutputStream
    // Bitmap.compress(Format, Quality, OutputStream)
    // Where Quality ranges from 1 - 100.
    bitmap.compress(
        Bitmap.CompressFormat.JPEG,
        compressOptions?.quality ?: 100,
        byteArrayOutputStream
    )

    // Right now, we have our bitmap inside byteArrayOutputStream Object,
    // all we need next is to write it to the compressed file we created earlier,
    // java.io.FileOutputStream can help us do just That!
    val fileOutputStream = FileOutputStream(compressed)
    fileOutputStream.write(byteArrayOutputStream.toByteArray())
    fileOutputStream.flush()
    fileOutputStream.close()

    //File written, return to the caller. Done!
    return compressed
}

fun getCompressDirectory(context: Context, compressOptions: CompressOptions?): File {
    if (compressOptions == null || compressOptions.directoryType == CACHE_EXTERNAL_STORAGE) {
        // getting device external cache directory, might not be available on some devices,
        // so our code fall back to internal storage cache directory, which is always available but in smaller quantity
        var cacheDir: File? = context.externalCacheDir
        if (cacheDir == null) //fall back
            cacheDir = context.cacheDir
        val rootDir: String = cacheDir?.absolutePath?.toString() + "/ImageCompressor"
        val root = File(rootDir)

        //Create ImageCompressor folder if it doesn't already exists.
        if (!root.exists()) root.mkdirs()
        return root
    } else {
        var targetDir: File? = when (compressOptions.directoryType) {
            CACHE_INTERNAL_STORAGE -> {
                context.cacheDir
            }

            INTERNAL_STORAGE -> {
                context.filesDir
            }

            else -> {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            }
        }
        val rootDir: String = targetDir?.absolutePath?.toString() + "/ImageCompressor"
        val root = File(rootDir)

        //Create ImageCompressor folder if it doesn't already exists.
        if (!root.exists()) root.mkdirs()
        return root
    }
}

fun decodeImageFromFiles(path: String?, width: Int?, height: Int?): Bitmap {
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

fun getBitmap(context: Context, imageUri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                context.contentResolver,
                imageUri
            )
        )
    } else {
        context
            .contentResolver
            .openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
    }
}

fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap): File {
    //create a file to write bitmap data
    destinationFile.createNewFile()
    //Convert bitmap to byte array
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
    val bitmapData = bos.toByteArray()
    //write the bytes in file
    val fos = FileOutputStream(destinationFile)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
    return destinationFile;
}

