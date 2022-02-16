package com.dwagner.filepicker.io

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.dwagner.filepicker.AppConstants
import com.dwagner.filepicker.FilterMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FileRepository {
    private val _resultURIs = MutableStateFlow(listOf<AndroidFile>())
    val resultURIs = _resultURIs.asStateFlow()

    /**
     * This method creates [AndroidFile][AndroidFile] objects for all media images and videos
     * found on the android device. getFiles() is a non-blocking suspend function and it is run
     * on a background thread of [Dispatchers.IO][kotlinx.coroutines.Dispatchers.IO].
     **/
    suspend fun getFiles(context: Context, filterMode: FilterMode) {
        withContext(Dispatchers.IO) {
            val resultFiles = when (filterMode) {
                FilterMode.ALL -> getImageFiles(context) + getVideoFiles(context)
                FilterMode.PHOTO -> getImageFiles(context)
                FilterMode.VIDEO -> getVideoFiles(context)
            }

            _resultURIs.emit(resultFiles)
        }
    }

    private fun getImageFiles(context: Context): List<AndroidFile> {
        val mediaImageFiles = mutableListOf<AndroidFile>()
        val columns = arrayOf(MediaStore.Images.Media._ID)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            "$orderBy DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                val imageContentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaImageFiles.add(
                    AndroidFile.Image(
                        fileURI = imageContentUri,
                        imageID = id,
                        context = context
                    )
                )
            }

        }
        Log.d(AppConstants.LOGGING_TAG, "query for images executed")
        return mediaImageFiles
    }

    private fun getVideoFiles(context: Context): List<AndroidFile> {
        val mediaVideoFiles = mutableListOf<AndroidFile>()
        val columns = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.VideoColumns.DURATION)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            "$orderBy DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                val videoContentURI = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val timeInMs =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))

                mediaVideoFiles.add(
                    AndroidFile.Video(
                        fileURI = videoContentURI,
                        videoID = id,
                        durationMillis = timeInMs,
                        context = context
                    )
                )
            }

        }
        Log.d(AppConstants.LOGGING_TAG, "query for videos executed")
        return mediaVideoFiles
    }
}