package com.dwagner.filepicker.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile


class FileItemViewHolder(
    private val binding: FileItemBinding,
    private val selectionHandler: SelectionHandler
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Pair<AndroidFile, Boolean>) {
        val (file,isSelected) = item

        binding.apply {
            root.setOnClickListener {
                // isSelected is the current value, have to invert it
                selectionHandler.onSelection(file, !isSelected)
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