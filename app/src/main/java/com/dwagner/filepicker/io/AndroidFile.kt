package com.dwagner.filepicker.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.dwagner.filepicker.AppConstants


sealed class AndroidFile {
    abstract val fileURI: Uri

    data class Image(override val fileURI: Uri, val imageID : Long, val context: Context) : AndroidFile() {
        val thumbnail : Bitmap?
            get() =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(fileURI, Size(640, 480), null)
                }
                else {
                    // explicitly using deprecated way of generating thumbnails to support API
                    // levels lower than 29.
                    Log.d(AppConstants.LOGGING_TAG, "Path to img: $fileURI.p")
                    val bitmapOptions = BitmapFactory.Options()
                    bitmapOptions.inSampleSize = 1
                    MediaStore.Images.Thumbnails.getThumbnail(
                        context.contentResolver,
                        imageID,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        bitmapOptions
                    )
//
//                    ThumbnailUtils.createImageThumbnail(fileURI.toString())
                }
            }

    data class Video(override val fileURI: Uri) : AndroidFile()
}