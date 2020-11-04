package org.techtown.photo

import android.content.Context
import android.content.Intent
import android.util.Log
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class GlobalUploadObserver : RequestObserverDelegate {
    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
        Log.e("RECEIVER", "Progress: $uploadInfo")
    }

    override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
        val response = String(serverResponse.body)
        Log.e("RECEIVER", "Success: $response")

        val intent = Intent("org.techtown.photo.UPLOAD_SUCCESS")
        intent.putExtra("response", response)
        context.sendBroadcast(intent)

    }

    override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
        when (exception) {
            is UserCancelledUploadException -> {
                Log.e("RECEIVER", "Error, user cancelled upload: $uploadInfo")
            }

            is UploadError -> {
                Log.e("RECEIVER", "Error, upload error: ${exception.serverResponse}")
            }

            else -> {
                Log.e("RECEIVER", "Error: $uploadInfo", exception)
            }
        }
    }

    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
        Log.e("RECEIVER", "Completed: $uploadInfo")
    }

    override fun onCompletedWhileNotObserving() {
        Log.e("RECEIVER", "Completed while not observing")
    }
}