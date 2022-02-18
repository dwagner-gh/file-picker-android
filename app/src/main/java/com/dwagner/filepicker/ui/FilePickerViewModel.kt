package com.dwagner.filepicker.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwagner.filepicker.FilterMode
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.io.FileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class FPViewState {
    abstract val files : List<AndroidFile>

    data class FilesLoaded(override val files: List<AndroidFile>) : FPViewState() {}
}

class FilePickerViewModel(
    private val fpRepository: FileRepository,
    private val context: Application
) : ViewModel() {

    private var _states : MutableStateFlow<FPViewState> = MutableStateFlow(FPViewState.FilesLoaded(listOf()))
    private val _selectedFiles : MutableList<AndroidFile> = mutableListOf()
    private var lastState : List<AndroidFile> = listOf()
    var lastFilterMode : FilterMode = FilterMode.ALL
        private set
    val states = _states.asStateFlow()

    init {
        // forwarding new states of repository to view model list
        viewModelScope.launch {
            fpRepository.resultURIs.collect { files: List<AndroidFile> ->
                lastState = files
                _states.emit(FPViewState.FilesLoaded(files))
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
        // toList() called in order to create a new list, otherwise collection isn't triggered
        // because it's the same list object
        if (isSelected) {
            _selectedFiles.add(file)
        }
        else {
            _selectedFiles.remove(file)
        }

    }
    
    fun deselectAll() {
        _selectedFiles.removeAll{ true }
    }
    
    fun getSelectedFiles(): List<AndroidFile> {
        return _selectedFiles
    }

    // retain last loaded files, so view can access data after configuration change
    fun lastLoadedFiles(): List<AndroidFile> = lastState
}