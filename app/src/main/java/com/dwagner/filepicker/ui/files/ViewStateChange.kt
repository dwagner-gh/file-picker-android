package com.dwagner.filepicker.ui.files

import com.dwagner.filepicker.io.AndroidFile

sealed class ViewStateChange {
    data class SelectFiles(val files: Map<AndroidFile, Boolean>) : ViewStateChange()
    data class SelectFile(val file: AndroidFile, val isSelected: Boolean) : ViewStateChange()
    data class DataLoaded(val files: List<AndroidFile>, val filter: FilterMode) : ViewStateChange()
}

interface ViewStateChangeObserver {
    fun onStateChange(stateChange: ViewStateChange)
}