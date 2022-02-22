package com.dwagner.filepicker.ui.files

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class FileItemViewHolder(
    private val binding: FileItemBinding,
    private val coroutineScope: CoroutineScope,
    private val selectedCallback: SelectedCallback
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile, isSelected: Boolean) {
        binding.apply {
            root.setOnClickListener {
                // have to invert current selected status
                selectedCallback(file, !isSelected)
            }

            check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

            // async loading of thumbnail in coroutine
            coroutineScope.launch {
                thumbnail.setImageBitmap(file.loadThumbnail())
            }

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