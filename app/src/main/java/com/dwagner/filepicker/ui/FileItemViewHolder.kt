package com.dwagner.filepicker.ui

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemViewHolder(
    private val binding: FileItemBinding,
    val onRowClick: (AndroidFile) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            root.setOnClickListener {
                check.visibility = if (check.isVisible) View.INVISIBLE else View.VISIBLE
                onRowClick(file)
            }

            thumbnail.setImageBitmap(file.thumbnail)

            if (file is AndroidFile.Video) {
                duration.visibility = View.VISIBLE
                duration.text = file.durationString
            }
        }
    }
}