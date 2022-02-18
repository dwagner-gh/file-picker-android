package com.dwagner.filepicker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.AboutBinding

class AboutFragment : Fragment() {

    private var binding: AboutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = AboutBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.aboutContent?.loadUrl("file:///android_asset/" + getString(R.string.about_html))
        super.onViewCreated(view, savedInstanceState)
    }
}