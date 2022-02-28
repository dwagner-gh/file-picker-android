package com.dwagner.filepicker.ui.files.selected

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.clickWithDebounce
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SelectedItemViewHolder(
    private val binding: SelectedItemBinding,
    private val coroutineScope: CoroutineScope,
    private val deselectCallback: DeselectCallback
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            root.setOnClickListener {
                // disable view to prevent double clicking, element gets removed after click
                it.isEnabled = false
                deselectCallback(file)
            }

            root.isEnabled = true

            coroutineScope.launch {
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