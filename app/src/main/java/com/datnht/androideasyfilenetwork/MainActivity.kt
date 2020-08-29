package com.datnht.androideasyfilenetwork

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.datnht.core.JsonManager
import com.datnht.image_compress.CompressManager
import com.datnht.image_compress.core.CompressOptions
import com.datnht.image_compress.core.INTERNAL_STORAGE
import com.datnht.image_compress.core.ImageSource
import com.datnht.image_compress.listener.IImageCompressTaskListener
import com.datnht.image_compress.task.BackgroundImageCompressTask
import com.datnht.image_compress.utils.getBitmap
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private var selectedBitmap: Bitmap? = null
    private val workManager = WorkManager.getInstance(this)
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

//                CompressManager.getCompressManager().foregroundCompress(
//                    this,
//                    ImageSource.UriSource(it),
//                    iImageCompressTaskListener = iImageCompressTaskListener,
//                    options = CompressOptions(
//                        quality = 50,
//                        directoryType = INTERNAL_STORAGE
//                    )
//                )

                CompressManager.getCompressManager().backgroundCompressWork(
                    this,
                    workManager,
                    ImageSource.UriSource(it),
                    options = CompressOptions(
                        quality = 10,
                        directoryType = INTERNAL_STORAGE
                    )
                )
            }
        }

    //image compress task callback
    private val iImageCompressTaskListener: IImageCompressTaskListener =
        object : IImageCompressTaskListener {

            override fun onComplete(compressedPaths: List<String>) {
                //photo compressed. Yay!

                //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)
                val filePath: String = compressedPaths[0] ?: return
                Log.d(
                    "ImageCompressor",
                    "New photo size ==> " + File(filePath).length()
                ) //log new file size.
                image_compress.setImageBitmap(BitmapFactory.decodeFile(filePath))
            }

            override fun onError(error: Throwable?) {
                //very unlikely, but it might happen on a device with extremely low storage.
                //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
                Log.wtf("ImageCompressor", "Error occurred", error)
            }
        }

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

        CompressManager.getCompressManager().getCompressWork(workManager)?.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val process = it.first().progress
                val value = process.getInt(BackgroundImageCompressTask.WORK_COMPRESS_PROCESS, 0)
                if (it.first().state == WorkInfo.State.SUCCEEDED) {
                    val filePathJson =
                        it[0].outputData.getString(BackgroundImageCompressTask.WORK_COMPRESS_PATH)
                    val listPaths = JsonManager.INSTANCE.jsonToList<String>(filePathJson ?: return@Observer)
                    image_compress.setImageBitmap(BitmapFactory.decodeFile(listPaths[0]))
                }
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        CompressManager.getCompressManager().onDestroyTask()
    }
}