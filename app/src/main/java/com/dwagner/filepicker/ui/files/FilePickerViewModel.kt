package com.dwagner.filepicker.ui.files

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.io.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilePickerViewModel(
    private val fpRepository: FileRepository,
    private val context: Application
) : ViewModel() {

    private val _selectedFiles: MutableMap<AndroidFile, Boolean> = mutableMapOf()
    private var lastLoadedFiles: List<AndroidFile> = listOf()

    // using set to avoid calling the same observer twice
    private var viewStateObservers: MutableSet<ViewStateChangeObserver> = mutableSetOf()
    private var job : Job? = null
    var lastFilterMode: FilterMode? = null
        private set

    fun loadFiles(filterMode: FilterMode) {
        job?.cancel()
        job = viewModelScope.launch {
            fpRepository.resultURIs.collect { files: List<AndroidFile> ->
                lastLoadedFiles = files.toList()

                // notify observers (usually fragments) that data was loaded
                viewStateObservers.forEach {
                    it.onStateChange(ViewStateChange.DataLoaded(files.toList(), filterMode))
                }
            }
        }
        lastFilterMode = filterMode
        viewModelScope.launch {
            fpRepository.getFiles(context, filterMode)
        }
    }

    fun setFileSelected(file: AndroidFile, isSelected: Boolean) {
        if (isSelected) {
            _selectedFiles[file] = true
        } else {
            _selectedFiles.remove(file)
        }
        viewModelScope.launch {
            viewStateObservers.forEach {
                it.onStateChange(ViewStateChange.SelectFile(file, isSelected))
            }
        }
    }

    fun isFileSelected(file: AndroidFile): Boolean {
        return _selectedFiles[file] ?: false
    }

    fun deselectAll() {
        val copy = _selectedFiles.toMap()
        _selectedFiles.clear()
        viewModelScope.launch {
            viewStateObservers.forEach {
                // map all selected files to false, meaning they are no longer selected
                it.onStateChange(ViewStateChange.SelectFiles(copy.mapValues { false }))
            }
        }
    }

    fun getSelectedFiles(): List<AndroidFile> {
        return _selectedFiles.keys.toList()
    }

    fun observeViewStateChange(observer: ViewStateChangeObserver) {
        viewStateObservers.add(observer)
    }

    fun stopObservingViewStateChange(observer: ViewStateChangeObserver) {
        viewStateObservers.remove(observer)
    }

    /**
     * Initializes the view model and it's observers. Either loads new files,
     * or emits the last loaded and selected files, if filter mode is the same.
     **/
    fun init(filterMode: FilterMode) {
        if (lastLoadedFiles.isNotEmpty() && filterMode == lastFilterMode) {
            val lastLoadedCopy = lastLoadedFiles.toList()
            viewStateObservers.forEach {
                it.onStateChange(ViewStateChange.DataLoaded(lastLoadedCopy, filterMode))
            }
        }

        else {
            loadFiles(filterMode)
        }

        val selectedFilesCopy = _selectedFiles.toMap()
        viewStateObservers.forEach { it.onStateChange(ViewStateChange.SelectFiles(selectedFilesCopy)) }
    }
}

