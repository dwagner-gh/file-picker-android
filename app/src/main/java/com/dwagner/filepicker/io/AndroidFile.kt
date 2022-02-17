package com.dwagner.filepicker.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import com.dwagner.filepicker.R
import java.util.concurrent.TimeUnit

/**
 * A basic file wrapper for android Uris. Files can be compared via
 * [==, eauals(other: Any?)][AndroidFile.equals], files are equal, if their URI is equal.
 */
sealed class AndroidFile {
    abstract val fileURI: Uri
    abstract val thumbnail: Bitmap

    override fun equals(other: Any?): Boolean {
        if (other is AndroidFile) {
            return fileURI == other.fileURI
        }
        return false
    }

    override fun hashCode(): Int {
        return fileURI.hashCode()
    }

    /**
     * A simple wrapper for an image media file
     * @param fileURI content Uri of image media file
     * @param imageID this id is needed for legacy support (API level < 29)
     * @param context application context, needed to get the contentResolver
     **/
    data class Image(
        override val fileURI: Uri,
        val imageID: Long,
        val width: Int,
        val height: Int,
        private val context: Context
    ) :
        AndroidFile() {

        private val thumbnailSize = Size(480, 480)

        override val thumbnail: Bitmap
            get() =
                cropToSquare(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(fileURI, thumbnailSize, null)
                    } else {
                        // explicitly using deprecated way of generating thumbnails to support API
                        // levels lower than 29.
                        val bitmapOptions = BitmapFactory.Options()
                        bitmapOptions.inSampleSize = 1
                        MediaStore.Images.Thumbnails.getThumbnail(
                            context.contentResolver,
                            imageID,
                            MediaStore.Images.Thumbnails.MINI_KIND,
                            bitmapOptions
                        )
                    }
                )
    }

    /**
     * A simple wrapper for a video media file
     * @param videoID this id is needed for legacy support (API level < 29)
     * @param durationMillis duration of the video in milliseconds
     **/
    data class Video(
        override val fileURI: Uri,
        val videoID: Long,
        val durationMillis: Long,
        val height: Int,
        val width: Int,
        private val context: Context
    ) : AndroidFile() {

        private val thumbnailSize = Size(480, 480)

        val durationString: String
            get() {
                var millis = durationMillis
                val hours = TimeUnit.MILLISECONDS.toHours(millis)
                millis -= TimeUnit.HOURS.toMillis(hours)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
                millis -= TimeUnit.MINUTES.toMillis(minutes)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

                return String.format(
                    context.getString(R.string.video_duration),
                    hours,
                    minutes,
                    seconds
                )
            }

        override val thumbnail: Bitmap
            get() =
                cropToSquare(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(fileURI, thumbnailSize, null)
                    } else {
                        // explicitly using deprecated way of generating thumbnails to support API
                        // levels lower than 29.
                        val bitmapOptions = BitmapFactory.Options()
                        bitmapOptions.inSampleSize = 1
                        bitmapOptions.outHeight = thumbnailSize.height
                        bitmapOptions.outWidth = thumbnailSize.width
                        MediaStore.Video.Thumbnails.getThumbnail(
                            context.contentResolver,
                            videoID,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            bitmapOptions
                        )
                    }
                )
    }
}


// https://stackoverflow.com/a/28783691/2852865
private fun cropToSquare(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val newWidth = if (height > width) width else height
    val newHeight = if (height > width) height - (height - width) else height
    var cropW = (width - height) / 2
    cropW = if (cropW < 0) 0 else cropW
    var cropH = (height - width) / 2
    cropH = if (cropH < 0) 0 else cropH
    return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
}