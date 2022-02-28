package com.dwagner.filepicker

import android.os.SystemClock
import android.view.View

/**
 * Useful extension function to avoid fast double clicking on views,
 * which normally would result in unexpected behaviour.
 * Clicks that happen during the [debounce time][debounceTime] are ignored.
 * [Source][https://stackoverflow.com/a/56462539/2852865]
 *
 * @param debounceTime Determines after how many milliseconds a view can be clicked again
 * (Default 600ms). The given [action][action] is executed, if since the last click enough
 * milliseconds have passed.
 *
 * @param action action to be executed if since the last click the
 * [debounce period][debounceTime] is over.
 */
fun View.clickWithDebounce(debounceTime: Long = 600L, action: (View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action(v)

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}