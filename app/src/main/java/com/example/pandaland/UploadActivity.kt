package com.example.pandaland

import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pedro.library.AutoPermissions

import kotlinx.android.synthetic.main.activity_upload.*
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.io.File
import java.util.*


class UploadActivity : AppCompatActivity() {
    var uri: Uri? = null

    var receiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            if(intent != null) {
                val response = intent.getStringExtra("response")

                //질문 -> url?
                val url = "http://192.168.40.82:8001/images/captured1598940956605.jpg"
                Glide.with(applicationContext)
                    .load(url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(imageOutput)
            }
        }
    }

    val filter = IntentFilter("org.techtown.photo.UPLOAD_SUCCESS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        fileButton.setOnClickListener {
            openGallery()
        }

        photoButton.setOnClickListener {
            takePicture()
        }

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        AutoPermissions.Companion.loadAllPermissions(this, 101)
    }

    private fun openGallery() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, 102)
    }

    fun takePicture(){
        val capturedFile = File(externalCacheDir, "captured.jpg")
        if(capturedFile.exists()) {
            capturedFile.delete()
        }

        capturedFile.createNewFile()
        uri = if(Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(this, "org.techtown.photo.fileprovider", capturedFile)
        } else {
            Uri.fromFile(capturedFile)
        }

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            101 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri!!))
                    imageOutput.setImageBitmap(bitmap)

                    requestUpload(uri!!)
                }
            }
            102 -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImage(data)
                    }
                }
            }
        }
    }

    @TargetApi(19)
    fun handleImage(data: Intent?) {
        var imagePath: String? = null
        val uri = data?.data

        if (uri != null) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                if (uri.authority == "com.android.providers.media.documents") {
                    val id = docId.split(":")[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection
                    )
                } else if (uri.authority == "com.android.providers.downloads.documents") {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(
                            "content://downloads/public_downloads"
                        ), docId.toLong()
                    )
                    imagePath = getImagePath(contentUri, null)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }

            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageOutput.setImageBitmap(bitmap)
        }
    }

    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }

        return path
    }

    /**
     * 업로드 요청
     */
    fun requestUpload(uri: Uri) {
        val serverUrl = "http://192.168.40.82:8001/profile/upload"
        val localPath: String = uri.toString()
        val fieldName = "photo"
        try {
            val uploadId: String = UUID.randomUUID().toString()
            MultipartUploadRequest(this, serverUrl)
                .setMethod("POST")
                .setUploadID(uploadId)
                .addFileToUpload(localPath, fieldName)
                .addParameter("name", "my picture")
                .setMaxRetries(2)
                .startUpload()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()

        registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(receiver)
    }







}