package com.dwagner.filepicker.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwagner.filepicker.FilterMode
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.io.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class FPViewState {
    abstract val files : List<AndroidFile>

    data class FilesSelected(override val files: List<AndroidFile>) : FPViewState() {}
    data class FilesLoaded(override val files: List<AndroidFile>) : FPViewState() {}
}

class FilePickerViewModel(
    private val fpRepository: FileRepository,
    private val context: Application
) : ViewModel() {

    private var _states : MutableStateFlow<FPViewState> = MutableStateFlow(FPViewState.FilesLoaded(listOf()))
    private var _selectedFiles : MutableList<AndroidFile> = mutableListOf()
    val states = _states.asStateFlow()

    init {
        // forwarding new states of repository to view model list
        viewModelScope.launch {
            fpRepository.resultURIs.collect { files: List<AndroidFile> ->
                _selectedFiles = files.filter { _selectedFiles.contains(it) }.toMutableList()
                _states.emit(FPViewState.FilesLoaded(files))
            }
        }
    }

    fun getFiles(filterMode: FilterMode) {
        viewModelScope.launch {
            fpRepository.getFiles(context, filterMode)
        }
    }

    fun setFileChecked(file: AndroidFile, isChecked: Boolean) {
        // toList() called in order to create a new list, otherwise collection isn't triggered
        // because it's the same list object
        if (isChecked) {
            _selectedFiles.add(file)
            viewModelScope.launch { _states.emit(FPViewState.FilesSelected(_selectedFiles.toList())) }
        }
        else {
            _selectedFiles.remove(file)
            viewModelScope.launch { _states.emit(FPViewState.FilesSelected(_selectedFiles.toList())) }
        }

    }
    
    fun deselectAll() {
        _selectedFiles = mutableListOf()
        viewModelScope.launch { _states.emit(FPViewState.FilesSelected(_selectedFiles.toList())) }
    }
    
    fun getSelectedFiles(): List<AndroidFile> {
        return _selectedFiles
    }
}