package com.dwagner.filepicker.ui.files.selected

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.dwagner.filepicker.databinding.SelectedItemBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.files.FilePickerViewModel
import com.dwagner.filepicker.ui.files.SelectionObserver

class SelectedItemAdapter(
    private val inflater: LayoutInflater,
    private val viewModel: FilePickerViewModel
) : ListAdapter<AndroidFile, SelectedItemViewHolder>(DiffCallbackSelectedFiles), SelectionObserver {

    // adapter has own internal state, but it reflects the changes to the view model
    private val selectedItems : MutableList<AndroidFile>

    init {
        viewModel.observeSelectedFiles(this)
        selectedItems = viewModel.getSelectedFiles().toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedItemViewHolder = SelectedItemViewHolder(
        SelectedItemBinding.inflate(inflater, parent, false), viewModel)

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        holder.bind(selectedItems[position])
    }

    override fun getItemCount(): Int {
        return selectedItems.size
    }

    override fun onSelection(file: AndroidFile, isSelected: Boolean) {
        if (!isSelected) {
            // deselection
            val index = this.selectedItems.indexOf(file)
            this.selectedItems.removeAt(index)
            this.notifyItemRemoved(index)
        }

        else {
            // new item selected
            this.selectedItems.add(file)
            this.notifyItemInserted(selectedItems.size)
        }
    }

    override fun onDeselectAll(selected: List<AndroidFile>) {
        // just clear internal list, ignore the passed list
        val oldSize = selectedItems.size
        selectedItems.clear()
        this.notifyItemRangeRemoved(0, oldSize)
    }

    fun onDestroy() {
        viewModel.stopObservingSelectedFiles(this)
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