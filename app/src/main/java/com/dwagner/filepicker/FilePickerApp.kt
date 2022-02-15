package com.dwagner.filepicker

import android.app.Application
import androidx.viewbinding.BuildConfig
import com.dwagner.filepicker.ui.FilePickerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class FilePickerApp : Application() {

    private val koinModule = module {
        viewModel { FilePickerViewModel() }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FilePickerApp)
            // androidLogger() crashes; current workaround:
            // https://github.com/InsertKoinIO/koin/issues/1188#issuecomment-970240532
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            modules(koinModule)
        }
    }
}