package com.dwagner.filepicker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dwagner.filepicker.FilterMode
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.FilePickerBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.selected.SelectedItemAdapter
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel


class FilePickerFragment : Fragment(), SelectionHandler {

    private var binding: FilePickerBinding? = null
    private val fpViewModel: FilePickerViewModel by viewModel()
    private lateinit var selectedAdapter : SelectedItemAdapter
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
        fileItemAdapter = FileItemAdapter(layoutInflater, this)

        selectedAdapter = SelectedItemAdapter(layoutInflater, this)

        if (fpViewModel.lastLoadedFiles().isNotEmpty()) {
            // load data of view model if data is present
            fileItemAdapter.submitList(fpViewModel.lastLoadedFiles().map { it to fpViewModel.getSelectedFiles().contains(it) })
        }

        else {
            // get all files, because no files are loaded from previous runs
            fpViewModel.getFiles(FilterMode.ALL)
        }

        // update selection ui to represent selected items of view model
        this.updateSelectionUI()

        // setting up RecyclerViews
        binding?.items?.adapter = fileItemAdapter
        binding?.selectedItems?.adapter = selectedAdapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.selectedItems?.layoutManager = layoutManager

        // setting up collection of new files and new selections
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fpViewModel.states.collect { state ->
                when (state) {
                    is FPViewState.FilesLoaded -> {
                        // if only files are loaded, select nothing
                        fileItemAdapter.submitList(state.files.map { it to fpViewModel.getSelectedFiles().contains(it) })
                    }
                }

            }
        }

        binding?.deselectAll?.setOnClickListener { this.deselectAll() }
        binding?.acceptChoice?.setOnClickListener {
            // show URIs of all selected files in a dialog
            val fileURIs =
                fpViewModel.getSelectedFiles().map { it.fileURI.toString() }.toTypedArray()
            ShowURIsDialogFragment(fileURIs).show(childFragmentManager, ShowURIsDialogFragment.TAG)
            this.deselectAll()
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
        super.onDestroyView()
    }

    override fun onSelection(file: AndroidFile, isSelected: Boolean) {
        fpViewModel.setFileSelected(file, isSelected)
        updateSelectionUI()
    }

    private fun deselectAll() {
        fpViewModel.deselectAll()
        updateSelectionUI()
    }

    private fun updateSelectionUI() {
        if (fpViewModel.getSelectedFiles().isEmpty()) {
            // handle updating if no files selected
            binding?.selectedView?.visibility = View.GONE
            selectedAdapter.submitList(listOf())
            fileItemAdapter.submitList(fpViewModel.lastLoadedFiles().map { it to false })

        } else {
            // handling updating if files are selected
            selectedAdapter.submitList(fpViewModel.getSelectedFiles().toList())
            fileItemAdapter.submitList(
                fpViewModel.lastLoadedFiles()
                    .map { it to fpViewModel.getSelectedFiles().contains(it)})

            binding?.selectedView?.visibility = View.VISIBLE
            binding?.selectedText?.text =
                String.format(getString(R.string.selected_text), fpViewModel.getSelectedFiles().size)
        }
    }
}