package com.dwagner.filepicker.ui.selected

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.SelectionHandler

class SelectedItemAdapter(
    private val inflater: LayoutInflater,
    private val selectionHandler: SelectionHandler
) : ListAdapter<AndroidFile, SelectedItemViewHolder>(DiffCallbackSelectedFiles) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedItemViewHolder = SelectedItemViewHolder(
        SelectedItemBinding.inflate(inflater, parent, false), selectionHandler)

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// used to compare items of the adapter, so the recyclerview knows if it has to rearrange or not
object DiffCallbackSelectedFiles : DiffUtil.ItemCallback<AndroidFile>() {
    override fun areItemsTheSame(
        oldItem: AndroidFile,
        newItem: AndroidFile
    ) =
        oldItem.fileURI == newItem.fileURI

    override fun areContentsTheSame(
        oldItem: AndroidFile,
        newItem: AndroidFile
    ) =
        oldItem.fileURI == newItem.fileURI
}