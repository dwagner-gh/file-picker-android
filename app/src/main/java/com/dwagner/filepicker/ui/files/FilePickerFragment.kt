package com.dwagner.filepicker.ui.files

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
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

        // determines if fragment needs to return file uris to an activity, and if so, if only
        // a subset of files are allowed (videos, images..)
        val intent = requireActivity().intent
        if(intent.action == Intent.ACTION_PICK || intent.action == Intent.ACTION_GET_CONTENT) {
            // another activity wants file URIs as result
            when {
                // only image URIs should be returned
                intent.type?.startsWith("image") == true -> fpViewModel.init(FilterMode.PHOTO)
                // only video URIs should be returned
                intent.type?.startsWith("video") == true -> fpViewModel.init(FilterMode.VIDEO)
                else -> fpViewModel.init(fpViewModel.lastFilterMode ?: FilterMode.ALL)
            }
        }

        else {
            fpViewModel.init(fpViewModel.lastFilterMode ?: FilterMode.ALL)
        }


        // setting up RecyclerViews
        binding?.items?.adapter = fileItemAdapter
        binding?.selectedItems?.adapter = selectedItemAdapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.selectedItems?.layoutManager = layoutManager

        binding?.deselectAll?.setOnClickListener { fpViewModel.deselectAll() }
        binding?.acceptChoice?.setOnClickListener {
            val selectedFiles = fpViewModel.getSelectedFiles()

            if (requireActivity().intent.action == Intent.ACTION_GET_CONTENT ||
                requireActivity().intent.action == Intent.ACTION_PICK) {
                // return intent result
                if(selectedFiles.isNotEmpty()) {
                    val resultIntent = Intent(requireActivity().intent.action)
                    val clipData = ClipData.newUri(requireActivity().contentResolver, "", selectedFiles[0].fileURI)

                    for (i in 1 until selectedFiles.size) {
                        clipData.addItem(ClipData.Item(selectedFiles[i].fileURI))
                    }

                    resultIntent.clipData = clipData
                    requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                    requireActivity().finish()
                }
            }

            else {
                // don't return intent result, show uris in a dialog
                ShowURIsDialogFragment(selectedFiles.map { it.fileURI.toString() }.toTypedArray()).show(childFragmentManager, ShowURIsDialogFragment.TAG)
            }

            this.fpViewModel.deselectAll()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        val intent = requireActivity().intent
        // when activity was started with the intention of retrieving file uris, and file selection
        // was limited to videos or photos, the filter menu is hidden
        val showMenu =
            !((intent.action == Intent.ACTION_PICK || intent.action == Intent.ACTION_GET_CONTENT)
                    && intent.type != "*/*") // mime type != */* means either image/* or video/*

        if (!showMenu) {
            return
        }
        // menu should be shown
        inflater.inflate(R.menu.actions_picker, menu)
        when (fpViewModel.lastFilterMode) {
            FilterMode.ALL -> menu.findItem(R.id.all).isChecked = true
            FilterMode.VIDEO -> menu.findItem(R.id.video).isChecked = true
            FilterMode.PHOTO -> menu.findItem(R.id.photo).isChecked = true
            null -> menu.findItem(R.id.all).isChecked = true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.all -> {
            fpViewModel.loadFiles(FilterMode.ALL)
            item.isChecked = true
            true
        }
        R.id.photo -> {
            fpViewModel.loadFiles(FilterMode.PHOTO)
            item.isChecked = true
            true
        }
        R.id.video -> {
            fpViewModel.loadFiles(FilterMode.VIDEO)
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
        val updateSelectedView = {
            binding?.selectedView?.visibility = if (fpViewModel.getSelectedFiles().isEmpty()) View.GONE else View.VISIBLE
            binding?.selectedText?.text =
                String.format(
                    getString(R.string.selected_text),
                    fpViewModel.getSelectedFiles().size
                )
        }
        when(stateChange) {
            is ViewStateChange.SelectFiles -> updateSelectedView()
            is ViewStateChange.SelectFile -> updateSelectedView()
            else -> {} // do nothing
        }
    }
}