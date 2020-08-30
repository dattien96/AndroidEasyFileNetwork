# Compress Image
## Using Thread
-CompressOptions: Choose the path directoty (inter - exter - cache inter - cache exter)
-Source: canbe uris or paths

      CompressManager.getCompressManager().foregroundCompress(
                    this,
                    FileSource.UriSource(it),
                    iImageCompressTaskListener = iImageCompressTaskListener,
                    options = CompressOptions(
                        quality = 50,
                        directoryType = INTERNAL_STORAGE
                    )
                )

Listener
      
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
## Using work

      CompressManager.getCompressManager().backgroundCompressWork(
                    this,
                    workManager,
                    FileSource.UriSource(it),
                    options = CompressOptions(
                        quality = 10,
                        directoryType = INTERNAL_STORAGE
                    )
                )
 Ob ouput 
      
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
        
  OnDestroy
  
      override fun onDestroy() {
        super.onDestroy()
        CompressManager.getCompressManager().onDestroyTask(workManager)
    }
    
  Note: Work doesn't accept non-primitive type of data. So we must use json to conver object/list <--> json and pass it to work class
# Save File

-SaveFileOptions: Choose the path directoty (inter - exter or share storage)
-Source: canbe uris or paths

    if (!uris.isNullOrEmpty()) {
                SaveFileManager.getSaveFileManager().saveFileWork(
                    this,
                    workManager,
                    FileSource.UriSource(uris),
                    SaveFileOptions(
                        directoryType = SHARE_STORAGE
                    )
                )
            }
            
 Ob output
    
    SaveFileManager.getSaveFileManager().getSaveWork(workManager).observe(this, Observer {
            if (!it.isNullOrEmpty() && it.first().state == WorkInfo.State.SUCCEEDED) {
                Toast.makeText(this, "save succeed", Toast.LENGTH_SHORT).show()
            }
        })
        
 onDestroy
 
      override fun onDestroy() {
        super.onDestroy()
        SaveFileManager.getSaveFileManager().onDestroyTask(workManager)
    }
