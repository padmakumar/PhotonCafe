package com.photon.phocafe

import android.app.Application
import android.support.multidex.MultiDex

import com.squareup.leakcanary.LeakCanary

/**
 * Created by padmakumar_m on 11/3/2017.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}
