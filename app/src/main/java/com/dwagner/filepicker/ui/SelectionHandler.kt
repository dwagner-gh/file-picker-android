package com.dwagner.filepicker.ui

import com.dwagner.filepicker.io.AndroidFile

interface SelectionHandler {
    fun onSelection(file: AndroidFile, isSelected: Boolean)
}