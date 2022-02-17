package com.dwagner.filepicker.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.dwagner.filepicker.R

class ShowURIsDialogFragment(private val uris: Array<String>) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setItems(uris) { dialogInterface: DialogInterface, i: Int -> }
            .setTitle(R.string.chosen_uris)
            .setPositiveButton(getString(android.R.string.ok)) { _,_ -> }
            .create()

    companion object {
        const val TAG = "ShowURIsDialogFragment"
    }
}
