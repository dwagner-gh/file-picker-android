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
import androidx.recyclerview.widget.DividerItemDecoration
import com.dwagner.filepicker.AppConstants
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.FilePickerBinding
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class FilePickerFragment : Fragment() {

    private var binding: FilePickerBinding? = null
    private val fpViewModel: FilePickerViewModel by viewModel()

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
                Toast.makeText(requireActivity(), R.string.need_permission, Toast.LENGTH_SHORT).show()
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
        val readStoragePermission= ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE)

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
        val fileItemAdapter = FileItemAdapter(
            layoutInflater,
            onRowClicked = { /** TODO create click handling **/ })

        // setting up RecyclerView
        binding?.items?.apply {
            this.adapter = fileItemAdapter
        }

        // setting up collection of new files
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fpViewModel.states.collect { state ->
                fileItemAdapter.submitList(state)
                Log.d(AppConstants.LOGGING_TAG, "Media Store images collected")
            }
        }
        fpViewModel.getFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
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