package com.dwagner.filepicker.ui

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.AppConstants
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemViewHolder(
    private val binding: FileItemBinding,
    val onRowClick: (AndroidFile) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {

        when (file) {
            is AndroidFile.Image -> {
                binding.apply {
                    root.setOnClickListener { onRowClick(file) }
                    Log.d(AppConstants.LOGGING_TAG, "is thumbnail null? ${file.thumbnail}")
                    thumbnail.setImageBitmap(file.thumbnail)
                }
            }
            is AndroidFile.Video -> {

            }
        }

    }
}