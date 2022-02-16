package com.dwagner.filepicker.ui

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.AppConstants
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemViewHolder(
    private val binding: FileItemBinding,
    val onRowClick: (AndroidFile) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {

        binding.apply {
            root.setOnClickListener { onRowClick(file) }
            thumbnail.setImageBitmap(file.thumbnail)

            if (file is AndroidFile.Video) {
                duration.visibility = View.VISIBLE
                duration.text = file.durationString
            }
        }



    }
}