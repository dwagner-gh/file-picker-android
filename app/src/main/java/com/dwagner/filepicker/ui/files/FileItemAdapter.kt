package com.dwagner.filepicker.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.FileItemBinding
import com.dwagner.filepicker.io.AndroidFile
import kotlinx.coroutines.CoroutineScope

typealias SelectedCallback = (file: AndroidFile, isSelected: Boolean) -> Unit

class FileItemAdapter(
    private val inflater: LayoutInflater,
    private val coroutineScope: CoroutineScope,
    private val selectedCallback: SelectedCallback
) : ListAdapter<AndroidFile, FileItemViewHolder>(DiffCallbackAllFiles), ViewStateChangeObserver {

    private var selectedFilesMap : MutableMap<AndroidFile, Boolean> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder =
        FileItemViewHolder(FileItemBinding.inflate(inflater, parent, false), coroutineScope, selectedCallback)

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedFilesMap[item] ?: false)
    }

    override fun onStateChange(stateChange: ViewStateChange) {
        when(stateChange) {
            is ViewStateChange.SelectFile -> onSelection(stateChange.file, stateChange.isSelected)
            is ViewStateChange.SelectFiles -> onSelectFiles(stateChange.files)
            is ViewStateChange.DataLoaded -> this.submitList(stateChange.files)
        }
    }

    private fun onSelectFiles(files: Map<AndroidFile, Boolean>) {
        // for each changed file, trigger a change notification
        files.forEach {
            val (file, isSelected) = it
            val index = this.currentList.indexOf(file)

            if (isSelected) {
                selectedFilesMap[file] = true
            }
            else {
                selectedFilesMap.remove(file)
            }

            if (index != -1) {
                // because files can be filtered, not all files in the map
                // might be in the current list of the adapter.
                this.notifyItemChanged(index)
            }
        }
    }

    private fun onSelection(file: AndroidFile, isSelected: Boolean) {
        if (isSelected) {
            selectedFilesMap[file] = true
        }

        else {
            selectedFilesMap.remove(file)
        }

        this.notifyItemChanged(this.currentList.indexOf(file))
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