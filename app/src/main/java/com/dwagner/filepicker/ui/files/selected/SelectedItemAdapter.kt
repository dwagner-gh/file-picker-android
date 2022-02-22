package com.dwagner.filepicker.ui.files.selected

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.files.ViewStateChange
import com.dwagner.filepicker.ui.files.ViewStateChangeObserver
import kotlinx.coroutines.CoroutineScope

typealias DeselectCallback = (AndroidFile) -> Unit

class SelectedItemAdapter(
    private val inflater: LayoutInflater,
    private val coroutineScope: CoroutineScope,
    private val deselectCallback: DeselectCallback
) : ListAdapter<AndroidFile, SelectedItemViewHolder>(DiffCallbackSelectedFiles), ViewStateChangeObserver {

    // adapter has own internal state, but it reflects the changes to the view model
    private val selectedItems : MutableList<AndroidFile> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedItemViewHolder = SelectedItemViewHolder(
        SelectedItemBinding.inflate(inflater, parent, false), coroutineScope, deselectCallback)

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        holder.bind(selectedItems[position])
    }

    override fun getItemCount(): Int {
        return selectedItems.size
    }

    override fun onStateChange(stateChange: ViewStateChange) {
        when(stateChange) {
            is ViewStateChange.SelectFile -> onSelection(stateChange.file, stateChange.isSelected)
            is ViewStateChange.SelectFiles -> onSelectFiles(stateChange.files)
            else -> { /* No other changes need to be handled */ }
        }
    }

    private fun onSelection(file: AndroidFile, isSelected: Boolean) {
        if (!isSelected) {
            // deselection
            val index = this.selectedItems.indexOf(file)
            this.selectedItems.removeAt(index)
            this.notifyItemRemoved(index)
        }

        else {
            // new item selected
            this.selectedItems.add(file)
            this.notifyItemInserted(selectedItems.size - 1)
        }
    }

    private fun onSelectFiles(files: Map<AndroidFile, Boolean>) {
        files.forEach {
            val (file, isSelected) = it
            if (isSelected) {
                // add file
                selectedItems.add(file)
                notifyItemInserted(selectedItems.size - 1)
            }
            else {
                // remove file
                val index = selectedItems.indexOf(file)
                selectedItems.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }
}

// used to compare items of the adapter, so the recyclerview knows if it has to rearrange or not
object DiffCallbackSelectedFiles : DiffUtil.ItemCallback<AndroidFile>() {
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