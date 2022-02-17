package com.dwagner.filepicker.ui

import com.dwagner.filepicker.io.AndroidFile

typealias RowClickHandler = (AndroidFile, Boolean) -> Unit
typealias OverviewClickHandler = (file: AndroidFile) -> Unit