package com.dwagner.filepicker.ui.files

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.io.FileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilePickerViewModel(
    private val fpRepository: FileRepository,
    private val context: Application
) : ViewModel() {

    private val _selectedFiles : MutableMap<AndroidFile, Boolean> = mutableMapOf()
    private var lastState : List<AndroidFile> = listOf()
    private var observersData : MutableList<DataLoadedObserver> = mutableListOf()
    private var observersSelected : MutableList<SelectionObserver> = mutableListOf()
    var lastFilterMode : FilterMode = FilterMode.ALL
        private set

    init {
        // forwarding new states of repository to view model list
        viewModelScope.launch {
            fpRepository.resultURIs.collect { files: List<AndroidFile> ->
                lastState = files

                // notify observers (usually fragments) that data was loaded
                for (observer in observersData) {
                    observer.onDataLoaded()
                }
            }
        }
    }

    fun getFiles(filterMode: FilterMode) {
        viewModelScope.launch {
            fpRepository.getFiles(context, filterMode)
            lastFilterMode = filterMode
        }
    }

    fun setFileSelected(file: AndroidFile, isSelected: Boolean) {
        _selectedFiles[file] = isSelected
        observersSelected.forEach { observer -> observer.onSelection(file, isSelected) }
    }

    fun isFileSelected(file: AndroidFile) : Boolean {
        return _selectedFiles[file] ?: false
    }

    fun deselectAll() {
        val copy = _selectedFiles.map { it.key }
        _selectedFiles.clear()
        observersSelected.forEach { observer -> observer.onDeselectAll(copy) }
    }
    
    fun getSelectedFiles(): List<AndroidFile> {
        return _selectedFiles.keys.toList().filter { _selectedFiles[it] ?: false }
    }

    // retain last loaded files, so view can access data after configuration change
    fun lastLoadedFiles(): List<AndroidFile> = lastState

    fun observeLoadedData(observer: DataLoadedObserver) {
        observersData.add(observer)
    }

    fun stopObservingLoadedData(observer: DataLoadedObserver) {
        observersData.remove(observer)
    }
    
    fun observeSelectedFiles(observer: SelectionObserver) {
        observersSelected.add(observer)
    }
    
    fun stopObservingSelectedFiles(observer: SelectionObserver) {
        observersSelected.remove(observer)
    }
}

interface DataLoadedObserver {
    fun onDataLoaded()
}

interface SelectionObserver {
    fun onSelection(file: AndroidFile, isSelected: Boolean)
    fun onDeselectAll(selected: List<AndroidFile>)
}