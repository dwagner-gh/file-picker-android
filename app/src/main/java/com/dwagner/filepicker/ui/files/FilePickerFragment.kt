package com.dwagner.filepicker.ui.files

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.FilePickerBinding
import com.dwagner.filepicker.ui.files.selected.SelectedItemAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class FilePickerFragment : Fragment(), ViewStateChangeObserver {

    private var binding: FilePickerBinding? = null
    private val fpViewModel: FilePickerViewModel by viewModel()
    private lateinit var selectedItemAdapter: SelectedItemAdapter
    private lateinit var fileItemAdapter: FileItemAdapter

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                // permission granted, set up view
                permissionGranted()
            } else {
                // permission not granted
                Toast.makeText(requireActivity(), R.string.need_permission, Toast.LENGTH_SHORT)
                    .show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FilePickerBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val readStoragePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        when (readStoragePermission) {
            PackageManager.PERMISSION_GRANTED -> {
                permissionGranted()
            }
            PackageManager.PERMISSION_DENIED -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun permissionGranted() {
        fileItemAdapter = FileItemAdapter(
            layoutInflater,
            fpViewModel.viewModelScope
        ) { file, isSelected -> fpViewModel.setFileSelected(file, isSelected) }


        selectedItemAdapter = SelectedItemAdapter(
            layoutInflater,
            fpViewModel.viewModelScope
            // always want to deselect when a file is selected
        ) { file -> fpViewModel.setFileSelected(file, false) }


        fpViewModel.observeViewStateChange(fileItemAdapter)
        fpViewModel.observeViewStateChange(selectedItemAdapter)
        fpViewModel.observeViewStateChange(this)

        // emits current state (last loaded data and selected files)
        fpViewModel.init()

        // setting up RecyclerViews
        binding?.items?.adapter = fileItemAdapter
        binding?.selectedItems?.adapter = selectedItemAdapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.selectedItems?.layoutManager = layoutManager

        binding?.deselectAll?.setOnClickListener { fpViewModel.deselectAll() }
        binding?.acceptChoice?.setOnClickListener {
            // show URIs of all selected files in a dialog
            val fileURIs =
                fpViewModel.getSelectedFiles().map { it.fileURI.toString() }.toTypedArray()
            ShowURIsDialogFragment(fileURIs).show(childFragmentManager, ShowURIsDialogFragment.TAG)
            this.fpViewModel.deselectAll()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_picker, menu)
        when (fpViewModel.lastFilterMode) {
            FilterMode.ALL -> menu.findItem(R.id.all).isChecked = true
            FilterMode.VIDEO -> menu.findItem(R.id.video).isChecked = true
            FilterMode.PHOTO -> menu.findItem(R.id.photo).isChecked = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.all -> {
            fpViewModel.getFiles(FilterMode.ALL)
            item.isChecked = true
            true
        }
        R.id.photo -> {
            fpViewModel.getFiles(FilterMode.PHOTO)
            item.isChecked = true
            true
        }
        R.id.video -> {
            fpViewModel.getFiles(FilterMode.VIDEO)
            item.isChecked = true
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        binding = null
        fpViewModel.stopObservingViewStateChange(this)
        fpViewModel.stopObservingViewStateChange(fileItemAdapter)
        fpViewModel.stopObservingViewStateChange(selectedItemAdapter)

        super.onDestroyView()
    }

    override fun onStateChange(stateChange: ViewStateChange) {
        // type of view state change doesn't matter, we only need to determine
        // whether or not the overview of selected files needs to be shown or hidden
        if (fpViewModel.getSelectedFiles().isEmpty()) {
            // handle updating if no files selected
            binding?.selectedView?.visibility = View.GONE

        } else {
            // handling updating if files are selected
            binding?.selectedView?.visibility = View.VISIBLE
            binding?.selectedText?.text =
                String.format(
                    getString(R.string.selected_text),
                    fpViewModel.getSelectedFiles().size
                )
        }
    }
}