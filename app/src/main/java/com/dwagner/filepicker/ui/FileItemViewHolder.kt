package com.dwagner.filepicker.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile



class FileItemViewHolder(
    private val binding: FileItemBinding,
    val onRowClick: RowClickHandler
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Pair<AndroidFile, Boolean>) {
        val (file,isSelected) = item

        binding.apply {
            root.setOnClickListener {
                onRowClick(file, isSelected)
            }

            check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
            thumbnail.setImageBitmap(file.thumbnail)

            if (file is AndroidFile.Video) {
                duration.visibility = View.VISIBLE
                duration.text = file.durationString
            }

            else {
                duration.visibility = View.GONE
            }
        }
    }
}