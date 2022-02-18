package com.dwagner.filepicker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemAdapter(
    private val inflater: LayoutInflater,
    private val selectionHandler: SelectionHandler
) : ListAdapter<Pair<AndroidFile, Boolean>, FileItemViewHolder>(DiffCallbackAllFiles) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder =
        FileItemViewHolder(FileItemBinding.inflate(inflater, parent, false), selectionHandler)

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object DiffCallbackAllFiles : DiffUtil.ItemCallback<Pair<AndroidFile, Boolean>>() {
    override fun areItemsTheSame(
        oldItem: Pair<AndroidFile, Boolean>,
        newItem: Pair<AndroidFile, Boolean>
    ) =
        oldItem.first == newItem.first

    override fun areContentsTheSame(
        oldItem: Pair<AndroidFile, Boolean>,
        newItem: Pair<AndroidFile, Boolean>
    ) =
        oldItem.first == newItem.first && oldItem.second == newItem.second
}