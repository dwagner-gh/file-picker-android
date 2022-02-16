package com.dwagner.filepicker.ui

import android.app.Application
import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.io.FileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FilePickerViewModel(private val fpRepository: FileRepository, private val context: Application) : ViewModel() {

    private val _states = MutableStateFlow(listOf<AndroidFile>())
    val states = _states.asStateFlow()
    private var job: Job? = null

    init {
        // forwarding new states of repository to view model flow
        job = viewModelScope.launch {
            fpRepository.resultURIs.collect { files ->
                _states.emit(files)
            }
        }
    }

    fun getFiles() {
        viewModelScope.launch {
            fpRepository.getFiles(context)
        }
    }
}