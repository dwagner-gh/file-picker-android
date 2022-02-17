package com.dwagner.filepicker.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dwagner.filepicker.AppConstants
import com.dwagner.filepicker.FilterMode
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.FilePickerBinding
import com.dwagner.filepicker.io.AndroidFile
import com.dwagner.filepicker.ui.selected.SelectedItemAdapter
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel


class FilePickerFragment : Fragment() {

    private var binding: FilePickerBinding? = null
    private val fpViewModel: FilePickerViewModel by viewModel()
    private val clickedItems = mutableListOf<AndroidFile>()

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
        val fileItemAdapter = FileItemAdapter(layoutInflater) { file, isChecked ->
            // isChecked represents the current checked status of the file
            // it need to be inverted and passed to the view model
            fpViewModel.setFileChecked(file, !isChecked)
        }

        val selectedAdapter =
            SelectedItemAdapter(layoutInflater) { file: AndroidFile ->
                // all items in this adapter are selected, when clicked they should be removed
                fpViewModel.setFileChecked(file, false)
            }

        // setting up RecyclerViews
        binding?.items?.adapter = fileItemAdapter

        val selectedItemsList = binding?.selectedItems
        selectedItemsList?.adapter = selectedAdapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        selectedItemsList?.layoutManager = layoutManager


        // setting up collection of new files and new selections
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fpViewModel.states.collect { state ->
                when (state) {
                    is FPViewState.FilesLoaded -> {
                        // if only files are loaded, select nothing
                        fileItemAdapter.submitList(state.files.map { it to false })
                    }
                    is FPViewState.FilesSelected -> {
                        // map current list of adapter for all files to new selected items
                        val mapped = fileItemAdapter.currentList
                            .map { it.first to state.files.contains(it.first) }
                        fileItemAdapter.submitList(mapped)
                        // this adapter only shows selected items, so list can be used directly
                        selectedAdapter.submitList(state.files)
                        if (state.files.isEmpty()) {
                            selectedItemsList?.visibility = View.GONE
                            binding?.selectedView?.visibility = View.GONE
                        }
                        else {
                            selectedItemsList?.visibility = View.VISIBLE
                            binding?.selectedView?.visibility = View.VISIBLE
                            binding?.selectedText?.text =
                                String.format(getString(R.string.selected_text), state.files.size)

                        }
                    }
                }

            }
        }
        binding?.deselectAll?.setOnClickListener { fpViewModel.deselectAll() }
        fpViewModel.getFiles(FilterMode.ALL)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_picker, menu)
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

    private fun safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (t: ActivityNotFoundException) {
            Log.e(AppConstants.LOGGING_TAG, "Exception starting $intent", t)
            Toast.makeText(requireActivity(), R.string.oops, Toast.LENGTH_LONG).show()
        }
    }
}