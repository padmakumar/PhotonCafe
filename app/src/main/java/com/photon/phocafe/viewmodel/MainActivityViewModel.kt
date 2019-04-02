package com.photon.phocafe.viewmodel


import android.arch.lifecycle.ViewModel

import com.photon.phocafe.Filters

/**
 * ViewModel for.
 */

class MainActivityViewModel : ViewModel() {

    var isSigningIn: Boolean = false
    var filters: Filters

    init {
        isSigningIn = false
        filters = Filters.getDefault()
    }
}
