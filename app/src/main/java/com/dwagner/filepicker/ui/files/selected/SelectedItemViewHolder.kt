package com.dwagner.filepicker.ui.files.selected

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.files.FilePickerViewModel
import com.dwagner.filepicker.ui.files.SelectionObserver
import kotlinx.coroutines.launch

class SelectedItemViewHolder(
    private val binding: SelectedItemBinding,
    private val viewModel: FilePickerViewModel
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            root.setOnClickListener {
                // when a file is clicked, we always when to deselect, because all items in this
                // view are marked as selected
                viewModel.setFileSelected(file, false)
            }

            viewModel.viewModelScope.launch {
                thumbnailSelected.setImageBitmap(file.loadThumbnail())
            }

            if (file is AndroidFile.Video) {
                durationSelected.visibility = View.VISIBLE
                durationSelected.text = file.durationString
            }

            else {
                durationSelected.visibility = View.GONE
            }
        }
    }
}