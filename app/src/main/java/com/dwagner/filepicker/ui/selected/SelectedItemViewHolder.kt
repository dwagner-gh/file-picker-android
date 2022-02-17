package com.dwagner.filepicker.ui.selected

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.OverviewClickHandler

class SelectedItemViewHolder(
    private val binding: SelectedItemBinding,
    val onRowClick: OverviewClickHandler
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(file: AndroidFile) {
        binding.apply {
            root.setOnClickListener {
                onRowClick(file)
            }
            thumbnailSelected.setImageBitmap(file.thumbnail)

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