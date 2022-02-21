package com.dwagner.filepicker.ui.files

import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile
import kotlinx.coroutines.launch


class FileItemViewHolder(
    private val binding: FileItemBinding,
    private val viewModel : FilePickerViewModel,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            val isSelected = viewModel.isFileSelected(file)
            root.setOnClickListener {
                // have to invert current selected status
                viewModel.setFileSelected(file, !isSelected)
            }

            check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

            // async loading of thumbnail in coroutine
            viewModel.viewModelScope.launch {
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