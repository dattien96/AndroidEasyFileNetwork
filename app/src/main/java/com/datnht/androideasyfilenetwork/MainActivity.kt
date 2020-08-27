package com.datnht.androideasyfilenetwork

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.datnht.easy_upload.listener.IImageCompressTaskListener
import com.datnht.easy_upload.task.ImageCompressTask
import com.datnht.easy_upload.utils.convertBitmapToFile
import com.datnht.easy_upload.utils.getBitmap
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private var selectedBitmap: Bitmap? = null

    private val askStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                pickMultipleContents("image/*")
                Log.e("TAG", " permnission granted")
            } else {
                Log.e("TAG", " permnission denied")
            }
        }

    private val pickMultipleContents =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uri ->
            uri?.let { it ->
                // để gọi cái này: pickImages("image/*") pass mimetype kiểu này là cho phép user chọn img
                // để gọi cái này: pickImages("video/*") pass mimetype kiểu này là cho phép user chọn video

                selectedBitmap = getBitmap(this, it[0])
                image_origin.setImageBitmap(selectedBitmap)

                val selectedImgFile = File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    Date().time.toString() + "_selectedImg.jpg")

                convertBitmapToFile(selectedImgFile, selectedBitmap ?: return@let)
                mExecutorService.execute(ImageCompressTask(this, listOf<String>(selectedImgFile.absolutePath), iImageCompressTaskListener))
            }
        }

    //image compress task callback
    private val iImageCompressTaskListener: IImageCompressTaskListener =
        object : IImageCompressTaskListener {

            override fun onComplete(compressed: List<File?>) {
                //photo compressed. Yay!

                //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)
                val file: File = compressed[0] ?: return
                Log.d(
                    "ImageCompressor",
                    "New photo size ==> " + file.length()
                ) //log new file size.
                image_compress.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()))
            }

            override fun onError(error: Throwable?) {
                //very unlikely, but it might happen on a device with extremely low storage.
                //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
                Log.wtf("ImageCompressor", "Error occurred", error)
            }
        }

    //create a single thread pool to our image compression class.
    private var mExecutorService: ExecutorService = Executors.newFixedThreadPool(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_compress_image?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    AlertDialog.Builder(this)
                        .setTitle("Resquest Per")
                        .setMessage("Cái này hiện lên chỉ khi user đã từ chối 1 lần trước đó. Nhằm cho phép app giải thích cho user hiểu là cần cấp quyền. Nếu nó từ chối tiếp mà tick k hỏi lại thì coi như bị chặn mãi")
                        .setPositiveButton(
                            "ok"
                        ) { _, i ->
                            //Prompt the user once explanation has been shown
                            askStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        .create()
                        .show()
                } else { // No explanation needed, we can request the permission.
                    askStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                pickMultipleContents("image/*")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        //clean up!
        mExecutorService.shutdown()
    }
}