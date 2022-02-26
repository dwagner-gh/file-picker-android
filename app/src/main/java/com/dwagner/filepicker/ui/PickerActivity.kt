package com.dwagner.filepicker.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.dwagner.filepicker.R
import com.dwagner.filepicker.databinding.ActivityPickerBinding

class PickerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    // navigation component was initialized, findNavController(id) works now
    // called whenever the up (back) button is pressed
    override fun onSupportNavigateUp() =
        navigateUp(findNavController(R.id.nav_host), appBarConfiguration)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPickerBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // need to use findNavController() on NavFragment directly, because navigation component
        // hasn't been initialized yet in onCreate(...)
        // creates the up (back) button in the toolbar
        supportFragmentManager.findFragmentById(R.id.nav_host)?.findNavController()?.let { nav ->
            appBarConfiguration = AppBarConfiguration(nav.graph)
            setupActionBarWithNavController(nav, appBarConfiguration)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actions_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.about_item -> {
            findNavController(R.id.nav_host).navigate(R.id.globalShowAbout)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}