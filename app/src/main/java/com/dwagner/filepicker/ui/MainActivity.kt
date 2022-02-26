package com.dwagner.filepicker.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    var selectedPaths = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, selectedPaths)
        binding.uris.adapter = adapter
        binding.toolbar.title = getString(R.string.title_selected_files)
        binding.selectedFilesText.text = String.format(getString(R.string.selected_text), 0)

        val selectImagesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    selectedPaths.clear()
                    // if multiple image selected
                    if (data?.clipData != null) {
                        val count = data.clipData?.itemCount ?: 0

                        for (i in 0 until count) {
                            val imageUri: Uri? = data.clipData?.getItemAt(i)?.uri
                            imageUri?.let {
                                selectedPaths.add(it.toString())
                            }
                        }

                        binding.selectedFilesText.text =
                            String.format(getString(R.string.selected_text), count)
                    }

                    // if single image selected
                    else if (data?.data != null) {
                        val imageUri: Uri? = data.data
                        imageUri?.let {
                            selectedPaths.add(it.toString())
                        }

                        binding.selectedFilesText.text =
                            String.format(getString(R.string.selected_text), 1)
                    }

                    adapter.notifyDataSetChanged()
                }
            }

        binding.selectMediaFiles.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "*/*"
            // other categories work as well, but usually open system file picker
            // and from there the file picker app can be selected
            intent.addCategory(Intent.CATEGORY_APP_GALLERY)
            selectImagesActivityResult.launch(Intent.createChooser(intent, getString(R.string.choose_files)))
        }
    }

}