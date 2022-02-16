package com.dwagner.filepicker.io

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.dwagner.filepicker.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FileRepository {
    private val _resultURIs = MutableStateFlow(listOf<AndroidFile>())
    val resultURIs = _resultURIs.asStateFlow()

    suspend fun getFiles(context: Context) {
        withContext(Dispatchers.IO) {
            val galleryImageUrls = mutableListOf<AndroidFile>()

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
                    Log.d(AppConstants.LOGGING_TAG, "path of image: ${cursor.getString(idColumn)}")

                    val imageUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    galleryImageUrls.add(AndroidFile.Image(imageUri, id, context))
                    Log.d("file_picker", "URI of image: $imageUri")
                }

            }
            Log.d(AppConstants.LOGGING_TAG, "query for images executed")
            _resultURIs.emit(galleryImageUrls)
        }
    }

}