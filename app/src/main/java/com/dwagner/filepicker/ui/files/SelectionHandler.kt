package com.dwagner.filepicker.ui.files

import com.dwagner.filepicker.io.AndroidFile

interface SelectionHandler {
    fun onSelection(file: AndroidFile, isSelected: Boolean)
}