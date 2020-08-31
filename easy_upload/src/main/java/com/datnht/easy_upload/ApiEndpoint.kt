package com.datnht.easy_upload

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface ApiEndpoint {

    @Multipart
    @POST("file")
    fun attachFile(
        @Part("name") filename: RequestBody,
        @Part("type") mimeType: RequestBody,
        @Part("size") fileSize: RequestBody,
        @Part filePart: MultipartBody.Part
    ): Single<DataResponse>
}

/**
 * TODO : remove commend code
 */
//fun createUploadRequestBodyDefault(file: File, mimeType: String) =
//    file.asRequestBody(mimeType.toMediaType())
//
//private fun createUploadRequestBody(
//    file: File,
//    mimeType: String,
//    progressEmitter: PublishSubject<Double>
//): RequestBody {
//    val fileRequestBody = file.asRequestBody(mimeType.toMediaType())
//    return CountingRequestBody(fileRequestBody) { bytesWritten, contentLength ->
//        val progress = 1.0 * bytesWritten / contentLength
//        progressEmitter.onNext(progress)
//
//        if (progress >= 1.0) {
//            progressEmitter.onComplete()
//        }
//    }
//}
//
//
//private fun createUploadRequest(
//    filename: String,
//    file: File,
//    mimeType: String,
//    progressEmitter: PublishSubject<Double>
//): Single<DataResponse> {
//    val requestBody = createUploadRequestBody(file, mimeType, progressEmitter)
//    return remoteApi.attachFile(
//        filename = filename.toPlainTextBody(),
//        mimeType = mimeType.toPlainTextBody(),
//        fileSize = file.length().toString().toPlainTextBody(),
//        filePart = MultipartBody.Part.createFormData(
//            name = "files[]",
//            filename = filename,
//            body = requestBody
//        )
//    )
//}
//
//private fun String.toPlainTextBody() = toRequestBody("text/plain".toMediaType())
//
//fun uploadAttachment(
//    filename: String, file: File, mimeType: String
//): Observable<AttachmentUploadRemoteResult> {
//    val progressEmitter = PublishSubject.create<Double>()
//    val uploadRequest = createUploadRequest(
//        filename, file, mimeType, progressEmitter
//    )
//
//    val uploadResult = uploadRequest
//        .map<AttachmentUploadRemoteResult> {
//            CountingRequestResult.Completed(it)
//        }
//        .toObservable()
//
//    val progressResult = progressEmitter
//        .map<AttachmentUploadRemoteResult> {
//            CountingRequestResult.Progress(it)
//        }
//
//    return progressResult.mergeWith(uploadResult)
//}
//
//typealias AttachmentUploadRemoteResult =
//        CountingRequestResult<DataResponse>
//
class DataResponse