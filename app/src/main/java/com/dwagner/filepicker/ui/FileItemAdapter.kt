package com.dwagner.filepicker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemAdapter(
    private val inflater: LayoutInflater,
    private val onRowClicked: (AndroidFile) -> Unit
) : ListAdapter<AndroidFile, FileItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder =
        FileItemViewHolder(FileItemBinding.inflate(inflater, parent, false), onRowClicked)

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// used to compare items of the adapter, so the recyclerview knows if it has to rearrange or not
private object DiffCallback : DiffUtil.ItemCallback<AndroidFile>() {
    override fun areItemsTheSame(oldItem: AndroidFile, newItem: AndroidFile) =
        oldItem.fileURI == newItem.fileURI

    override fun areContentsTheSame(oldItem: AndroidFile, newItem: AndroidFile) =
        oldItem.fileURI == newItem.fileURI
}