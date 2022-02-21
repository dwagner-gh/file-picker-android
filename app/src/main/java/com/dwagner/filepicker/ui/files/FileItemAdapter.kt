package com.dwagner.filepicker.ui.files

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile

class FileItemAdapter(
    private val inflater: LayoutInflater,
    private val viewModel: FilePickerViewModel
) : ListAdapter<AndroidFile, FileItemViewHolder>(DiffCallbackAllFiles), SelectionObserver, DataLoadedObserver {

    init {
        viewModel.observeSelectedFiles(this)
        viewModel.observeLoadedData(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder =
        FileItemViewHolder(FileItemBinding.inflate(inflater, parent, false), viewModel)

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onDataLoaded() {
        // add new loaded files and trigger data changed notification
        this.submitList(viewModel.lastLoadedFiles())
    }

    override fun onDeselectAll(selected: List<AndroidFile>) {
        selected.forEach {
            val index = this.currentList.indexOf(it)
            if (index != -1) {
                this.notifyItemChanged(index)
            }
        }
    }

    override fun onSelection(file: AndroidFile, isSelected: Boolean) {
        this.notifyItemChanged(this.currentList.indexOf(file))
    }

    fun onDestroy() {
        viewModel.stopObservingSelectedFiles(this)
        viewModel.stopObservingLoadedData(this)
    }
}

object DiffCallbackAllFiles : DiffUtil.ItemCallback<AndroidFile>() {
    override fun areItemsTheSame(
        oldItem: AndroidFile,
        newItem: AndroidFile
    ) =
        oldItem == newItem

    override fun areContentsTheSame(
        oldItem: AndroidFile,
        newItem: AndroidFile
    ) =
        oldItem == newItem
}