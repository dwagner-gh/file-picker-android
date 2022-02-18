package com.dwagner.filepicker.ui.files.selected

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.files.SelectionHandler
import kotlinx.coroutines.launch

class SelectedItemViewHolder(
    private val binding: SelectedItemBinding,
    private val viewModel: ViewModel,
    private val selectionHandler: SelectionHandler,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            root.setOnClickListener {
                selectionHandler.onSelection(file, false)
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